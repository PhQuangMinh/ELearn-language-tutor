package com.example.BTL_Mobile.repository;

import com.example.BTL_Mobile.model.UserDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserDeviceTokenRepository extends JpaRepository<UserDeviceToken, Integer> {

    Optional<UserDeviceToken> findByFcmToken(String fcmToken);

    Optional<UserDeviceToken> findByFcmTokenAndUserId(String fcmToken, Integer userId);

    List<UserDeviceToken> findByUserIdAndActiveTrue(Integer userId);

    @Query("""
            select udt from UserDeviceToken udt
            where udt.active = true
              and not exists (
                select 1 from UserStreak us
                where us.user.id = udt.user.id
                  and us.lastStreakUpdated >= :startOfDay
              )
            """)
    List<UserDeviceToken> findReminderTargets(@Param("startOfDay") LocalDateTime startOfDay);
}
