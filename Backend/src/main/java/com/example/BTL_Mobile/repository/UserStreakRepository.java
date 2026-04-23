package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.UserStreak;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Integer> {
    Optional<UserStreak> findByUserId(Integer userId);
}
