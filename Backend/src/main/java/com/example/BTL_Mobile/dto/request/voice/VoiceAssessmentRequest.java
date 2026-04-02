package com.example.BTL_Mobile.dto.request.voice;

import com.example.BTL_Mobile.model.enums.ELanguage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VoiceAssessmentRequest {

    @NotBlank
    private String referenceText;

    @NotNull
    private MultipartFile audio;

    @NotNull
    private ELanguage language;

}
