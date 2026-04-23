package com.example.BTL_Mobile.dto.request;

import com.example.BTL_Mobile.model.enums.ERole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserCreateRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 4, max = 50, message = "Username phải từ 4 đến 50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Pattern(regexp = "^[A-Za-z0-9._%+-]+@gmail\\.com$", message = "Email phải là @gmail.com")
    @Size(max = 100, message = "Email tối đa 100 ký tự")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, max = 100, message = "Password phải từ 8 đến 100 ký tự")
    private String password;

    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    private ERole role;

    private Boolean enabled;
}
