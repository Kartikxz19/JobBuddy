package com.jainhardik120.jobbuddy.data.remote

import com.jainhardik120.jobbuddy.Result
import com.jainhardik120.jobbuddy.data.remote.dto.GoogleLoginRequest
import com.jainhardik120.jobbuddy.data.remote.dto.LoginResponse
import com.jainhardik120.jobbuddy.data.remote.dto.MessageError

interface JobBuddyAPI {
    suspend fun loginGoogle(request: GoogleLoginRequest) : Result<LoginResponse, MessageError>
}