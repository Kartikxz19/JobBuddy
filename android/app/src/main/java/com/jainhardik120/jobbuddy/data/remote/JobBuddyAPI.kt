package com.jainhardik120.jobbuddy.data.remote

import android.net.Uri
import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.remote.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.remote.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.remote.dto.MessageError
import kotlinx.coroutines.flow.Flow

interface JobBuddyAPI {
    suspend fun loginGoogle(request: GoogleLoginRequest): Result<LoginResponse, MessageError>

    fun uploadResume(contentUri: Uri): Flow<ProgressUpdate>

    suspend fun checkResumeScore(request: ResumeScoreRequest): Result<ResumeScoreResponse, MessageError>

    suspend fun getResumeStatus(): Result<ResumeStatusResponse, MessageError>
}

data class ProgressUpdate(
    val bytesSent: Long,
    val totalBytes: Long
)