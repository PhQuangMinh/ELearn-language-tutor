package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.enums.EStreakStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_streak", schema = "btl_mobile")
public class UserStreak {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak;

    @Column(name = "last_streak_updated")
    private LocalDateTime lastStreakUpdated;

    @NotNull
    @Column(name = "longest_streak", nullable = false)
    private Integer longestStreak;

    @Size(max = 50)
    @NotNull
    @Column(name = "status", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private EStreakStatus status;

}