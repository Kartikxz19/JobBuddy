package com.jainhardik120.jobbuddy.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class JobPosting(
    val role : String,
    val experience : String,
    val skills : List<String>,
    val description : String
)