package com.example.BTL_Mobile.dto.response;

import com.example.BTL_Mobile.model.enums.EWordType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WordDTO {

    private String word;

    private String pronunciation;

    private String meaning;

    private EWordType type;

}
