package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLessonResponse {
    private Integer id;
    private Integer topicId;
    private String title;
    private String type;
    private String imageUrl;
    private Integer parentId;
}