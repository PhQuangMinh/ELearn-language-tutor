package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.dto.response.streak.StreakExtendedDTO;
import com.example.BTL_Mobile.dto.response.streak.UserStreakInfoDTO;
import com.example.BTL_Mobile.exception.BusinessException;
import com.example.BTL_Mobile.mapper.UserStreakMapper;
import com.example.BTL_Mobile.model.User;
import com.example.BTL_Mobile.model.UserStreak;
import com.example.BTL_Mobile.repository.UserRepository;
import com.example.BTL_Mobile.repository.UserStreakRepository;
import com.example.BTL_Mobile.service.UserStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserStreakServiceImpl implements UserStreakService {

    private final UserRepository userRepository;

    private final UserStreakRepository userStreakRepository;

    private final UserStreakMapper userStreakMapper;

    @Override
    public UserStreakInfoDTO getUserStreakInfo(int userId) {
        UserStreak userStreak = getRawUserStreak(userId);
        return userStreakMapper.toInfoDTO(userStreak);
    }

    @Override
    public StreakExtendedDTO extendStreak(int userId) {
        UserStreak userStreak = getRawUserStreak(userId);
        LocalDateTime lastUpdated = userStreak.getLastStreakUpdated();
        boolean extensible = lastUpdated == null || !lastUpdated.toLocalDate().equals(LocalDate.now());
        if(extensible){
            userStreak.setLastStreakUpdated(LocalDateTime.now());
            userStreak.setCurrentStreak(userStreak.getCurrentStreak() + 1);
            userStreak.setLongestStreak(Math.max(userStreak.getLongestStreak(), userStreak.getCurrentStreak()));
            userStreakRepository.save(userStreak);
        }
        return StreakExtendedDTO.builder()
                .extended(extensible)
                .currentStreak(userStreak.getCurrentStreak())
                .build();
    }

    private UserStreak getRawUserStreak(int userId){
        Optional<UserStreak> optUserStreak = userStreakRepository.findByUserId(userId);
        UserStreak userStreak;
        if(optUserStreak.isPresent()) userStreak = optUserStreak.get();
        else {
            boolean userExist = userRepository.existsById(userId);
            if(!userExist) throw new BusinessException("User not exist");
            userStreak = UserStreak.builder()
                    .user(User.builder().id(userId).build())
                    .currentStreak(0)
                    .longestStreak(0)
                    .build();
            userStreakRepository.save(userStreak);
        }
        return userStreak;
    }

}
