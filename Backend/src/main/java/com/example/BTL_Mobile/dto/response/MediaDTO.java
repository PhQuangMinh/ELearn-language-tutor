package com.example.BTL_Mobile.dto.response;

import com.example.BTL_Mobile.model.enums.EMediaType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MediaDTO {

    private int id;

    private String url;

    private EMediaType type;

    private int size;

    private String name;

}
