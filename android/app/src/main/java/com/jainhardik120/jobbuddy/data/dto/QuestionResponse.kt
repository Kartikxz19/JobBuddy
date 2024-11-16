package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class QuestionResponse(
    val question : String,
    val answer : String
)