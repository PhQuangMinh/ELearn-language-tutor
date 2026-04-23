package com.nhom2.elearnlanguage.data.dto

data class ApiResponseDTO<T> (
    val success: Boolean,
    val message: String,
    val data: T? = null,
    val errorCode: String? = null
)