package com.tbdd.demo_voice_ai.dto;

import java.util.List;

public record VoiceAssessmentResponse(
        String recognitionStatus,
        String displayText,
        OverallAssessment overall,
        List<WordAssessment> words,
        String rawJson
) {
    public record OverallAssessment(
            Double accuracyScore,
            Double fluencyScore,
            Double prosodyScore,
            Double completenessScore,
            Double pronScore
    ) {}

    public record WordAssessment(
            String word,
            Long offset,
            Long duration,
            Double accuracyScore,
            String errorType,
            List<SyllableAssessment> syllables,
            List<PhonemeAssessment> phonemes
    ) {}

    public record SyllableAssessment(
            String syllable,
            Long offset,
            Long duration,
            Double accuracyScore
    ) {}

    public record PhonemeAssessment(
            String phoneme,
            Long offset,
            Long duration,
            Double accuracyScore
    ) {}
}

