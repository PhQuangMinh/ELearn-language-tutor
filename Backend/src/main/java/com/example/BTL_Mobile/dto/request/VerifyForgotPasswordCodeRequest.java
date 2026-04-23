package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyForgotPasswordCodeRequest {

    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "Email's format is incorrect!")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email's format is incorrect!")
    private String email;

    @NotBlank(message = "Your verification code is incorrect.")
    @Pattern(regexp = "^[0-9]{6}$", message = "Your verification code is incorrect.")
    @Size(min = 6, max = 6, message = "Your verification code is incorrect.")
    private String code;
}

