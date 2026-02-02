package com.tbdd.demo_voice_ai.controller;

import com.tbdd.demo_voice_ai.dto.VoiceAssessmentResponse;
import com.tbdd.demo_voice_ai.service.VoiceAssessmentService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequestMapping("/api/voice")
public class VoiceAssessmentController {

    private final VoiceAssessmentService service;

    public VoiceAssessmentController(VoiceAssessmentService service) {
        this.service = service;
    }

    /**
     * FE gửi audio dạng multipart (khuyến nghị WAV).
     *
     * curl -X POST http://localhost:8080/api/voice/assess ^
     *   -F "audio=@sample.wav" ^
     *   -F "referenceText=こんにちわ" ^
     *   -F "language=ja-JP"
     */
    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VoiceAssessmentResponse> assess(
            @RequestPart("audio") MultipartFile audio,
            @RequestParam("referenceText") String referenceText,
            @RequestParam(value = "language", required = false) String language
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException {

        if (audio == null || audio.isEmpty()) {
            throw new IllegalArgumentException("Thiếu file audio (field name: audio).");
        }

        byte[] bytes = audio.getBytes();
        VoiceAssessmentResponse resp = service.assessPronunciationWav(bytes, referenceText, language);
        return ResponseEntity.ok(resp);
    }
}

