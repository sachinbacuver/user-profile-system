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


@WebMvcTest(ProfileController.class)
class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProfileServiceImpl profileService;


    @Test
    void whenPostProfile_thenReturns201Created() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest("user-123");

        UserProfile expectedProfile = new UserProfile("user-123");

        when(profileService.createProfile(any(CreateProfileRequest.class)))
                .thenReturn(expectedProfile);

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  // Expect 201 Created
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value("user-123"));
    }

    @Test
    void whenPostProfileWithExtraFields_thenIgnoreExtraFieldsAndReturn201() throws Exception {
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

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value("user-123"))
                .andExpect(jsonPath("$.bio").doesNotHaveJsonPath());
    }

    @Test
    void whenPostProfileWithoutUserId_thenReturns400BadRequest() throws Exception {
        String requestJson = "{}";

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPostProfileWithNullUserId_thenReturns400BadRequest() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest(null);

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenPostProfileWithEmptyUserId_thenReturns400BadRequest() throws Exception {
        CreateProfileRequest request = new CreateProfileRequest("");

        mockMvc.perform(post("/profiles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}