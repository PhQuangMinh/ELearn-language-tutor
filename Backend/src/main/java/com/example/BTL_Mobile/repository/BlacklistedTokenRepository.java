package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Integer> {
    
    Optional<BlacklistedToken> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM BlacklistedToken bt WHERE bt.expiresAt < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
