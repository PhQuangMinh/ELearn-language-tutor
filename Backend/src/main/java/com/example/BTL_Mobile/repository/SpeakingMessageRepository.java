package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.SpeakingMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpeakingMessageRepository extends JpaRepository<SpeakingMessage, Integer> {
}
