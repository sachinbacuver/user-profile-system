package com.tdd.profile_api_service.service;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;
import com.tdd.profile_api_service.repository.ProfileRepository;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    
//    @InjectMocks
//    private ProfileService profileService;
    
    
    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void whenCreateProfile_shouldSaveToRepository() {
        CreateProfileRequest request = new CreateProfileRequest();
        request.setUserId("user-123");
        request.setGender("M");

        UserProfile savedProfile = new UserProfile();
        savedProfile.setUserId("user-123");
        savedProfile.setGender("M");

        when(profileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);

        profileService.createProfile(request);

        verify(profileRepository).save(any(UserProfile.class));
    }
}