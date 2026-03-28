package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.response.streak.UserStreakInfoDTO;
import com.example.BTL_Mobile.service.UserStreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/streak")
public class UserStreakController {

    private final UserStreakService userStreakService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<UserStreakInfoDTO> getStreakInfoOfUser(@PathVariable int userId){
        return ResponseEntity.ok(userStreakService.getUserStreakInfo(userId));
    }

}
