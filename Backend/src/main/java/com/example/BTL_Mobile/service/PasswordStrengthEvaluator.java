package com.example.BTL_Mobile.service;

import com.example.BTL_Mobile.dto.response.PasswordStrengthResponse;

public final class PasswordStrengthEvaluator {

    private PasswordStrengthEvaluator() {
    }

    public static PasswordStrengthResponse evaluate(String password) {
        String p = password == null ? "" : password.trim();

        if (p.length() < 8) {
            return PasswordStrengthResponse.builder()
                    .pass(false)
                    .level("TOO_SHORT")
                    .message("Your password's length must be >= 8 characters!")
                    .color("red")
                    .build();
        }

        boolean hasLetter = p.chars().anyMatch(Character::isLetter);
        boolean hasDigit = p.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = p.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        if (!hasLetter || !hasDigit) {
            return PasswordStrengthResponse.builder()
                    .pass(false)
                    .level("LOW")
                    .message("How strong your password: Low (Only numbers or characters).")
                    .color("red")
                    .build();
        }

        if (!hasSpecial) {
            return PasswordStrengthResponse.builder()
                    .pass(true)
                    .level("MEDIUM")
                    .message("How strong your password: Medium.")
                    .color("orange")
                    .build();
        }

        if (p.length() >= 12) {
            return PasswordStrengthResponse.builder()
                    .pass(true)
                    .level("SUPER_STRONG")
                    .message("How strong your password: Super Strong.")
                    .color("green")
                    .build();
        }

        return PasswordStrengthResponse.builder()
                .pass(true)
                .level("STRONG")
                .message("How strong your password: Strong.")
                .color("green")
                .build();
    }
}
