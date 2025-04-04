package com.group16.uno.controller;

import com.group16.uno.model.User;
import com.group16.uno.service.EmailService;
import com.group16.uno.service.UserService;
import com.group16.uno.repository.PasswordTokenRepository;
import com.group16.uno.config.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import com.group16.uno.dto.UserDto;
import org.modelmapper.ModelMapper;

import java.util.*;

import com.group16.uno.repository.PasswordTokenRepository;
import com.group16.uno.model.PasswordResetToken;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication Controller", description = "Register, Login and reset password")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ModelMapper modelMapper;
    private final PasswordTokenRepository passwordTokenRepository;
    private final EmailService emailService;
    public AuthController(UserService userService, AuthenticationManager authenticationManager, JwtService jwtService, ModelMapper modelMapper, PasswordTokenRepository passwordTokenRepository, EmailService emailService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.modelMapper = modelMapper;
        this.passwordTokenRepository = passwordTokenRepository;
        this.emailService = emailService;
    }

    @Operation(summary = "Create a user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String email,
                                      @RequestParam String password) {

        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }

        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Invalid email");
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters");
        }

        Optional<User> existingUser = userService.getUserByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body("Username already taken");
        }

        User createdUser = userService.createUser(username, email, password);
        UserDto userDto = modelMapper.map(createdUser, UserDto.class);
        return ResponseEntity.ok(userDto);
    }

    @Operation(summary = "Sign in")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Invalid username or password")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username,
                                   @RequestParam String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
        }

        User user = userService.getUserByUsername(username).get();
        String jwt = jwtService.generateToken(user.getUsername());

        UserDto userDto = modelMapper.map(user, UserDto.class);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("user", userDto);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Send reset password token to email adress")
    @PostMapping("/reset-password")
    public ResponseEntity<?> sendResetPasswordMail(@RequestParam String email) {
        try {

            Optional<User> optionalUser = userService.getUserByEmail(email);

            if (optionalUser.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found with this email.");
            }

            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();

            PasswordResetToken resetToken = new PasswordResetToken(token, user);
            passwordTokenRepository.save(resetToken);

            String resetLink = "http://localhost:8080/swagger-ui/index.html#/Authentication%20Controller/validateResetTokenUsingGET?token=" + token;

            System.out.println("Reset link: " + resetLink);
            this.emailService.sendResetEmail(user.getEmail(), resetLink, token);

            return ResponseEntity.ok("Password reset link has been sent to your email.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing reset password.");
        }
    }

    @Operation(summary = "Set new password using the reset token")
    @PostMapping("/set-new-password")
    public ResponseEntity<?> setNewPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        Optional<PasswordResetToken> optionalToken = passwordTokenRepository.findByToken(token);
        try{
            if (optionalToken.isEmpty() || optionalToken.get().getExpiryDate().before(new Date())) {
                return ResponseEntity.badRequest().body("Invalid or expired token.");
            }

            PasswordResetToken resetToken = optionalToken.get();
            User user = resetToken.getUser();

            userService.updatePassword(user, newPassword);
            passwordTokenRepository.delete(resetToken);

            return ResponseEntity.ok("Password has been successfully updated.");
        }
        catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while setting the new password.");
        }

    }


    @Operation(summary = "Validate password reset token before resetting password. (Will be used after front end implementation)")
    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            Optional<PasswordResetToken> optionalToken = this.passwordTokenRepository.findByToken(token);

            if (optionalToken.isEmpty() || optionalToken.get().getExpiryDate().before(new Date())) {
                return ResponseEntity.badRequest().body("Invalid or expired token");
            }

            return ResponseEntity.ok("Token is valid.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while validating the token.");
        }
    }




}


