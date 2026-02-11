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
public class ResetPasswordWithTokenRequest {

    @NotBlank(message = "Email cannot be empty!")
    @Email(message = "Email's format is incorrect!")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email's format is incorrect!")
    private String email;

    @NotBlank(message = "Your verification code is incorrect.")
    @Size(min = 10, message = "Your verification code is incorrect.")
    private String resetToken;

    @NotBlank(message = "Your password's length must be >= 8 characters!")
    @Size(min = 8, message = "Your password's length must be >= 8 characters!")
    private String newPassword;

    @NotBlank(message = "Confirm password does not match!")
    private String confirmPassword;
}

