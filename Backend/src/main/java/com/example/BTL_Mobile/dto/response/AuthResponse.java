package com.example.BTL_Mobile.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;
    private String refreshToken;
    @Default
    private String type = "Bearer";
    private Integer id;
    private String username;
    private String email;
    private String fullName;
    private String role;

    public AuthResponse(String token, Integer id, String username, String email, String fullName, String role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }

    public AuthResponse(String token, String refreshToken, Integer id, String username, String email, String fullName, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}
