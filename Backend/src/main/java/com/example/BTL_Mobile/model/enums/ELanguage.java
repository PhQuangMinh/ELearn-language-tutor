package com.example.BTL_Mobile.model.enums;

import lombok.Getter;

@Getter
public enum ELanguage {
    US_ENGLISH("en-US"),
    JAPANESE("ja-JP");

    private final String value;

    ELanguage(String value) {
        this.value = value;
    }

}
