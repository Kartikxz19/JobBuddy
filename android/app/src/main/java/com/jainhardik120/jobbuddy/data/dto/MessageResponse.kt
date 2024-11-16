package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    @SerialName("message")
    val message:  String
)