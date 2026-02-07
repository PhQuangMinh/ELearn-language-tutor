package com.example.BTL_Mobile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "speaking_sessions", schema = "btl_mobile")
public class SpeakingSession {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;

    @NotNull
    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @NotNull
    @Column(name = "ended_at", nullable = false)
    private LocalDateTime endedAt;

    @NotNull
    @Column(name = "grammar_score", nullable = false)
    private Double grammarScore;

    @NotNull
    @Column(name = "vocabulary_score", nullable = false)
    private Double vocabularyScore;

    @NotNull
    @Column(name = "pronunciation_score", nullable = false)
    private Double pronunciationScore;

}