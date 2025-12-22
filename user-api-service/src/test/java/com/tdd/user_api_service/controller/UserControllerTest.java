package com.tdd.user_api_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

//import static org.hamcrest.CoreMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;


    @Test
    void whenPostUser_thenReturns201Created() throws Exception {
        CreateUserRequest request = new CreateUserRequest(
                "john.doe@example.com",
                "John",
                "Doe",
                "securePassword123"
        );

        UserResponse expectedResponse = new UserResponse(
                "507f1f77bcf86cd799439011",
                "john.doe@example.com",
                "John",
                "Doe"
        );

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(expectedResponse);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())  
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("507f1f77bcf86cd799439011"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
        
        verify(userService, times(1)).createUser(any(CreateUserRequest.class));
    }
}
