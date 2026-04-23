package com.example.BTL_Mobile.controller;

import com.example.BTL_Mobile.dto.request.DeviceTokenRequest;
import com.example.BTL_Mobile.dto.response.ApiResponse;
import com.example.BTL_Mobile.security.CurrentUserContext;
import com.example.BTL_Mobile.service.UserDeviceTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me/device-tokens")
@RequiredArgsConstructor
public class UserDeviceTokenController {

    private final CurrentUserContext currentUserContext;
    private final UserDeviceTokenService userDeviceTokenService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> registerDeviceToken(@Valid @RequestBody DeviceTokenRequest request) {
        Integer userId = currentUserContext.requireUserId();
        userDeviceTokenService.registerToken(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Đăng ký thiết bị nhận thông báo thành công!", null));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> unregisterDeviceToken(@RequestParam("token") String token) {
        Integer userId = currentUserContext.requireUserId();
        userDeviceTokenService.unregisterToken(userId, token);
        return ResponseEntity.ok(ApiResponse.success("Hủy đăng ký thiết bị nhận thông báo thành công!", null));
    }
}
