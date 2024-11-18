package com.jainhardik120.jobbuddy.ui.presentation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable

@Serializable
sealed class AppRoutes() {

    @Serializable
    data object LoginScreen : AppRoutes()

    @Serializable
    data object HomeScreen : AppRoutes()

    @Serializable
    data object ProfileUpdateScreen : AppRoutes()

    @Serializable
    data object BookmarksScreen : AppRoutes()

    @Serializable
    data class VirtualInterviewScreen(
        val jobId: Int
    ) : AppRoutes()

    @Serializable
    data class JobDetailsScreen(
        val jobId: Int
    ) : AppRoutes()
}

fun NavBackStackEntry.toAppRoute(): AppRoutes? {
    return when (this.destination.route) {
        (AppRoutes.LoginScreen::class).qualifiedName -> AppRoutes.LoginScreen
        (AppRoutes.HomeScreen::class).qualifiedName -> AppRoutes.HomeScreen
        (AppRoutes.ProfileUpdateScreen::class).qualifiedName -> AppRoutes.ProfileUpdateScreen
        (AppRoutes.BookmarksScreen::class).qualifiedName -> AppRoutes.BookmarksScreen
        (AppRoutes.JobDetailsScreen::class).qualifiedName -> this.toRoute<AppRoutes.JobDetailsScreen>()
        (AppRoutes.VirtualInterviewScreen::class).qualifiedName -> this.toRoute<AppRoutes.VirtualInterviewScreen>()
        else -> null
    }
}