package com.jainhardik120.jobbuddy.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jainhardik120.gatepay.ui.presentation.screens.login.LoginEvent
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.ui.presentation.screens.login.LoadingDialog
import com.jainhardik120.jobbuddy.ui.presentation.screens.login.LoginScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val viewModel = hiltViewModel<ApplicationViewModel>()
    val navController = rememberNavController()
    val isLoggedIn = viewModel.isLoggedIn
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
            LoginScreen()
        }
        composable(AppRoutes.HomeScreen.route) {
        }
    }
}

