package com.firstClubAssignment.membershipProgram.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank(message = "Name is required")
        String name,

        String phoneNo,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email
) {}
