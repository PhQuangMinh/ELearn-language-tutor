package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateScenarioRequest {

    @NotBlank(message = "Nhân vật của bạn không được để trống")
    @Size(max = 30, message = "Nhân vật của bạn không được vượt quá 30 ký tự")
    private String myCharacter;

    @NotBlank(message = "Nhân vật AI không được để trống")
    @Size(max = 30, message = "Nhân vật AI không được vượt quá 30 ký tự")
    private String aiCharacter;

    @NotBlank(message = "Giới tính AI không được để trống")
    private String aiGender;

    @NotBlank(message = "Tình huống không được để trống")
    @Size(max = 256, message = "Tình huống không được vượt quá 256 ký tự")
    private String situation;
}
