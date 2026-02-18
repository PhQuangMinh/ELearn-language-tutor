package com.nhom2.elearnlanguage.domain.model

data class UserScenario(
    val id: Int,
    val myCharacter: String,
    val aiCharacter: String,
    val aiGender: String,
    val situation: String,
    val likesCount: Int = 0,
    val createdBy: String = ""
)
