package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.EWordType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminFlashCardUpdateRequest {

    private Integer dictionaryWordId;

    @Size(max = 50)
    private String word;

    @Size(max = 50)
    private String pronunciation;

    @Size(max = 255)
    private String meaning;

    private EWordType type;

    @Size(max = 255)
    private String example;

    @Size(max = 1000)
    private String imageUrl;

    @Size(max = 255)
    private String imageName;

    private Integer imageSize;
}
