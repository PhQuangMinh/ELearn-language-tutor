package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminScenarioCreateRequest {

    @NotNull(message = "Topic không được để trống")
    private Integer topicId;

    @NotNull(message = "Lesson không được để trống")
    private Integer lessonId;

    @NotBlank(message = "Tiêu đề scenario không được để trống")
    @Size(max = 255, message = "Tiêu đề scenario tối đa 255 ký tự")
    private String title;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;

    @NotBlank(message = "AI role không được để trống")
    @Size(max = 50, message = "AI role tối đa 50 ký tự")
    private String aiRole;

    @NotBlank(message = "User role không được để trống")
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