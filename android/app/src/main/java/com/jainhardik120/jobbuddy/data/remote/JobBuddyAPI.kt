package com.jainhardik120.jobbuddy.data.remote

import android.net.Uri
import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.dto.EvaluationResponse
import com.jainhardik120.jobbuddy.data.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.dto.InterviewEvaluationRequest
import com.jainhardik120.jobbuddy.data.dto.InterviewQuestions
import com.jainhardik120.jobbuddy.data.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.dto.MessageError
import com.jainhardik120.jobbuddy.data.dto.MessageResponse
import com.jainhardik120.jobbuddy.data.dto.ProfileDetails
import com.jainhardik120.jobbuddy.data.dto.ResumeScoreRequest
import com.jainhardik120.jobbuddy.data.dto.ResumeScoreResponse
import com.jainhardik120.jobbuddy.data.dto.ResumeStatusResponse
import kotlinx.coroutines.flow.Flow

interface JobBuddyAPI {
    suspend fun loginGoogle(request: GoogleLoginRequest): Result<LoginResponse, MessageError>

    fun uploadResume(contentUri: Uri): Flow<ProgressUpdate>

    suspend fun checkResumeScore(request: ResumeScoreRequest): Result<ResumeScoreResponse, MessageError>

    suspend fun getResumeStatus(): Result<ResumeStatusResponse, MessageError>

    suspend fun getProfileDetails(): Result<ProfileDetails, MessageError>

    suspend fun updateProfileDetails(data: ProfileDetails): Result<MessageResponse, MessageError>

    suspend fun getInterviewQuestions(jobDescription: String): Result<InterviewQuestions, MessageError>

    suspend fun evaluateInterview(request: InterviewEvaluationRequest): Result<EvaluationResponse, MessageError>

}

