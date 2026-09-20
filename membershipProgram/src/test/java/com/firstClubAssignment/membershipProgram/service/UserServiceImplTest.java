package com.firstClubAssignment.membershipProgram.service;

import com.firstClubAssignment.membershipProgram.entity.User;
import com.firstClubAssignment.membershipProgram.model.CreateUserRequest;
import com.firstClubAssignment.membershipProgram.model.UserProfileResponse;
import com.firstClubAssignment.membershipProgram.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private CreateUserRequest createRequest;

    @BeforeEach
    void setUp() {
        createRequest = new CreateUserRequest("Alice", "1234567890", "alice@example.com");
    }

    @Test
    void createUser_Success() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserProfileResponse response = userService.createUser(createRequest);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.email()).isEqualTo("alice@example.com");
        verify(userRepository).save(argThat(user -> user.getRoles().contains("ROLE_USER")));
    }

    @Test
    void createUser_AlreadyExists_ThrowsException() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> userService.createUser(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User already exists");
    }

    @Test
    void createAdmin_Success() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserProfileResponse response = userService.createAdmin(createRequest);

        assertThat(response.id()).isEqualTo(2L);
        verify(userRepository).save(argThat(user -> user.getRoles().contains("ROLE_ADMIN")));
    }

    @Test
    void getUserByEmail_Success() {
        User user = User.builder()
                .id(1L)
                .name("Alice")
                .email("alice@example.com")
                .phoneNo("1234567890")
                .build();

        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        UserProfileResponse response = userService.getUserByEmail("alice@example.com");

        assertThat(response.email()).isEqualTo("alice@example.com");
        assertThat(response.name()).isEqualTo("Alice");
    }

    @Test
    void getUserByEmail_NotFound_ThrowsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("notfound@example.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }
}