package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.AiRespondRequest;
import com.example.BTL_Mobile.dto.response.AiRespondResponse;

public interface AiResponseService {

    AiRespondResponse respond(AiRespondRequest request);
}

