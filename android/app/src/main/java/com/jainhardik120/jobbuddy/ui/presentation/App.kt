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
import com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails.JobDetailsScreen
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
            navController.navigate(AppRoutes.HomeScreen)
        } else {
            navController.popBackStack()
            navController.navigate(AppRoutes.LoginScreen)
        }
    }
    NavHost(
        navController = navController, startDestination = if (isLoggedIn) {
            AppRoutes.HomeScreen
        } else {
            AppRoutes.LoginScreen
        }
    ) {
        composable<AppRoutes.LoginScreen> {
            LoginScreen(navController = navController)
        }
        composable<AppRoutes.HomeScreen> {
            HomeScreen(navController = navController)
        }
        composable<AppRoutes.ProfileUpdateScreen> {
            ProfileUpdateScreen(navController = navController)
        }
        composable<AppRoutes.VirtualInterviewScreen> {
            VirtualInterviewScreen(navController = navController)
        }
        composable<AppRoutes.JobDetailsScreen> {
            JobDetailsScreen(navController = navController)
        }
    }
}