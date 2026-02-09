package com.example.BTL_Mobile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "vocabulary_collection_words", schema = "btl_mobile")
public class VocabularyCollectionWord {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vocabulary_collection_id", nullable = false)
    private UserVocabularyCollection vocabularyCollection;

    @NotNull
    @Column(name = "is_practicing", nullable = false)
    private Boolean isPracticing = false;

    @NotNull
    @Column(name = "last_practice_at")
    private LocalDateTime lastPracticeAt;

}