package edu.cit.markahan.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.cit.markahan.Entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    // Method to find a user by username
    UserEntity findByEmail(String email);
    UserEntity findByOauthId(String oauthId);
}
