package com.tdd.user_api_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;
import com.tdd.user_api_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks 
    private UserServiceImpl userService;

    @Test
    void whenCreateUser_shouldSaveToRepositoryAndReturnResponse() {
        CreateUserRequest request = new CreateUserRequest(
                "jane.doe@example.com",
                "Jane",
                "Doe",
                "password123"
        );

        User savedUser = new User(
                "mock-id-123",
                "jane.doe@example.com",
                "Jane",
                "Doe",
                "password123"
        );

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.createUser(request);

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("mock-id-123");
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Doe");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        
        verify(userRepository).save(userCaptor.capture());

        User userPassedToSave = userCaptor.getValue();

        assertThat(userPassedToSave.id()).isNull(); 
        assertThat(userPassedToSave.email()).isEqualTo("jane.doe@example.com");
        assertThat(userPassedToSave.firstName()).isEqualTo("Jane");
        assertThat(userPassedToSave.password()).isEqualTo("password123");
    }
}