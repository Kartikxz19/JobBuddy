package com.jainhardik120.jobbuddy.ui.presentation

data class UploadState(
    val isUploading: Boolean = false,
    val isUploadComplete: Boolean = false,
    val progress: Float = 0f,
    val errorMessage: String? = null,
    val markdownText: String? = null
)