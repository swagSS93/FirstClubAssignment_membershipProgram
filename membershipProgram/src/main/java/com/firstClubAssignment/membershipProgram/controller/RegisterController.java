package com.firstClubAssignment.membershipProgram.controller;

import com.firstClubAssignment.membershipProgram.model.CreateUserRequest;
import com.firstClubAssignment.membershipProgram.model.UserProfileResponse;
import com.firstClubAssignment.membershipProgram.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
@Tag(name = "Register Controller", description = "Endpoints for managing user accounts and profiles")
public class RegisterController {

    private final UserService userService;

    @PostMapping("/user")
    @Operation(summary = "Register a new user", description = "Creates a new user record with basic details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid input data")
    })
    public ResponseEntity<UserProfileResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/admin")
    @Operation(summary = "Register a new admin", description = "Creates a new user record with basic details.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid input data")
    })
    public ResponseEntity<UserProfileResponse> createAdmin(@Valid @RequestBody CreateUserRequest request) {
        UserProfileResponse response = userService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}