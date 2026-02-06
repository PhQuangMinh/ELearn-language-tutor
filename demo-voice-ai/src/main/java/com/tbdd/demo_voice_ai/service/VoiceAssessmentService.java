package com.tbdd.demo_voice_ai.service;

import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.tbdd.demo_voice_ai.config.AzureSpeechProperties;
import com.tbdd.demo_voice_ai.dto.VoiceAssessmentResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

@Service
public class VoiceAssessmentService {

    private final AzureSpeechProperties props;
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    public VoiceAssessmentService(AzureSpeechProperties props) {
        this.props = props;
    }

    public VoiceAssessmentResponse assessPronunciationWav(byte[] wavBytes, String referenceText, String language)
            throws ExecutionException, InterruptedException, TimeoutException {

        if (wavBytes == null || wavBytes.length == 0) {
            throw new IllegalArgumentException("Audio rỗng.");
        }
        if (referenceText == null || referenceText.isBlank()) {
            throw new IllegalArgumentException("Thiếu referenceText (câu/đoạn chuẩn để chấm phát âm).");
        }
        if (props.key() == null || props.key().isBlank() || props.region() == null || props.region().isBlank()) {
            throw new IllegalStateException("Thiếu cấu hình Azure Speech (azure.speech.key / azure.speech.region).");
        }

        String lang = (language == null || language.isBlank()) ? props.defaultLanguage() : language;

        SpeechConfig config = null;
        AudioConfig audioInput = null;
        SpeechRecognizer speechRecognizer = null;
        PronunciationAssessmentConfig pronunciationConfig = null;
        SpeechRecognitionResult speechRecognitionResult = null;
        Path tmpWav = null;

        try {
            config = SpeechConfig.fromSubscription(props.key(), props.region());
            config.setSpeechRecognitionLanguage(lang);

            tmpWav = Files.createTempFile("voice-assess-", ".wav");
            Files.write(tmpWav, wavBytes);

            audioInput = AudioConfig.fromWavFileInput(tmpWav.toString());
            speechRecognizer = new SpeechRecognizer(config, audioInput);

            pronunciationConfig = new PronunciationAssessmentConfig(
                    referenceText,
                    PronunciationAssessmentGradingSystem.HundredMark,
                    PronunciationAssessmentGranularity.Word,
                    false
            );
            pronunciationConfig.enableProsodyAssessment();
            pronunciationConfig.applyTo(speechRecognizer);

            Future<SpeechRecognitionResult> future = speechRecognizer.recognizeOnceAsync();
            speechRecognitionResult = future.get(30, TimeUnit.SECONDS);

            PronunciationAssessmentResult paResult = PronunciationAssessmentResult.fromResult(speechRecognitionResult);

            String rawJson = speechRecognitionResult
                    .getProperties()
                    .getProperty(PropertyId.SpeechServiceResponse_JsonResult);

            String recognitionStatus = speechRecognitionResult.getReason().toString();

            String displayTextFallback = speechRecognitionResult.getText();
            VoiceAssessmentResponse.OverallAssessment overallFallback = new VoiceAssessmentResponse.OverallAssessment(
                    paResult == null ? null : (double) paResult.getAccuracyScore(),
                    paResult == null ? null : (double) paResult.getFluencyScore(),
                    paResult == null ? null : (double) paResult.getProsodyScore(),
                    paResult == null ? null : (double) paResult.getCompletenessScore(),
                    paResult == null ? null : (double) paResult.getPronunciationScore()
            );

            if (rawJson != null && !rawJson.isBlank()) {
                ParsedAzure parsed = parseAzureResult(rawJson);
                return new VoiceAssessmentResponse(
                        recognitionStatus,
                        parsed.displayText() != null ? parsed.displayText() : displayTextFallback,
                        parsed.overall() != null ? parsed.overall() : overallFallback,
                        parsed.words(),
                        rawJson
                );
            }

            return new VoiceAssessmentResponse(
                    recognitionStatus,
                    displayTextFallback,
                    overallFallback,
                    Collections.emptyList(),
                    rawJson
            );
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Lỗi đọc/ghi file WAV tạm: " + e.getMessage());
        } finally {
            safeClose(speechRecognitionResult);
            safeClose(pronunciationConfig);
            safeClose(speechRecognizer);
            safeClose(audioInput);
            safeClose(config);
            if (tmpWav != null) {
                try {
                    Files.deleteIfExists(tmpWav);
                } catch (Exception ignored) {
                    // best-effort
                }
            }
        }
    }

