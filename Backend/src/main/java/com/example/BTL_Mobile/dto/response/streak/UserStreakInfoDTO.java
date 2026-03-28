package com.example.BTL_Mobile.dto.response.streak;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStreakInfoDTO {

    private int id;

    private int currentStreak;

    private int longestStreak;

    private LocalDateTime lastStreakUpdated;

}
