package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.ELessonType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLessonUpdateRequest {

    private Integer topicId;

    @Size(max = 255, message = "Tiêu đề lesson tối đa 255 ký tự")
    private String title;

    private ELessonType type;

    @Size(max = 1000, message = "Image URL tối đa 1000 ký tự")
    private String imageUrl;

    private Integer parentId;
}