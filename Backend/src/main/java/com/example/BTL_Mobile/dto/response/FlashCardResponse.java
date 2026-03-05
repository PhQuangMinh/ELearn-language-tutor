package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashCardResponse {

    private Integer id;

    private String word;

    private String pronunciation;

    private String meaning;

    private String example;

    private String imageUrl;

}
