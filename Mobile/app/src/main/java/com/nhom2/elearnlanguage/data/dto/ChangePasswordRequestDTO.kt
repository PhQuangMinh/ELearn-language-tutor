package com.nhom2.elearnlanguage.data.dto

data class ChangePasswordRequestDTO(
    val currentPassword: String,
    val newPassword: String,
    val confirmPassword: String
)
