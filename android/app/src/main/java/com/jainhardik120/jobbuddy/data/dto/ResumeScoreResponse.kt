package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumeScoreResponse(
    @SerialName("answer")
    val message: String,
    @SerialName("extracted_resume")
    val resumeText: String,
    @SerialName("job_posting")
    val jobDescription: String
)