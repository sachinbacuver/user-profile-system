package com.tdd.profile_api_service.service;

import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;

public interface ProfileService {

	UserProfile createProfile(CreateProfileRequest profileRequest);
}
