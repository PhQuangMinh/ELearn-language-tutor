package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.FlashCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Integer> {
}

