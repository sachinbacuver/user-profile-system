package com.tdd.user_api_service.dto;

public record CreateUserRequest(
	    String email,
	    String firstName,
	    String lastName,
	    String password
	) {}
