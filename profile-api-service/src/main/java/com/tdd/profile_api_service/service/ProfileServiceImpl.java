package com.tdd.profile_api_service.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.tdd.profile_api_service.dto.CreateProfileRequest;
import com.tdd.profile_api_service.model.UserProfile;
import com.tdd.profile_api_service.repository.ProfileRepository;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    public ProfileServiceImpl(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public UserProfile createProfile(CreateProfileRequest request) {
        UserProfile profile = new UserProfile(request.getUserId());
        
        String bio = request.getBio();
        String profilePhoto = request.getProfilePhoto();
        String gender = request.getGender();
        
        if(bio!=null) {
        	profile.setBio(bio);
        }
        if(profilePhoto!=null) {
        	profile.setProfilePhoto(profilePhoto);
        }

        if(gender!=null) {
        	profile.setGender(gender);
        }
        return profileRepository.save(profile); 
    }
    
    public Optional<UserProfile> getProfile(String userId) {
        return profileRepository.findById(userId); 
    }
}