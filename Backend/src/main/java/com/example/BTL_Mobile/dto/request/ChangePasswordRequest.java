package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Current password cannot be empty!")
    private String currentPassword;

    @NotBlank(message = "Your password's length must be >= 8 characters!")
    private String newPassword;

    @NotBlank(message = "Confirm password does not match!")
    private String confirmPassword;
}
