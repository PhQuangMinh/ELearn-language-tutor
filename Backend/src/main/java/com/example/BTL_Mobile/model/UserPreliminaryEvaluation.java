package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.enums.EEvaluationLevel;
import com.example.BTL_Mobile.model.enums.ELanguage;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "user_preliminary_evaluation", schema = "btl_mobile")
public class UserPreliminaryEvaluation {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 50)
    @NotNull
    @Column(name = "language", nullable = false, length = 50)
    private ELanguage language;

    @Size(max = 20)
    @NotNull
    @Column(name = "level", nullable = false, length = 20)
    private EEvaluationLevel level;

    @Size(max = 255)
    @NotNull
    @Column(name = "purpose", nullable = false)
    private String purpose;

}