package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScenarioDetailResponse {
    private Integer id;
    private String title;
    private String description;
    private String tasks;
    private String openningMessage;
    private String suggestion;
    private String translation;
}

