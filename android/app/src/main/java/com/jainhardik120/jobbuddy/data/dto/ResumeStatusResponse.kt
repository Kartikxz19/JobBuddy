package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResumeStatusResponse(
    val message: String,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("upload_time")
    val uploadTime: String? = null
)