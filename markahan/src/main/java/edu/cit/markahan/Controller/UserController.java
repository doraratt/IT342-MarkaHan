package edu.cit.markahan.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.markahan.Entity.UserEntity;
import edu.cit.markahan.Repository.UserRepository;
import edu.cit.markahan.Service.UserService;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/postUser")
    public ResponseEntity<UserEntity> postUser(@RequestBody UserEntity user) {
        try {
            UserEntity createdUser = userService.postUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (principal != null) {
                String email = principal.getAttribute("email");
                if (email != null) {
                    UserEntity userEntity = userRepository.findByEmail(email);
                    if (userEntity != null) {
                        response.put("id", userEntity.getUserId());
                        response.put("email", userEntity.getEmail());
                        response.put("firstName", userEntity.getFirstName());
                        response.put("lastName", userEntity.getLastName());
                        return ResponseEntity.ok(response);
                    }
                }
                // Fallback for OAuth2 users not yet in DB
                response.put("email", principal.getAttribute("email"));
                response.put("firstName", principal.getAttribute("given_name"));
                response.put("lastName", principal.getAttribute("family_name"));
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        } catch (Exception e) {
            response.put("error", "Failed to retrieve user information: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/getAllUser")
    public ResponseEntity<List<UserEntity>> getAllUser() {
        try {
            List<UserEntity> users = userService.getAllUser();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/putUser/{id}")
    public ResponseEntity<UserEntity> putUser(@PathVariable int id, @RequestBody UserEntity newUserDetails) {
        try {
            UserEntity updatedUser = userService.putUser(id, newUserDetails);
            if (updatedUser != null) {
                return ResponseEntity.ok(updatedUser);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @DeleteMapping("/deleteUser/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        try {
            String result = userService.deleteUser(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user: " + e.getMessage());
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<UserEntity> signup(@RequestBody UserEntity user) {
        try {
            UserEntity newUser = userService.registerUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        try {
            UserEntity loggedInUser = userService.loginUser(user.getEmail(), user.getPassword());
            if (loggedInUser != null) {
                return ResponseEntity.ok(loggedInUser);
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}