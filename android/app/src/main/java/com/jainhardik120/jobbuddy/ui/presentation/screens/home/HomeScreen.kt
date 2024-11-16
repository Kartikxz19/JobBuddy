package com.jainhardik120.jobbuddy.ui.presentation.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.R
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController
) {
    val viewModel: UploadFileViewModel = hiltViewModel()
    val state by viewModel.state

    CollectUiEvents(
        navHostController = navController,
        events = viewModel.uiEvent,
        hostState = null
    )

    Scaffold(
        Modifier.Companion.imePadding(), topBar = {
            CenterAlignedTopAppBar(title = {
                Text(text = stringResource(id = R.string.app_name))
            }, actions = {
                IconButton({
                    navController.navigate(AppRoutes.ProfileUpdateScreen.route)
                }) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Profile Icon"
                    )
                }
            })
        }, contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            Modifier.Companion
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .systemBarsPadding()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            var showBottomSheet = remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()

            if (showBottomSheet.value) {
                ModalBottomSheet(
                    onDismissRequest = {
                        if (!state.isCreatingJobData) {
                            showBottomSheet.value = false
                        }
                    }, sheetState = sheetState
                ) {
                    var jobDescription = remember { mutableStateOf("") }
                    TextField(
                        value = jobDescription.value,
                        onValueChange = { jobDescription.value = it })
                    Button(
                        onClick = { viewModel.createJob(jobDescription.value) },
                        enabled = !state.isCreatingJobData
                    ) {
                        Text(text = if (state.isCreatingJobData) "Creating Job" else "Create Job")
                    }
                }
            }

            Button({
                showBottomSheet.value = true
            }) {
                Text("Add Job")
            }

            LazyColumn {
                itemsIndexed(state.jobList) { index, item ->
                    Row {
                        Text(item.id.toString())
                        Text(item.role)
                    }
                }
            }
        }
    }
}