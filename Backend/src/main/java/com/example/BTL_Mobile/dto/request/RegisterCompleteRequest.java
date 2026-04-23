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
public class RegisterCompleteRequest {

    @NotBlank(message = "Your email cannot be empty!")
    @Email(message = "Your email's format is incorrect!")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Your email's format is incorrect!")
    private String email;

    @NotBlank(message = "Registration token is required")
    private String registerToken;

    @NotBlank(message = "Your password's length must be >= 8 characters!")
    @Size(min = 8, message = "Your password's length must be >= 8 characters!")
    private String password;
}

