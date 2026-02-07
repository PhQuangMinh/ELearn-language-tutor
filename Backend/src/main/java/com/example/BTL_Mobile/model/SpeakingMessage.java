package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.enums.EMessageSender;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "speaking_messages", schema = "btl_mobile")
public class SpeakingMessage {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "speaking_session_id", nullable = false)
    private SpeakingSession speakingSession;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Size(max = 50)
    @NotNull
    @Column(name = "sender", nullable = false, length = 50)
    private EMessageSender sender;

    @Size(max = 255)
    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @NotNull
    @Column(name = "duration", nullable = false)
    private Integer duration;

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