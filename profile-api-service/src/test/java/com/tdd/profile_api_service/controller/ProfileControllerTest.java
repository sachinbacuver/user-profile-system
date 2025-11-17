package com.tdd.profile_api_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;
import com.tdd.profile_api_service.service.ProfileServiceImpl;

/**
 * TDD Test Class for ProfileController
 * Following Red-Green-Refactor cycle
 * 
 * Purpose: Profile-api-service creates EMPTY profiles with just userId
 * Returns UserProfile entity directly (no separate Response DTO)
 */
@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileServiceImpl profileService;

    /**
     * RED Phase: This test will FAIL because:
     * - ProfileController doesn't exist yet
     * - CreateProfileRequest DTO doesn't exist
     * - ProfileService doesn't exist
     * - /profiles endpoint doesn't exist
     */
    @Test
    void whenPostProfile_thenReturns201Created() throws Exception {
        // Arrange - Request contains ONLY userId
        CreateProfileRequest request = new CreateProfileRequest("user-123");

        // Mock response - return UserProfile entity directly
        UserProfile expectedProfile = new UserProfile("user-123");
        // Note: bio, profilePhoto, gender are null (empty profile)

        // Mock the service layer
        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenReturn(expectedProfile);

        // Act & Assert - POST to /profiles with just userId
        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  // Expect 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"));
        // bio, profilePhoto, gender will be null in response
    }

    /**
     * Test that extra fields in request are ignored
     * Record with only userId will naturally ignore extra fields
     */
    @Test
    void whenPostProfileWithExtraFields_thenIgnoreExtraFieldsAndReturn201() throws Exception {
        // Arrange - Request with extra fields (should be ignored)
        String requestJson = """
            {
                "userId": "user-123",
                "bio": "Should be ignored",
                "profilePhoto": "Should be ignored",
                "gender": "M"
            }
            """;

        UserProfile expectedProfile = new UserProfile("user-123");

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenReturn(expectedProfile);

        // Act & Assert - Extra fields are ignored, empty profile created
        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.bio").doesNotHaveJsonPath());
    }

    /**
     * Test for missing userId - should return 400 Bad Request
     */
    @Test
    void whenPostProfileWithoutUserId_thenReturns400BadRequest() throws Exception {
        // Arrange - Request without userId
        String requestJson = "{}";

        // Act & Assert
        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test for null userId - should return 400 Bad Request
     */
    @Test
    void whenPostProfileWithNullUserId_thenReturns400BadRequest() throws Exception {
        // Arrange - Request with null userId
        CreateProfileRequest request = new CreateProfileRequest(null);

        // Act & Assert
        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test for empty userId - should return 400 Bad Request
     */
    @Test
    void whenPostProfileWithEmptyUserId_thenReturns400BadRequest() throws Exception {
        // Arrange - Request with empty userId
        CreateProfileRequest request = new CreateProfileRequest("");

        // Act & Assert
        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}