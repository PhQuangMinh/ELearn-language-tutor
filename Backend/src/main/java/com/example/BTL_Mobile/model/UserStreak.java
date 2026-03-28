package com.example.BTL_Mobile.model;

import com.example.BTL_Mobile.model.audit.AbstractAuditEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "user_streak", schema = "btl_mobile")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreak extends AbstractAuditEntity {

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

}