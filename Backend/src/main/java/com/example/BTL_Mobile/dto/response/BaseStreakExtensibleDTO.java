package com.example.BTL_Mobile.dto.response;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseStreakExtensibleDTO {

    private boolean streakExtended;

    private int currentStreak;

}
