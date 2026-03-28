package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.streak.StreakExtendedDTO;
import com.example.BTL_Mobile.dto.response.streak.UserStreakInfoDTO;

public interface UserStreakService {

    UserStreakInfoDTO getUserStreakInfo(int userId);

    StreakExtendedDTO extendStreak(int userId);

}
