package com.example.BTL_Mobile.dto.response.streak;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreakExtendedDTO {

    private boolean extended;

    private int currentStreak;

}
