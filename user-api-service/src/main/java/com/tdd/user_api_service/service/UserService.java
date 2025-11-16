package com.tdd.user_api_service.service;

import org.springframework.stereotype.Service;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;

@Service
public interface UserService {
	
	UserResponse createUser(CreateUserRequest request);

}
