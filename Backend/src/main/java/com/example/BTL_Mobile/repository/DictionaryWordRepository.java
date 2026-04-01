package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.DictionaryWord;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DictionaryWordRepository extends JpaRepository<DictionaryWord, Integer> {
    Optional<DictionaryWord> findByWordIgnoreCase(String word);
}

