package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InterviewQuestions(
    val questions: List<String>
)