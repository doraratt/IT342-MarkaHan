package edu.cit.markahan.Service;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.UserRepository;

@Service
public class UserService {
    @Autowired
    UserRepository urepo;

    public UserService() {
        super();
    }

    public UserEntity postUser(UserEntity user) {
        return urepo.save(user);
    }

    public List<UserEntity> getAllUser() {
        return urepo.findAll();
    }

    @SuppressWarnings("finally")
    public UserEntity putUser(int userId, UserEntity newUserDetails) {
        UserEntity user = new UserEntity();

        try {
            user = urepo.findById(userId).get();
            user.setFirstName(newUserDetails.getFirstName());
            user.setLastName(newUserDetails.getLastName());
            user.setEmail(newUserDetails.getEmail());
            user.setPassword(newUserDetails.getPassword());
        } catch (NoSuchElementException nex) {
            throw new RuntimeException("User " + userId + " not found!");
        } finally {
            return urepo.save(user);
        }
    }

    public String deleteUser(int id) {
        String msg = "";
        if (urepo.findById(id) != null) {
            urepo.deleteById(id);
            msg = "User successfully deleted.";
        } else
            msg = id + " not found.";
        return msg;
    }

    // Signup Method
    public UserEntity registerUser(UserEntity user) {
        if (urepo.findByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }
        return urepo.save(user);
    }

    // Login Method
    public UserEntity loginUser(String email, String password) {
        UserEntity user = urepo.findByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user; // Successful login
        }
        return null; // Login failed
    }

    @Bean
    public CustomOAuth2UserService customOAuth2UserService() {
        return new CustomOAuth2UserService(urepo);
    }
}

class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        logger.info("CustomOAuth2UserService initialized with UserRepository");
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        try {
            logger.info("Loading OAuth2 user from request: {}", userRequest);
            OAuth2User user = super.loadUser(userRequest);
            logger.info("OAuth2 user loaded: {}", user.getAttributes());

            // Extract user information
            String oauthId = user.getAttribute("sub");
            String email = user.getAttribute("email");
            String firstName = user.getAttribute("given_name");
            String lastName = user.getAttribute("family_name");

            logger.info("OAuth2 user details - oauthId: {}, email: {}, firstName: {}, lastName: {}", 
                oauthId, email, firstName, lastName);

            if (email != null && oauthId != null && userRepository != null) {
                UserEntity existingUser = userRepository.findByOauthId(oauthId);
                if (existingUser == null) {
                    existingUser = userRepository.findByEmail(email);
                    if (existingUser == null) {
                        // Create new user
                        UserEntity newUser = new UserEntity();
                        newUser.setEmail(email);
                        newUser.setOauthId(oauthId);
                        newUser.setFirstName(firstName != null ? firstName : "User");
                        newUser.setLastName(lastName != null ? lastName : "");
                        newUser.setPassword("OAUTH2_USER"); // No password needed for OAuth users
                        userRepository.save(newUser);
                        logger.info("Created new user from OAuth2: {}", email);
                    } else {
                        // Update existing user with OAuth ID
                        existingUser.setOauthId(oauthId);
                        userRepository.save(existingUser);
                        logger.info("Linked OAuth ID to existing user: {}", email);
                    }
                }
            } else {
                logger.warn("Missing email, oauthId, or userRepository in OAuth2 attributes");
                if (userRepository == null) {
                    logger.error("UserRepository is null - dependency injection failed");
                }
            }

            return user;
        } catch (Exception e) {
            logger.error("Error in loadUser method", e);
            throw new OAuth2AuthenticationException(null, e.getMessage(), e);
        }
    }
}