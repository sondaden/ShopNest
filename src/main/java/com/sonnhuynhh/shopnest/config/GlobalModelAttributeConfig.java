package com.sonnhuynhh.shopnest.config;

import com.sonnhuynhh.shopnest.model.User;
import com.sonnhuynhh.shopnest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Thêm thông tin user vào model cho tất cả các request
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributeConfig {

    private final UserRepository userRepository;

    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        
        String identifier = auth.getName();
        return userRepository.findByUsername(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElse(null);
    }
    
    @ModelAttribute("currentUserDisplayName")
    public String getCurrentUserDisplayName() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }
        
        // Ưu tiên: fullName > username > email
        if (user.getFullName() != null && !user.getFullName().isEmpty()) {
            return user.getFullName();
        }
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            return user.getUsername();
        }
        return user.getEmail();
    }
}
