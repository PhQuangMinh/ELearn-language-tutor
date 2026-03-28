package com.example.BTL_Mobile.mapper;

import com.example.BTL_Mobile.dto.response.streak.UserStreakInfoDTO;
import com.example.BTL_Mobile.model.UserStreak;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public abstract class UserStreakMapper {

    public abstract UserStreakInfoDTO toInfoDTO(UserStreak userStreak);

}
