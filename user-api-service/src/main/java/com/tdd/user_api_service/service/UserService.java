package com.tdd.user_api_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;

@Service
public interface UserService {
	
	UserResponse createUser(CreateUserRequest request);
	
	List<User> getAllUsers();
	
	Optional<User> getUser(String userId);

}
