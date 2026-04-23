package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.request.ConversationImproveRequest;
import com.example.BTL_Mobile.dto.response.ConversationImproveResponse;

public interface ConversationImproveService {

    ConversationImproveResponse improve(ConversationImproveRequest request);
}
