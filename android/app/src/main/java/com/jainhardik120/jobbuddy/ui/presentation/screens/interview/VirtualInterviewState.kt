package com.jainhardik120.jobbuddy.ui.presentation.screens.interview

import com.jainhardik120.jobbuddy.data.dto.QuestionResponse
import com.jainhardik120.jobbuddy.data.local.entity.Job

data class VirtualInterviewState(
    val questions: List<String> = emptyList(),
    val questionAnswerResponses: List<QuestionResponse> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val isRecording: Boolean = false,
    val jobData: Job? = null,
    val currentPartialText: String = "",
    val isGeneratingQuestions: Boolean = false,
    val isEvaluating: Boolean = false,
    val evaluationResult: String = ""
)