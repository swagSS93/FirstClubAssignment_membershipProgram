package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.User;
import com.firstClubAssignment.membershipProgram.model.CreateUserRequest;
import com.firstClubAssignment.membershipProgram.model.UserProfileResponse;
import com.firstClubAssignment.membershipProgram.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserProfileResponse createUser(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("User already exists with email: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .phoneNo(request.phoneNo())
                .email(request.email())
                .roles(Set.of("ROLE_USER"))
                .build();

        User savedUser = userRepository.save(user);
        return mapToProfileResponse(savedUser);
    }

    @Override
    @Transactional
    public UserProfileResponse createAdmin(CreateUserRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Admin already exists with email: " + request.email());
        }

        User user = User.builder()
                .name(request.name())
                .phoneNo(request.phoneNo())
                .email(request.email())
                .roles(Set.of("ROLE_ADMIN"))
                .build();

        User savedUser = userRepository.save(user);
        return mapToProfileResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));
        return mapToProfileResponse(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        String planName = (user.getMembershipPlan() != null) ? user.getMembershipPlan().getName() : null;
        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getPhoneNo(),
                user.getEmail(),
                planName,
                user.getTier(),
                user.getMembershipStartDate(),
                user.getMembershipExpiryDate()
        );
    }
}
