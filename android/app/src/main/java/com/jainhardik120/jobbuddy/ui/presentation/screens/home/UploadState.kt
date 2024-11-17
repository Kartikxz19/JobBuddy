package com.jainhardik120.jobbuddy.ui.presentation.screens.home

import com.jainhardik120.jobbuddy.data.local.entity.Job

data class UploadState(
    val jobList: List<Job> = emptyList(),
    val isCreatingJobData: Boolean = false,
    val jobSheetOpened: Boolean = false
)