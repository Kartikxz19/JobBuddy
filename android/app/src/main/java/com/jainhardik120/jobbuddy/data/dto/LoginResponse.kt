package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    @SerialName("user_id")
    val userID: Int
)