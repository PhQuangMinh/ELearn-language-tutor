package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.ELessonType;
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
public class AdminLessonCreateRequest {

    @NotNull(message = "Topic không được để trống")
    private Integer topicId;

    @NotBlank(message = "Tiêu đề lesson không được để trống")
    @Size(max = 255, message = "Tiêu đề lesson tối đa 255 ký tự")
    private String title;

    @NotNull(message = "Loại lesson không được để trống")
    private ELessonType type;

    @Size(max = 1000, message = "Image URL tối đa 1000 ký tự")
    private String imageUrl;

    private Integer parentId;
}