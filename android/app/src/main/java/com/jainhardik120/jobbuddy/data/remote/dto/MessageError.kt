package com.jainhardik120.jobbuddy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageError(
    @SerialName("error")
    val error:  String
)