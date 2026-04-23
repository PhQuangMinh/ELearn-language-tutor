package com.example.BTL_Mobile.dto.response;

import com.example.BTL_Mobile.model.enums.EWordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminWordResponse {
    private Integer id;
    private String word;
    private String pronunciation;
    private String meaning;
    private EWordType type;
}

