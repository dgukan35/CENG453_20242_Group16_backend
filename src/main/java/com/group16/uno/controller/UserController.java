package com.group16.uno.controller;

import com.group16.uno.model.User;
import com.group16.uno.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.Optional;

@RestController
@Tag(name = "User Controller")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(description = "Username for the new user", required = true) @RequestParam String username,
            @Parameter(description = "Email for the new user", required = true) @RequestParam String email,
            @Parameter(description = "Password for the new user", required = true) @RequestParam String password)
    {


        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Username cannot be empty");
        }

        if (email == null || !email.contains("@")) {
            return ResponseEntity.badRequest().body("Email is invalid");
        }

        if (password == null || password.length() < 6) {
            return ResponseEntity.badRequest().body("Password must be at least 6 characters long");
        }

        Optional<User> existingUser = userService.getUserByUsername(username);
        if (existingUser.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken");
        }


        User newUser = userService.createUser(username, email, password);


        return ResponseEntity.ok("User registered successfully!");
    }

    @Operation(summary = "Get user details", description = "Fetches user details by id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User details retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public Optional<User> getUser(
            @Parameter(description = "Id of the user to fetch", required = true) @PathVariable String id) {
        return userService.getUserById(id);
    }
}
