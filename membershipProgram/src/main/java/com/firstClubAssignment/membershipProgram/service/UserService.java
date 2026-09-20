package com.firstClubAssignment.membershipProgram.service;


import com.firstClubAssignment.membershipProgram.model.CreateUserRequest;
import com.firstClubAssignment.membershipProgram.model.UserProfileResponse;

public interface UserService {
    UserProfileResponse createUser(CreateUserRequest request);
    UserProfileResponse createAdmin(CreateUserRequest request);
    UserProfileResponse getUserByEmail(String email);
}