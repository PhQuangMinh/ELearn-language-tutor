package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScenarioRepository extends JpaRepository<Scenario, Integer> {

    Optional<Scenario> findByLessonId(Integer lessonId);
}

