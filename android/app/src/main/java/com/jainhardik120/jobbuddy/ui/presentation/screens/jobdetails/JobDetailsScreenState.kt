package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import com.jainhardik120.jobbuddy.data.local.entity.Job
import com.jainhardik120.jobbuddy.data.local.entity.StudyPlan

data class JobDetailsScreenState(
    val studyPlan: List<StudyPlan> = emptyList(),
    val jobData: Job? = null,
    val profileEvaluation: String = "",
    val openEvaluationSheet: Boolean = false
)