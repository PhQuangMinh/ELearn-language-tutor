package com.example.BTL_Mobile.dto.response.voice;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoiceAssessmentResponse {

    private String recognitionStatus;

    private String displayText;

    private String audioUrl;

    private OverallAssessment overall;

    private List<WordAssessment> words;

    public record OverallAssessment(
            Double accuracyScore,
            Double fluencyScore,
            Double prosodyScore,
            Double completenessScore,
            Double pronScore
    ) {}

    public record WordAssessment(
            String word,
            Double accuracyScore,
            String errorType
    ) {}

}
