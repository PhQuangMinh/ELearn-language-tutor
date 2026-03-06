package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.FlashCard;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashCardRepository extends JpaRepository<FlashCard, Integer> {
	@Query(value = """
			SELECT f.*
			FROM flash_cards f
			JOIN topic_vocabulary tv ON tv.word_id = f.dictionary_word_id
			WHERE tv.topic_id = :topicId
			""", nativeQuery = true)
	List<FlashCard> findByTopicId(@Param("topicId") Integer topicId);
}

