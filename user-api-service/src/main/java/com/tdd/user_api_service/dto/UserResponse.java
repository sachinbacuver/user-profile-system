package com.tdd.user_api_service.dto;

public record UserResponse(
	    String id,
	    String email,
	    String firstName,
	    String lastName
	) {}
