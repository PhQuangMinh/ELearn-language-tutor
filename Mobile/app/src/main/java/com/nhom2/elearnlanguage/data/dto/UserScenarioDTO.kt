package com.nhom2.elearnlanguage.data.dto

data class UserScenarioDTO(
    val id: Int = 0,
    val myCharacter: String = "",
    val aiCharacter: String = "",
    val aiGender: String = "",
    val situation: String = "",
    val likesCount: Int = 0,
    val createdBy: String = ""
)
