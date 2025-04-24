package edu.cit.markahan.Config;

import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
 
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
 
    private static final Logger logger = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
 
    public OAuth2LoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                       Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oauthUser.getAttributes();
        logger.info("OAuth2 login success. Provider attributes: {}", attributes);
        
        String email = null;
        String firstName = null;
        String lastName = null;
        String oauthId = (String) attributes.get("sub");
        
        // Extract email
        if (attributes.containsKey("email")) {
            email = (String) attributes.get("email");
        }
        
        // Extract name
        if (attributes.containsKey("given_name") && attributes.containsKey("family_name")) {
            // Google typically provides these fields
            firstName = (String) attributes.get("given_name");
            lastName = (String) attributes.get("family_name");
        } else if (attributes.containsKey("name")) {
            // If only full name is available, split it
            String fullName = (String) attributes.get("name");
            String[] nameParts = fullName.split(" ", 2);
            firstName = nameParts[0];
            lastName = nameParts.length > 1 ? nameParts[1] : "";
        }
        
        if (email != null) {
            // First try to find user by OAuth ID
            UserEntity existingUser = userRepository.findByOauthId(oauthId);
            
            // If not found by OAuth ID, try by email
            if (existingUser == null) {
                existingUser = userRepository.findByEmail(email);
            }
            
            if (existingUser == null) {
                // Create new user
                UserEntity newUser = new UserEntity();
                newUser.setEmail(email);
                newUser.setOauthId(oauthId);
                newUser.setFirstName(firstName != null ? firstName : "User");
                newUser.setLastName(lastName != null ? lastName : "");
                
                // Generate a secure random password for OAuth users
                String randomPassword = UUID.randomUUID().toString();
                newUser.setPassword(passwordEncoder.encode(randomPassword));
                
                logger.info("Creating new user from OAuth2 login: {}", email);
                userRepository.save(newUser);
            } else {
                // Update existing user's OAuth ID if it's not set
                if (existingUser.getOauthId() == null) {
                    existingUser.setOauthId(oauthId);
                    userRepository.save(existingUser);
                    logger.info("Updated OAuth ID for existing user: {}", email);
                }
                logger.info("Existing user logged in via OAuth2: {}", email);
            }
        } else {
            logger.warn("Could not extract email from OAuth2 attributes");
        }
        
        // Redirect to frontend OAuth2 redirect component
        response.sendRedirect("http://localhost:5173/oauth2/redirect");
    }
}