    private ParsedAzure parseAzureResult(String rawJson) throws java.io.IOException {
        JsonNode root = jsonMapper.readTree(rawJson);

        String displayText = textOrNull(root.get("DisplayText"));

        JsonNode nbest0 = root.path("NBest").path(0);
        JsonNode pa = nbest0.path("PronunciationAssessment");

        VoiceAssessmentResponse.OverallAssessment overall = null;
        if (!pa.isMissingNode() && !pa.isNull()) {
            overall = new VoiceAssessmentResponse.OverallAssessment(
                    doubleOrNull(pa, "AccuracyScore"),
                    doubleOrNull(pa, "FluencyScore"),
                    doubleOrNull(pa, "ProsodyScore"),
                    doubleOrNull(pa, "CompletenessScore"),
                    // Azure trả "PronScore" (như sample) hoặc đôi khi "PronunciationScore"
                    firstNonNull(
                            doubleOrNull(pa, "PronScore"),
                            doubleOrNull(pa, "PronunciationScore")
                    )
            );
        }

        List<VoiceAssessmentResponse.WordAssessment> words = parseWords(nbest0.path("Words"));

        return new ParsedAzure(displayText, overall, words);
    }

    private List<VoiceAssessmentResponse.WordAssessment> parseWords(JsonNode wordsNode) {
        if (wordsNode == null || !wordsNode.isArray()) return Collections.emptyList();

        List<VoiceAssessmentResponse.WordAssessment> out = new ArrayList<>();
        for (JsonNode w : wordsNode) {
            String word = textOrNull(w.get("Word"));
            Long offset = longOrNull(w.get("Offset"));
            Long duration = longOrNull(w.get("Duration"));

            JsonNode wpa = w.path("PronunciationAssessment");
            Double accuracyScore = doubleOrNull(wpa, "AccuracyScore");
            String errorType = textOrNull(wpa.get("ErrorType"));

            List<VoiceAssessmentResponse.SyllableAssessment> syllables = parseSyllables(w.get("Syllables"));
            List<VoiceAssessmentResponse.PhonemeAssessment> phonemes = parsePhonemes(w.get("Phonemes"));

            out.add(new VoiceAssessmentResponse.WordAssessment(
                    word,
                    offset,
                    duration,
                    accuracyScore,
                    errorType,
                    syllables,
                    phonemes
            ));
        }
        return out;
    }

    private List<VoiceAssessmentResponse.SyllableAssessment> parseSyllables(JsonNode node) {
        if (node == null || !node.isArray()) return Collections.emptyList();
        List<VoiceAssessmentResponse.SyllableAssessment> out = new ArrayList<>();
        for (JsonNode s : node) {
            String syllable = textOrNull(s.get("Syllable"));
            Long offset = longOrNull(s.get("Offset"));
            Long duration = longOrNull(s.get("Duration"));
            Double accuracy = doubleOrNull(s.path("PronunciationAssessment"), "AccuracyScore");
            out.add(new VoiceAssessmentResponse.SyllableAssessment(syllable, offset, duration, accuracy));
        }
        return out;
    }

    private List<VoiceAssessmentResponse.PhonemeAssessment> parsePhonemes(JsonNode node) {
        if (node == null || !node.isArray()) return Collections.emptyList();
        List<VoiceAssessmentResponse.PhonemeAssessment> out = new ArrayList<>();
        for (JsonNode p : node) {
            String phoneme = textOrNull(p.get("Phoneme"));
            Long offset = longOrNull(p.get("Offset"));
            Long duration = longOrNull(p.get("Duration"));
            Double accuracy = doubleOrNull(p.path("PronunciationAssessment"), "AccuracyScore");
            out.add(new VoiceAssessmentResponse.PhonemeAssessment(phoneme, offset, duration, accuracy));
        }
        return out;
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) return null;
        // tools.jackson/jackson 3: asText()/isTextual()/textValue() bị deprecated
        String v = node.isString() ? node.asString() : node.toString();
        return (v == null || v.isBlank()) ? null : v;
    }

    private static Double doubleOrNull(JsonNode obj, String field) {
        if (obj == null || obj.isMissingNode() || obj.isNull()) return null;
        JsonNode node = obj.get(field);
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) return null;
        return node.doubleValue();
    }

    private static Long longOrNull(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull() || !node.isNumber()) return null;
        return node.longValue();
    }

    private static Double firstNonNull(Double a, Double b) {
        return a != null ? a : b;
    }

    private record ParsedAzure(
            String displayText,
            VoiceAssessmentResponse.OverallAssessment overall,
            List<VoiceAssessmentResponse.WordAssessment> words
    ) {}

    private static void safeClose(AutoCloseable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Exception ignored) {
        }
    }

}
