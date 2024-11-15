package com.jainhardik120.jobbuddy.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.ui.presentation.screens.login.LoginScreen
import androidx.compose.runtime.getValue
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.jainhardik120.jobbuddy.ui.presentation.screens.interview.VirtualInterviewScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.interview.VirtualInterviewViewModel
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.EditUserDetailsViewModel
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ProfileUpdateScreen
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlin.math.roundToInt

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
//        navController = navController, startDestination = AppRoutes.HomeScreen.route
    ) {
        composable(AppRoutes.LoginScreen.route) {
            LoginScreen()
        }
        composable(AppRoutes.HomeScreen.route) {
            val viewModel: UploadFileViewModel = hiltViewModel()
            val state by viewModel.state

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { contentUri ->
                contentUri?.let {
                    viewModel.uploadFile(contentUri)
                }
            }

            val context = LocalContext.current


            LaunchedEffect(key1 = state.errorMessage) {
                state.errorMessage?.let {
                    Toast.makeText(
                        context, state.errorMessage, Toast.LENGTH_LONG
                    ).show()
                }
            }

            LaunchedEffect(key1 = state.isUploadComplete) {
                if (state.isUploadComplete) {
                    Toast.makeText(
                        context, "Upload complete!", Toast.LENGTH_LONG
                    ).show()
                }
            }

            Scaffold(
                Modifier.imePadding(), topBar = {
                    CenterAlignedTopAppBar(title = {
                        Text(text = stringResource(id = R.string.app_name))
                    })
                }, contentWindowInsets = WindowInsets(0, 0, 0, 0)
            ) { paddingValues ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .consumeWindowInsets(paddingValues)
                        .systemBarsPadding()
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            appViewModel.logOut()
                        }
                    ) {
                        Text("Logout")
                    }
                    Button(onClick = {
                        viewModel.checkStatus()
                    }) {
                        Text("Check status")
                    }
                    Button(onClick = {
                        navController.navigate(AppRoutes.VirtualInterviewScreen.route)
                    }

                    ) {
                        Text("Take virtual interview")
                    }
                    Button(onClick = {
                        navController.navigate(AppRoutes.ProfileUpdateScreen.route)
                    }

                    ) {
                        Text("Go to profile edit")
                    }
                    var value = remember { mutableStateOf("") }
                    OutlinedTextField(value = value.value, onValueChange = { newVal ->
                        value.value = newVal
                    })
                    Button(
                        onClick = {
                            viewModel.checkResumeScore(value.value)
                        }, shape = MaterialTheme.shapes.small
                    ) {
                        Text("Check Score")
                    }
                    state.markdownText?.let { it ->
                        MarkdownText(markdown = it)

                    }
                    when {
                        !state.isUploading -> {
                            Button(onClick = {
                                filePickerLauncher.launch("*/*")
                            }) {
                                Text(text = "Pick a file")
                            }
                        }

                        else -> {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val animatedProgress by animateFloatAsState(
                                    targetValue = state.progress,
                                    animationSpec = tween(durationMillis = 100),
                                    label = "File upload progress bar"
                                )
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                        .height(16.dp)
                                )
                                Text(
                                    text = "${(state.progress * 100).roundToInt()}%"
                                )
                                Button(onClick = {
                                    viewModel.cancelUpload()
                                }) {
                                    Text(text = "Cancel upload")
                                }
                            }
                        }
                    }
                }
            }
        }

        composable(AppRoutes.ProfileUpdateScreen.route) {
            val viewModel: EditUserDetailsViewModel = hiltViewModel()
            val state by viewModel.state
            ProfileUpdateScreen(state = state, onEvent = viewModel::onEvent)
        }

        composable(AppRoutes.VirtualInterviewScreen.route) {
            VirtualInterviewScreen(navController = navController)
        }
    }
}


