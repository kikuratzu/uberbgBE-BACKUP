package com.uber.bg.uber.bg.Controllers;

import com.uber.bg.uber.bg.DTOs.ChangePasswordDTO;
import com.uber.bg.uber.bg.DTOs.ChangeUsernameDTO;
import com.uber.bg.uber.bg.DTOs.CreateUserDTO;
import com.uber.bg.uber.bg.DTOs.LoginUserDTO;
import com.uber.bg.uber.bg.Services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService service;

    @PostMapping("/createUser")
    public HttpStatus createUser(
            @RequestBody CreateUserDTO dto
            ) {
        service.createUser(dto);
        return HttpStatus.CREATED;
    }

    @PostMapping("/loginUser")
    public Map<UUID, String> loginUser(
            @RequestBody final LoginUserDTO loginUserDTO
            ) {
           return service.login(loginUserDTO);
    }

    @DeleteMapping("/logOutUser")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public HttpStatus logoutUser(@RequestHeader("Authorization") final String token) {
     service.logout(token.substring(7));
     return HttpStatus.ACCEPTED;
    }

    @PostMapping("/request-username-change")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public ResponseEntity<Map<String, String>> requestChange(@RequestBody final ChangeUsernameDTO dto) {
        service.initiateUsernameChangeFlow(dto);
        return ResponseEntity.ok(Map.of("message", "Verification code sent to your email."));
    }

    @PatchMapping("/confirm-username-change")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public ResponseEntity<Map<String, String>> confirmChange(@RequestBody final ChangeUsernameDTO dto,
                                                             @RequestParam final String code) {
        String newToken = service.changeUsername(dto, code);
        return ResponseEntity.ok(Map.of(
                "message", "Username updated successfully!",
                "token", newToken
        ));
    }

    @PostMapping("/request-password-change")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public ResponseEntity<Map<String, String>> requestPasswordChange(@RequestBody final ChangePasswordDTO dto){
        service.initiatePasswordChange(dto);
        return ResponseEntity.ok(Map.of("message", "Verification code sent to your email"));
    }

    @PatchMapping("/confirm-password-change")
    @PreAuthorize("hasAnyRole('PASSENGER','DRIVER','ADMIN')")
    public ResponseEntity<Map<String, String>> confirmPasswordChange(@RequestBody final ChangePasswordDTO dto,
                                                                     @RequestParam final String code) {
        service.changePassword(dto, code);
        return ResponseEntity.ok(Map.of("message","Password updated successfully!"));
    }

    @PostMapping("/resendCode")
    @PreAuthorize("hasAnyRole('DRIVER','PASSENGER','ADMIN')")
    public void resendCode(@RequestParam final String email,
                           HttpServletRequest request) {
        service.resendCode(email, request);
    }

}
