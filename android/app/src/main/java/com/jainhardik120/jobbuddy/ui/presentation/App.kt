package com.jainhardik120.jobbuddy.ui.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jainhardik120.jobbuddy.ui.presentation.screens.home.HomeScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.interview.VirtualInterviewScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.login.LoginScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ProfileUpdateScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val appViewModel = hiltViewModel<ApplicationViewModel>()
    val navController = rememberNavController()
    val isLoggedIn = appViewModel.isLoggedIn
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.popBackStack()
            navController.navigate(AppRoutes.HomeScreen.route)
        } else {
            navController.popBackStack()
            navController.navigate(AppRoutes.LoginScreen.route)
        }
    }
    NavHost(
        navController = navController, startDestination = if (isLoggedIn) {
            AppRoutes.HomeScreen.route
        } else {
            AppRoutes.LoginScreen.route
        }
    ) {
        composable(AppRoutes.LoginScreen.route) {
            LoginScreen(navController = navController)
        }
        composable(AppRoutes.HomeScreen.route) {
            HomeScreen(navController = navController)
        }
        composable(AppRoutes.ProfileUpdateScreen.route) {
            ProfileUpdateScreen(navController = navController)
        }
        composable(AppRoutes.VirtualInterviewScreen.route) {
            VirtualInterviewScreen(navController = navController)
        }
        composable(AppRoutes.JobDetailsScreen.route) {
            // Here we will enter job description, and then generate the content from backend and show relevant options to user
        }
    }
}

