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
public class RegisterInitiateRequest {

    @NotBlank(message = "Your name cannot be empty!")
    @Size(min = 4, max = 100, message = "Your name's length must be >= 4 characters.")
    private String fullName;

    @NotBlank(message = "Your email cannot be empty!")
    @Email(message = "Your email's format is incorrect!")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Your email's format is incorrect!")
    private String email;
}

