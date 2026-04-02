package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.enums.EQuestionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "questions", schema = "btl_mobile")
public class Question {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Size(max = 255)
    @NotNull
    @Column(name = "content", nullable = false)
    private String content;

    @Size(max = 50)
    @NotNull
    @Column(name = "type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EQuestionType type;

    @OneToMany(mappedBy = "question")
    private Set<Answer> answers;

    @ManyToOne
    @JoinColumn(name = "media_id")
    private Media media;

    @Column(name = "repeatable")
    private boolean repeatable;

}