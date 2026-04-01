package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.EWordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminFlashCardCreateRequest {

    /**
     * If provided, we reuse an existing dictionary word.
     * If not provided, the request must contain word fields to create one.
     */
    private Integer dictionaryWordId;

    @Size(max = 50)
    private String word;

    @Size(max = 50)
    private String pronunciation;

    @Size(max = 255)
    private String meaning;

    private EWordType type;

    @NotBlank
    @Size(max = 255)
    private String example;

    @NotBlank
    @Size(max = 1000)
    private String imageUrl;

    @Size(max = 255)
    private String imageName;

    private Integer imageSize;
}

