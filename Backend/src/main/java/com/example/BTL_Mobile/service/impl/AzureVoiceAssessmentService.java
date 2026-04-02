package com.example.BTL_Mobile.service.impl;

import com.example.BTL_Mobile.config.props.AzureSpeechProperties;
import com.example.BTL_Mobile.dto.request.voice.VoiceAssessmentRequest;
import com.example.BTL_Mobile.dto.response.voice.VoiceAssessmentResponse;
import com.example.BTL_Mobile.service.CloudinaryService;
import com.example.BTL_Mobile.service.VoiceAssessmentService;
import com.microsoft.cognitiveservices.speech.*;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class AzureVoiceAssessmentService implements VoiceAssessmentService {

    private final AzureSpeechProperties props;
    
    private final CloudinaryService cloudinaryService;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Override
    public VoiceAssessmentResponse assess(@Valid VoiceAssessmentRequest request) throws ExecutionException, InterruptedException, TimeoutException, IOException {
        byte[] wavBytes = request.getAudio().getBytes();
        if (wavBytes.length == 0) {
            throw new IllegalArgumentException("Audio payload is empty.");
        }
        String referenceText = request.getReferenceText();
        if (referenceText == null || referenceText.isBlank()) {
            throw new IllegalArgumentException("Missing referenceText (the reference sentence/paragraph for pronunciation assessment).");
        }
        if (props.key() == null || props.key().isBlank() || props.region() == null || props.region().isBlank()) {
            throw new IllegalStateException("Missing Azure Speech configuration (azure.speech.key / azure.speech.region).");
        }
        String language = request.getLanguage() == null ? null : request.getLanguage().getValue();
        String lang = (language == null || language.isBlank()) ? props.defaultLanguage() : language;

        SpeechConfig config = null;
        AudioConfig audioInput = null;
        SpeechRecognizer speechRecognizer = null;
        PronunciationAssessmentConfig pronunciationConfig = null;
        SpeechRecognitionResult speechRecognitionResult = null;
        Path tmpWav = null;
        CompletableFuture<String> audioUploadFuture = null;

        try {
            audioUploadFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return cloudinaryService.uploadAudio(
                            wavBytes,
                            request.getAudio().getOriginalFilename(),
                            "voice_assessment"
                    );
                } catch (Exception ex) {
                    throw new CompletionException(ex);
                }
            });

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
            String audioUrl = awaitAudioUpload(audioUploadFuture);

            String displayTextFallback = speechRecognitionResult.getText();
            VoiceAssessmentResponse.OverallAssessment overallFallback = new VoiceAssessmentResponse.OverallAssessment(
                    paResult == null ? null : paResult.getAccuracyScore(),
                    paResult == null ? null : paResult.getFluencyScore(),
                    paResult == null ? null : paResult.getProsodyScore(),
                    paResult == null ? null : paResult.getCompletenessScore(),
                    paResult == null ? null : paResult.getPronunciationScore()
            );

            if (rawJson != null && !rawJson.isBlank()) {
                ParsedAzure parsed = parseAzureResult(rawJson);
                return new VoiceAssessmentResponse(
                        recognitionStatus,
                        parsed.displayText() != null ? parsed.displayText() : displayTextFallback,
                        audioUrl,
                        parsed.overall() != null ? parsed.overall() : overallFallback,
                        parsed.words()
                );
            }

            return new VoiceAssessmentResponse(
                    recognitionStatus,
                    displayTextFallback,
                    audioUrl,
                    overallFallback,
                    Collections.emptyList()
            );
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to read/write temporary WAV file: " + e.getMessage());
        } finally {
            if (audioUploadFuture != null && !audioUploadFuture.isDone()) {
                audioUploadFuture.cancel(true);
            }
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

            JsonNode wpa = w.path("PronunciationAssessment");
            Double accuracyScore = doubleOrNull(wpa, "AccuracyScore");
            String errorType = textOrNull(wpa.get("ErrorType"));

            out.add(new VoiceAssessmentResponse.WordAssessment(
                    word,
                    accuracyScore,
                    errorType
            ));
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

    private static Double firstNonNull(Double a, Double b) {
        return a != null ? a : b;
    }

    private static String awaitAudioUpload(CompletableFuture<String> audioUploadFuture) {
        if (audioUploadFuture == null) return null;
        try {
            return audioUploadFuture.get(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Audio upload was interrupted.", e);
        } catch (TimeoutException e) {
            throw new IllegalStateException("Audio upload timed out.", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Failed to upload audio to Cloudinary: " + cause.getMessage(), cause);
        }
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
