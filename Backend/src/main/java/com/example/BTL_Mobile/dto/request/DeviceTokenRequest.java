package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {

    @NotBlank(message = "token is required")
    private String token;

    private String deviceId;

    private String appVersion;

    private String platform;
}
