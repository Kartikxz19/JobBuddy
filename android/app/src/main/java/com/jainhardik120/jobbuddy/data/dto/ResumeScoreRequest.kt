package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumeScoreRequest(
    @SerialName("job_description")
    val jobDescription: String
)