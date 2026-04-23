package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.audit.AbstractAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "speaking_sessions", schema = "btl_mobile")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpeakingSession extends AbstractAuditEntity {

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

    @Column(name = "ended_at")
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

    @OneToMany(mappedBy = "speakingSession")
    private Set<SpeakingMessage> messages;

}