package com.uber.bg.uber.bg.Services;

import com.uber.bg.uber.bg.DTOs.CreateUserDTO;
import com.uber.bg.uber.bg.DTOs.LoginUserDTO;
import com.uber.bg.uber.bg.Entities.User;
import com.uber.bg.uber.bg.Enumerations.USER_ROLE;
import com.uber.bg.uber.bg.Repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.FileAlreadyExistsException;
import java.time.Duration;
import java.util.HashMap;
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

    private final UserRepository userRepository;

    @Autowired
    RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        // 1. This throws a BadCredentialsException immediately if authentication fails
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        log.info("{} successfully authenticated", dto.getUsername());

        // 2. Fetch the user safely from your repository
        User user = userRepository.findByUsername(dto.getUsername());
        if (user == null) {
            throw new UsernameNotFoundException("User records out of sync for: " + dto.getUsername());
        }

        // 3. Generate token and build response map
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


}

