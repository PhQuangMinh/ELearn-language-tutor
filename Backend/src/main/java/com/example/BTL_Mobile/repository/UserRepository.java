package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
