package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumeScoreResponse(
    @SerialName("analysis")
    val message: String
)
@Serializable
data class InterviewInsightResponse(
    @SerialName("insights")
    val message: String
)