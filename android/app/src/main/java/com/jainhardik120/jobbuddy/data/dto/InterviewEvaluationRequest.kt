package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InterviewEvaluationRequest(
    @SerialName("job_description")
    val jobDescription: String,
    @SerialName("questions_responses")
    val questionsResponses : List<QuestionResponse>
)