package com.jainhardik120.jobbuddy.ui.presentation

sealed class AppRoutes(val route: String) {
    object LoginScreen : AppRoutes("login_screen")
    object HomeScreen : AppRoutes("home_screen")
    object ProfileUpdateScreen : AppRoutes("update_profile_screen")
    object VirtualInterviewScreen : AppRoutes("virtual_interview_screen")
    object JobDetailsScreen : AppRoutes("job_details_screen")
}