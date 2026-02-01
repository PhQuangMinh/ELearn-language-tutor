package com.example.BTL_Mobile.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2LoginRequest {
    
    @NotBlank(message = "Provider không được để trống")
    private String provider;  // google hoặc facebook
    
    @NotBlank(message = "Access token không được để trống")
    private String accessToken;  // Token từ Google/Facebook SDK
}
