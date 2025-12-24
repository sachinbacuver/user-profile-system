package com.tdd.profile_api_service.service;

import java.util.List;
import java.util.Optional;

import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;

public interface ProfileService {

	UserProfile createProfile(CreateProfileRequest profileRequest);
	Optional<UserProfile> getProfile(String userId);
	List<UserProfile> getAllProfiles();
}
