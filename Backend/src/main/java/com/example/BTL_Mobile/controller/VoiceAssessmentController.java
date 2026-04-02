package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.voice.VoiceAssessmentRequest;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.dto.response.voice.VoiceAssessmentResponse;
import com.example.BTL_Mobile.model.enums.ELanguage;
import com.example.BTL_Mobile.service.VoiceAssessmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/voice")
public class VoiceAssessmentController {

    private final VoiceAssessmentService voiceAssessmentService;

    @PostMapping(value = "/assess", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<VoiceAssessmentResponse>> assess(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("referenceText") String referenceText,
            @RequestParam(value = "language", required = false) ELanguage language
    ) throws IOException, ExecutionException, InterruptedException, TimeoutException {
        VoiceAssessmentRequest request = new VoiceAssessmentRequest(referenceText, audio, language);
        return ResponseEntity.ok(ApiResponse.success(voiceAssessmentService.assess(request)));
    }

}
