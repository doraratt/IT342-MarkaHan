package edu.cit.markahan.Service;

import java.util.List;
import java.util.NoSuchElementException;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
            throw new NameNotFoundException("User " + userId + " not found!");
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
}
