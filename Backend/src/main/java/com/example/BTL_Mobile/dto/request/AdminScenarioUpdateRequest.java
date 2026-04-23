package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminScenarioUpdateRequest {

    private Integer topicId;

    private Integer lessonId;

    @Size(max = 255, message = "Tiêu đề scenario tối đa 255 ký tự")
    private String title;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @Size(max = 50, message = "AI role tối đa 50 ký tự")
    private String aiRole;

    @Size(max = 50, message = "User role tối đa 50 ký tự")
    private String userRole;

    @Size(max = 500, message = "Tasks tối đa 500 ký tự")
    private String tasks;

    @Size(max = 500, message = "Openning message tối đa 500 ký tự")
    private String openningMessage;

    @Size(max = 500, message = "Suggestion tối đa 500 ký tự")
    private String suggestion;

    @Size(max = 500, message = "Translation tối đa 500 ký tự")
    private String translation;
}