package com.jainhardik120.jobbuddy.ui.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.FlashCard
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.presentation.screens.home.HomeScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.home.UploadFileViewModel
import com.jainhardik120.jobbuddy.ui.presentation.screens.interview.VirtualInterviewScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails.JobDetailsScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.login.LoginScreen
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ProfileUpdateScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

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
                        var expanded by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .wrapContentSize(Alignment.TopStart)
                        ) {
                            IconButton(onClick = { expanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = "Localized description"
                                )
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Profile") },
                                    onClick = {
                                        navController.navigate(AppRoutes.ProfileUpdateScreen)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Person,
                                            contentDescription = "Profile Icon"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        homeViewModel.logout()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.ExitToApp,
                                            contentDescription = "Logout"
                                        )
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Bookmarks") },
                                    onClick = {
                                        navController.navigate(AppRoutes.BookmarksScreen)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.AccountBox,
                                            contentDescription = "Bookmarks"
                                        )
                                    }
                                )
                            }
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
//            startDestination = if (isLoggedIn) {
                AppRoutes.HomeScreen,
//            } else {
//                AppRoutes.LoginScreen
//            },
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
            composable<AppRoutes.BookmarksScreen> {
                val viewModel: BookmarksViewModel = hiltViewModel()
                LazyColumn {
                    itemsIndexed(viewModel.state.value) { index, item ->
                        com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails.FlashCard(it = item)
                    }
                }
            }
        }
    }
}

@HiltViewModel
class BookmarksViewModel @Inject constructor(
    jbDatabase: JBDatabase
) : BaseViewModel() {
    private val _state = mutableStateOf(emptyList<FlashCard>())
    val state: State<List<FlashCard>> = _state

    init {
        jbDatabase.dao.getFlashCards().onEach {
            _state.value = it
        }.launchIn(viewModelScope)
    }
}