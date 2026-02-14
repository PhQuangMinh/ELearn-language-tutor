package com.nhom2.elearnlanguage.domain.model

data class User(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String?,
    val role: String
)
