package com.jainhardik120.jobbuddy.data.remote

import android.net.Uri
import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.remote.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.remote.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.remote.dto.MessageError
import com.jainhardik120.jobbuddy.data.remote.dto.MessageResponse
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Achievement
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ContactDetail
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Education
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Experience
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ProfileLink
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Project
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Skill
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

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

@Serializable
data class EvaluationResponse(
    val evaluation: String
)

data class ProgressUpdate(
    val bytesSent: Long,
    val totalBytes: Long
)

@Serializable
data class ProfileDetails(
    val achievements: List<Achievement>,
    val contactDetails: List<ContactDetail>,
    val education: List<Education>,
    val experience: List<Experience>,
    val profileLinks: List<ProfileLink>,
    val projects: List<Project>,
    val skills: List<Skill>
)

@Serializable
data class InterviewQuestions(
    val questions: List<String>
)