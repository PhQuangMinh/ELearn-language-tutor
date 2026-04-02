package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.voice.VoiceAssessmentRequest;
import com.example.BTL_Mobile.dto.response.voice.VoiceAssessmentResponse;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public interface VoiceAssessmentService {

    VoiceAssessmentResponse assess(VoiceAssessmentRequest request) throws ExecutionException, InterruptedException, TimeoutException, IOException;

}
