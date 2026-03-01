package com.example.BTL_Mobile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminTopicCreateRequest {

    @NotBlank(message = "Tên topic không được để trống")
    @Size(max = 255, message = "Tên tối đa 255 ký tự")
    private String name;

    @NotBlank(message = "Mô tả không được để trống")
    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String description;
}
