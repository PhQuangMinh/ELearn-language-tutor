package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordStrengthRequest {

    @NotBlank(message = "Password cannot be empty!")
    private String password;
}
