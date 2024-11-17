package com.jainhardik120.jobbuddy.ui.presentation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.ui.presentation.screens.home.HomeScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.home.UploadFileViewModel
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
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val route: AppRoutes? = currentBackStackEntry?.toAppRoute()
    val homeViewModel: UploadFileViewModel = hiltViewModel()

    Scaffold(
        Modifier.Companion.imePadding(),
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name))
            }, actions = {
                when (route) {
                    AppRoutes.HomeScreen -> {
                        IconButton({
                            navController.navigate(AppRoutes.ProfileUpdateScreen)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile Icon"
                            )
                        }
                        IconButton({
                            homeViewModel.logout()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp, contentDescription = "Logout"
                            )
                        }
                    }

                    else -> {

                    }
                }
            })
        },
        floatingActionButton = {
            when (route) {
                AppRoutes.HomeScreen -> {
                    ExtendedFloatingActionButton(onClick = {
                        homeViewModel.setJobSheet(true)
                    }, icon = { Icon(Icons.Filled.Add, "Add Icon") }, text = { Text("Add Job") })
                }

                else -> {

                }
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn) {
                AppRoutes.HomeScreen
            } else {
                AppRoutes.LoginScreen
            },
            modifier = Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .systemBarsPadding()
                .padding(8.dp)
        ) {
            composable<AppRoutes.LoginScreen> {
                LoginScreen(
                    navController = navController, snackbarHostState = snackBarHostState
                )
            }
            composable<AppRoutes.HomeScreen> {
                HomeScreen(
                    navController = navController,
                    snackbarHostState = snackBarHostState,
                    viewModel = homeViewModel
                )
            }
            composable<AppRoutes.ProfileUpdateScreen> {
                ProfileUpdateScreen(
                    navController = navController, snackbarHostState = snackBarHostState
                )
            }
            composable<AppRoutes.VirtualInterviewScreen> {
                VirtualInterviewScreen(
                    navController = navController, snackbarHostState = snackBarHostState
                )
            }
            composable<AppRoutes.JobDetailsScreen> {
                JobDetailsScreen(
                    navController = navController, snackbarHostState = snackBarHostState
                )
            }
        }
    }
}