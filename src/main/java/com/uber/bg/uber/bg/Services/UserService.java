package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.ChangePasswordDTO;
import com.uber.bg.uber.bg.DTOs.ChangeUsernameDTO;
import com.uber.bg.uber.bg.DTOs.CreateUserDTO;
import com.uber.bg.uber.bg.DTOs.LoginUserDTO;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Entities.VerificationCode;
import com.uber.bg.uber.bg.Enumerations.USER_ROLE;
import com.uber.bg.uber.bg.Exceptions.RateLimitException;
import com.uber.bg.uber.bg.Repositories.Jpa.UserRepository;
import com.uber.bg.uber.bg.Repositories.Redis.VerificationCodeRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JwtService service;

    @Autowired
    BlacklistTokenService blacklistTokenService;

    private final RateLimitService limitService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository, EmailService emailService, VerificationCodeRepository verificationCodeRepository, RateLimitService limitService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.verificationCodeRepository = verificationCodeRepository;
        this.limitService = limitService;
    }

    @Transactional
    public void createUser(final CreateUserDTO dto)  {

        if (userRepository.existsByUsername(dto.getUsername())){
            throw new IllegalArgumentException("user with this username already exists");
        }


            User user = User
                .builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                    .phoneNumber(dto.getPhoneNumber())
                .password(passwordEncoder.encode(dto.getPassword()))
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .role(("DRIVER".equalsIgnoreCase(String.valueOf(dto.getRole()))) ? USER_ROLE.DRIVER : USER_ROLE.PASSENGER)
                .build();


        userRepository.save(user);
        log.info("user created!");


    }

    @Transactional(readOnly = true)
    public Map<UUID, String> login(final LoginUserDTO dto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        log.info("{} successfully authenticated", dto.getUsername());

        User user = userRepository.findByUsername(dto.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User records out of sync for: " + dto.getUsername());
        }

        String token = service.generateToken(user.getUsername(), user.getId());

        Map<UUID, String> idStringMap = new HashMap<>();
        idStringMap.put(user.getId(), token);
        return idStringMap;
    }

    @Transactional
    public void logout(final String token){
    long RemainingTimeInMillis = service.getRemainingExpirationTime(token);
     if (RemainingTimeInMillis > 0){
         blacklistTokenService.blacklistToken(token, RemainingTimeInMillis);
     }

    }

    @Transactional
    public void initiateUsernameChangeFlow(final ChangeUsernameDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if (userRepository.existsByUsername(dto.getNewUsername())) {
            throw new IllegalArgumentException("The new username is already taken.");
        }

        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));

        VerificationCode verificationData = new VerificationCode(pin, user.getEmail());
        emailService.sendVerificationCode(user.getEmail(), "Your Identity Verification Code", pin);

        List<VerificationCode> oldCodes = verificationCodeRepository.findByEmail(dto.getEmail());

        if(!oldCodes.isEmpty()){
            verificationCodeRepository.deleteAll(oldCodes);
        }

        verificationCodeRepository.save(verificationData);
    }


    @Transactional
    public String changeUsername(final ChangeUsernameDTO dto, final String code) {

        VerificationCode savedToken = verificationCodeRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));

        if (!savedToken.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("Code does not match this user identity.");
        }

        if (userRepository.existsByUsername(dto.getNewUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        User user = userRepository.findByEmail(dto.getEmail());

        user.setUsername(dto.getNewUsername());
        SecurityContextHolder.clearContext();
        userRepository.saveAndFlush(user);

        verificationCodeRepository.delete(savedToken);

        return service.generateToken(user.getUsername(), user.getId());
    }

    @Transactional
    public void initiatePasswordChange(final ChangePasswordDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail());
        if (user == null || !passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));

        VerificationCode verificationData = new VerificationCode(pin, user.getEmail());
        emailService.sendVerificationCode(user.getEmail(), "Your Identity Verification Code", pin);

        List<VerificationCode> oldCodes = verificationCodeRepository.findByEmail(dto.getEmail());

        if(!oldCodes.isEmpty()){
            verificationCodeRepository.deleteAll(oldCodes);
        }

        verificationCodeRepository.save(verificationData);
    }

    @Transactional
    public void changePassword(final ChangePasswordDTO dto, final String code) {

        VerificationCode savedToken = verificationCodeRepository.findById(code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification code."));

        if (!savedToken.getEmail().equals(dto.getEmail())) {
            throw new IllegalArgumentException("Code does not match this user identity.");
        }

        User user = userRepository.findByEmail(dto.getEmail());

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        userRepository.saveAndFlush(user);

        verificationCodeRepository.delete(savedToken);

    }


    public void resendCode(final String email, HttpServletRequest request) {

        String userIp = request.getHeader("X-Forwarded-For");
        if(userIp == null || userIp.isEmpty()) {
            userIp = request.getRemoteAddr();
        }

        if (!limitService.tryConsume(userIp)) {
            throw new RateLimitException("Too many verification requests. Please wait before trying again.");
        }


        String pin = String.format("%06d", new SecureRandom().nextInt(1000000));

        VerificationCode verificationData = new VerificationCode(pin, email);
        emailService.sendVerificationCode(email, "Your Identity Verification Code", pin);

        List<VerificationCode> oldCodes = verificationCodeRepository.findByEmail(email);

        if(!oldCodes.isEmpty()){
            verificationCodeRepository.deleteAll(oldCodes);
        }

        verificationCodeRepository.save(verificationData);
    }


}

