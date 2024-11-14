package com.jainhardik120.jobbuddy.data.remote

import android.net.Uri
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.KeyValueStorage
import com.jainhardik120.jobbuddy.data.remote.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.remote.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.remote.dto.MessageError
import com.jainhardik120.jobbuddy.data.remote.dto.MessageResponse
import kotlinx.coroutines.flow.Flow
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


class JobBuddyAPIImpl(
    private val client: HttpClient,
    private val keyValueStorage: KeyValueStorage,
    private val fileReader: FileReader
) : JobBuddyAPI {

    companion object {
        private const val TAG = "JobBuddyAPIImpl"
    }

    private suspend inline fun <T, reified R> performApiRequest(
        call: () -> T
    ): Result<T, R> {
        return try {
            val response = call.invoke()
            Result.Success(response)
        } catch (e: ClientRequestException) {
            Result.ClientException(e.response.body<R>(), e.response.status)
        } catch (e: Exception) {
            Result.Exception(e.message)
        }
    }

    private fun HttpRequestBuilder.tokenAuthHeaders(headers: HeadersBuilder.() -> Unit = {}) {
        headers {
            keyValueStorage.getToken()?.let {
                bearerAuth(token = it)
            }
            headers()
        }
    }

    private suspend inline fun <reified T, reified R> requestBuilder(
        url: String,
        method: HttpMethod,
        body: T
    ): R {
        return client.request(url) {
            this.method = method
            contentType(ContentType.Application.Json)
            setBody(body)
            tokenAuthHeaders()
        }.body()
    }

    private suspend inline fun <reified T> requestBuilder(
        url: String,
        method: HttpMethod
    ): T {
        return client.request(url) {
            this.method = method
            tokenAuthHeaders()
        }.body()
    }

    override suspend fun loginGoogle(request: GoogleLoginRequest): Result<LoginResponse, MessageError> {
        return performApiRequest {
            requestBuilder(APIRoutes.LOGIN_ROUTE, HttpMethod.Post, request)
        }
    }

    override suspend fun checkResumeScore(request: ResumeScoreRequest): Result<ResumeScoreResponse, MessageError> {
        return performApiRequest {
            requestBuilder(APIRoutes.RESUME_SCORE_ROUTE, HttpMethod.Post, request)
        }
    }

    override suspend fun getResumeStatus(): Result<ResumeStatusResponse, MessageError> {
        return performApiRequest {
            requestBuilder(
                url = APIRoutes.RESUME_STATUS_ROUTE,
                method = HttpMethod.Get
            )
        }
    }

    override suspend fun getProfileDetails(): Result<ProfileDetails, MessageError> {
        return performApiRequest {
            requestBuilder(APIRoutes.PROFILE_ROUTE, HttpMethod.Get)
        }
    }

    override suspend fun updateProfileDetails(data: ProfileDetails): Result<MessageResponse, MessageError> {
        return performApiRequest {
            requestBuilder(APIRoutes.PROFILE_ROUTE, HttpMethod.Put, data)
        }
    }

    override fun uploadResume(contentUri: Uri): Flow<ProgressUpdate> = channelFlow {
        val info = fileReader.uriToFileInfo(contentUri)
        client.submitFormWithBinaryData(
            url = APIRoutes.RESUME_UPLOAD_ROUTE,
            formData = formData {
                append("resume_file", info.bytes, Headers.build {
                    append(HttpHeaders.ContentType, info.mimeType)
                    append(HttpHeaders.ContentDisposition, "filename=${info.name}.pdf")
                })
            }
        ) {
            tokenAuthHeaders()
            onUpload { bytesSentTotal, totalBytes ->
                if (totalBytes != null) {
                    if (totalBytes > 0L) {
                        send(ProgressUpdate(bytesSentTotal, totalBytes))
                    }
                }
            }
        }
    }
}


@Serializable
data class ResumeScoreRequest(
    @SerialName("job_description")
    val jobDescription: String
)

@Serializable
data class ResumeScoreResponse(
    @SerialName("answer")
    val message: String,
    @SerialName("extracted_resume")
    val resumeText: String,
    @SerialName("job_posting")
    val jobDescription: String
)

@Serializable
data class ResumeStatusResponse(
    val message: String,
    @SerialName("file_path")
    val filePath: String? = null,
    @SerialName("upload_time")
    val uploadTime: String? = null
)
