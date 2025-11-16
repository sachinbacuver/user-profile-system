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

/**
 * TDD Test Class for the UserService
 * This is a pure unit test, so we use MockitoExtension
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks // This injects the mock(s) into a real instance of UserServiceImpl
    private UserServiceImpl userService;

    /**
     * RED Phase: This test will FAIL until we create UserServiceImpl
     * and implement the DTO mapping and repository save logic.
     */
    @Test
    void whenCreateUser_shouldSaveToRepositoryAndReturnResponse() {
        // 1. Arrange (Setup)
        CreateUserRequest request = new CreateUserRequest(
                "jane.doe@example.com",
                "Jane",
                "Doe",
                "password123"
        );

        // This is the entity we expect the DB to return (with an ID)
        // We must use the record's constructor now
        User savedUser = new User(
                "mock-id-123",
                "jane.doe@example.com",
                "Jane",
                "Doe",
                "password123"
        );

        // Mock the repository's save method
        // When save() is called with *any* User object, return our 'savedUser'
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // 2. Act (Execute the method under test)
        UserResponse response = userService.createUser(request);

        // 3. Assert (Verify the results)

        // --- 3a. Verify the returned DTO is correct ---
        // Accessing record fields with accessor methods
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("mock-id-123");
        assertThat(response.email()).isEqualTo("jane.doe@example.com");
        assertThat(response.firstName()).isEqualTo("Jane");
        assertThat(response.lastName()).isEqualTo("Doe");

        // --- 3b. Verify the repository was called correctly ---
        
        // We use an ArgumentCaptor to "capture" the object that was passed to the save method
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        
        // Verify repository.save() was called exactly once, and capture the argument
        verify(userRepository).save(userCaptor.capture());

        // Get the captured User object
        User userPassedToSave = userCaptor.getValue();

        // Assert that the object passed to save() was mapped correctly from the DTO
        // Accessing record fields with accessor methods
        assertThat(userPassedToSave.id()).isNull(); // ID should be null before saving
        assertThat(userPassedToSave.email()).isEqualTo("jane.doe@example.com");
        assertThat(userPassedToSave.firstName()).isEqualTo("Jane");
        assertThat(userPassedToSave.password()).isEqualTo("password123");
    }
}