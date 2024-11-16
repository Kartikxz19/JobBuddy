package com.jainhardik120.jobbuddy.ui.presentation

import kotlinx.serialization.Serializable

sealed class AppRoutes() {

    @Serializable
    data object LoginScreen : AppRoutes()

    @Serializable
    data object HomeScreen : AppRoutes()

    @Serializable
    data object ProfileUpdateScreen : AppRoutes()

    @Serializable
    data class VirtualInterviewScreen(
        val jobId: Int
    ) : AppRoutes()

    @Serializable
    data class JobDetailsScreen(
        val jobId: Int
    ) : AppRoutes()
}