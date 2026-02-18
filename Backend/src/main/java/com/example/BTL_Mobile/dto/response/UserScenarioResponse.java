package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserScenarioResponse {
    private Integer id;
    private String myCharacter;
    private String aiCharacter;
    private String aiGender;
    private String situation;
    private Integer likesCount;
    private String createdBy;
}
