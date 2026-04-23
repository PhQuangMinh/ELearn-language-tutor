package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.EWordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminWordCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String word;

    @NotBlank
    @Size(max = 50)
    private String pronunciation;

    @NotBlank
    @Size(max = 255)
    private String meaning;

    @NotNull
    private EWordType type;
}

