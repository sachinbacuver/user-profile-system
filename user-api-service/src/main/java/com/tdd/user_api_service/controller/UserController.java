package com.tdd.user_api_service.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tdd.user_api_service.dto.CreateUserRequest;
import com.tdd.user_api_service.dto.UserResponse;
import com.tdd.user_api_service.model.User;
import com.tdd.user_api_service.service.UserService;

//@CrossOrigin(origins ="http://127.0.0.1:5500")
@CrossOrigin(origins ="https://d1iqlny8ed8jbc.cloudfront.net")
@RestController
@RequestMapping("/users")
public class UserController {
	
private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
//    (produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
    	
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    
    @GetMapping("/getAllUsers")
    public ResponseEntity<List<User>> getAllUsers(){
    	
    	List<User> users = userService.getAllUsers();
    	
    	return ResponseEntity.status(HttpStatus.CREATED).body(users);
    	
    }
    
    
    @GetMapping("/{userId}")
    public Optional<User> getUser(@PathVariable String userId){
    	
    	Optional<User> user = userService.getUser(userId);
    	return user;
    }
}
