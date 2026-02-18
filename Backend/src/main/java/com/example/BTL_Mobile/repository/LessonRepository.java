package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    
    List<Lesson> findByTopicIdOrderByIdAsc(Integer topicId);
    
    @Query("SELECT COUNT(l) FROM Lesson l WHERE l.topic.id = :topicId")
    Long countByTopicId(@Param("topicId") Integer topicId);
}
