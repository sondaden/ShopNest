package com.sonnhuynhh.shopnest.security;

import com.sonnhuynhh.shopnest.model.Role;
import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);
    
    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId(); // google, facebook
        String providerId = oAuth2User.getAttribute("sub"); // Google uses "sub" as user ID
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        logger.info("OAuth2 Login - Provider: {}, Email: {}, ProviderId: {}", provider, email, providerId);

        User user = processOAuth2User(provider, providerId, email, name, picture);
        
        logger.info("OAuth2 User processed - ID: {}, Username: {}, Email: {}", 
                    user.getId(), user.getUsername(), user.getEmail());

        return new CustomOAuth2User(oAuth2User, provider, email);
    }
    
    private User processOAuth2User(String provider, String providerId, String email, String name, String picture) {
        // First, try to find by email (this is the key - email should be unique)
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        
        if (existingUserByEmail.isPresent()) {
            // User with this email exists - update OAuth2 info if needed
            User user = existingUserByEmail.get();
            logger.info("Found existing user by email: {}", email);
            
            // Update provider info if not set or different
            if (user.getProvider() == null || !user.getProvider().equals(provider)) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                logger.info("Updated provider info for user: {}", user.getUsername());
            }
            
            // Update avatar if user doesn't have one
            if ((user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty()) && picture != null) {
                user.setAvatarUrl(picture);
            }
            
            // Update full name if not set
            if ((user.getFullName() == null || user.getFullName().isEmpty()) && name != null) {
                user.setFullName(name);
            }
            
            return userRepository.save(user);
        }
        
        // Check by provider and providerId
        Optional<User> existingUserByProvider = userRepository.findByProviderAndProviderId(provider, providerId);
        
        if (existingUserByProvider.isPresent()) {
            User user = existingUserByProvider.get();
            logger.info("Found existing user by provider: {}", provider);
            
            // Update avatar if changed
            if (picture != null && !picture.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(picture);
                return userRepository.save(user);
            }
            return user;
        }
        
        // Create new user
        logger.info("Creating new user for email: {}", email);
        User newUser = new User();
        newUser.setProvider(provider);
        newUser.setProviderId(providerId);
        newUser.setEmail(email);
        newUser.setFullName(name);
        newUser.setAvatarUrl(picture);
        
        // Generate unique username from email
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + "_" + counter;
            counter++;
        }
        newUser.setUsername(username);
        
        // Set a random password (user won't use it, they login via OAuth2)
        newUser.setPassword(UUID.randomUUID().toString());
        newUser.setRole(Role.USER);
        newUser.setActive(true);
        
        User savedUser = userRepository.save(newUser);
        logger.info("Created new OAuth2 user - ID: {}, Username: {}", savedUser.getId(), savedUser.getUsername());
        
        return savedUser;
    }
}
