package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.Lesson;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.model.UserLessonResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLessonResultRepository extends JpaRepository<UserLessonResult, Integer> {
    
    List<UserLessonResult> findByUserOrderByStartedAtDesc(User user);
    
    @Query("SELECT COUNT(DISTINCT ulr.lesson.id) FROM UserLessonResult ulr WHERE ulr.user.id = :userId AND ulr.lesson.topic.id = :topicId")
    Long countCompletedLessonsByUserAndTopic(@Param("userId") Integer userId, @Param("topicId") Integer topicId);
    
    Optional<UserLessonResult> findByUserAndLesson(User user, Lesson lesson);
    
    @Query("SELECT DISTINCT ulr.lesson FROM UserLessonResult ulr WHERE ulr.user.id = :userId ORDER BY ulr.startedAt DESC")
    List<Lesson> findRecentLessonsByUser(@Param("userId") Integer userId);
    @Query("""
            select distinct ulr.lesson.id
            from UserLessonResult ulr
            where ulr.user.id = :userId
              and ulr.lesson.topic.id = :topicId
              and ulr.isCompleted = true
            """)
    List<Integer> findCompletedLessonIdsInTopic(
            @Param("userId") Integer userId,
            @Param("topicId") Integer topicId
    );
}

