package com.jainhardik120.jobbuddy.data.remote

import android.net.Uri
import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.dto.EvaluationResponse
import com.jainhardik120.jobbuddy.data.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.dto.InterviewEvaluationRequest
import com.jainhardik120.jobbuddy.data.dto.InterviewInsightResponse
import com.jainhardik120.jobbuddy.data.dto.InterviewQuestions
import com.jainhardik120.jobbuddy.data.dto.JobPosting
import com.jainhardik120.jobbuddy.data.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.dto.MessageError
import com.jainhardik120.jobbuddy.data.dto.MessageResponse
import com.jainhardik120.jobbuddy.data.dto.ProfileDetails
import com.jainhardik120.jobbuddy.data.dto.ResumeScoreResponse
import com.jainhardik120.jobbuddy.data.local.entity.FlashCard
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

interface JobBuddyAPI {

    suspend fun loginGoogle(request: GoogleLoginRequest): Result<LoginResponse, MessageError>
    suspend fun listUploadedResumes(): Result<Resumes, MessageError>
    fun uploadResume(contentUri: Uri): Flow<ProgressUpdate>
    suspend fun downloadResume(resumeIdWithExtension: String): Result<HttpResponse, MessageError>
    suspend fun deleteResume(resumeId: String): Result<MessageResponse, MessageError>
    suspend fun getProfileDetails(): Result<ProfileDetails, MessageError>
    suspend fun updateProfileDetails(data: ProfileDetails): Result<MessageResponse, MessageError>
    suspend fun generateProfileFromResume(resumeId: String): Result<ProfileDetails, MessageError>
    suspend fun simplifyJobData(jobDescription: String): Result<JobPosting, MessageError>
    suspend fun checkResumeScore(jobData: JobPosting): Result<ResumeScoreResponse, MessageError>
    suspend fun generateInterviewInsights(jobData: JobPosting): Result<InterviewInsightResponse, MessageError>
    suspend fun generateTailoredResume(jobData: JobPosting): Result<HttpResponse, MessageError>
    suspend fun generateFlashCards(jobData: JobPosting): Result<StudyPlanResponse, MessageError>
    suspend fun getInterviewQuestions(jobData: JobPosting): Result<InterviewQuestions, MessageError>
    suspend fun evaluateInterview(request: InterviewEvaluationRequest): Result<EvaluationResponse, MessageError>
}

@Serializable
data class StudyPlanResponse(
    @SerialName("study_plan")
    val studyPlan: List<StudyPlan>
)

@Serializable
data class StudyPlan(
    val skill: String,
    val status: String,
    @SerialName("flashcards")
    val flashCards: List<FlashCard>
)

@Serializable
data class Resumes(
    val resumes: List<Resume>
)

@Serializable
data class Resume(
    @SerialName("resume_path")
    val resumePath: String,
    @SerialName("upload_time")
    val uploadTime: String,
    val generated: Boolean
)

fun Resume.toResumeId(): String {
    return (resumePath.substringAfterLast("_")).substringBeforeLast(".")
}