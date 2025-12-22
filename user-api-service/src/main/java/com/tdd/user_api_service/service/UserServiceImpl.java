package com.tdd.user_api_service.service;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;
import com.tdd.user_api_service.repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    
    
    UserServiceImpl(UserRepository userRepository,KafkaTemplate<String, UserResponse> kafkaTempalte, @Value("${app.kafka.user-created-topic}") String userCreatedTopic){
    	this.userRepository = userRepository;
    	this.kafkaTemplate = kafkaTempalte;
    	this.userCreatedTopic = userCreatedTopic;
    }
    private final KafkaTemplate<String, UserResponse> kafkaTemplate;
    
//    @Value("${app.kafka.user-created-topic}")
    private final String userCreatedTopic;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        
        User userToSave = new User(
                null,
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password() 
        );

        User savedUser = userRepository.save(userToSave);

        UserResponse response =  new UserResponse(
                savedUser.id(),
                savedUser.email(),
                savedUser.firstName(),
                savedUser.lastName()
        );
        
        try {
            kafkaTemplate.send(userCreatedTopic, savedUser.id(), response);
        } catch (Exception e) {
            System.err.println("Failed to send Kafka message: " + e.getMessage());
        }
        
        return response;
    }

	@Override
	public List<User> getAllUsers() {
		List<User> users = userRepository.findAll();
		
		return users;
	}

	@Override
	public Optional<User> getUser(String userId) {
		
		Optional<User> user = userRepository.findById(userId);
		
		
		return user;
	}
    
    
}
