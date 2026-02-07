package com.example.BTL_Mobile.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "user_speaking_duration", schema = "btl_mobile")
public class UserSpeakingDuration {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "date", nullable = false)
    private LocalDate date;

    @NotNull
    @Column(name = "speaking_count", nullable = false)
    private Integer speakingCount;

    @NotNull
    @Column(name = "valid_speaking_count", nullable = false)
    private Integer validSpeakingCount;

    @Column(name = "valid_speaking_duration")
    private Integer validSpeakingDuration;

    @Column(name = "speaking_duration")
    private Integer speakingDuration;

}