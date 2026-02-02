package com.tbdd.demo_voice_ai.dto;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path
) {}

