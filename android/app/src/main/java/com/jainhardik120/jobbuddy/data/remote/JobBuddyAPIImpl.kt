package com.jainhardik120.jobbuddy.data.remote

import android.util.Log
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


class JobBuddyAPIImpl(
    private val client: HttpClient,
    private val keyValueStorage: KeyValueStorage
) : JobBuddyAPI {

    companion object{
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
        Log.d(TAG, "loginGoogle: Call to API")
        Log.d(TAG, "loginGoogle: ${APIRoutes.LOGIN_ROUTE}")
        return performApiRequest {
            requestBuilder(APIRoutes.LOGIN_ROUTE, HttpMethod.Post, request)
        }
    }
}