package com.tdd.user_api_service.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;
import com.tdd.user_api_service.repository.UserRepository;

/**
 * This is the "GREEN" implementation.
 * We write the logic to make UserServiceTest pass.
 */
@Service
public class UserServiceImpl implements UserService {

    // The 'final' keyword makes sure this is injected by the constructor
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
        
        // 1. Map the DTO (request) to the Entity (model)
        // We use a null ID because MongoDB will generate it.
        User userToSave = new User(
                null,
                request.email(),
                request.firstName(),
                request.lastName(),
                request.password() // In a real app, you'd hash this!
        );

        // 2. Save the entity to the database (this is mocked in our test)
        // The mock will return the "savedUser" we defined in the test
        User savedUser = userRepository.save(userToSave);

        // 3. Map the saved Entity (which now has an ID) back to a DTO
        // We use the record's accessor methods (e.g., savedUser.id())
        UserResponse response =  new UserResponse(
                savedUser.id(),
                savedUser.email(),
                savedUser.firstName(),
                savedUser.lastName()
        );
        
        try {
            kafkaTemplate.send(userCreatedTopic, savedUser.id(), response);
        } catch (Exception e) {
            // In a real app, handle this! (e.g., retry, log, compensate)
            System.err.println("Failed to send Kafka message: " + e.getMessage());
        }
        
        return response;
    }
    
    
}
