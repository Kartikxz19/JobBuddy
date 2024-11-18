package com.jainhardik120.jobbuddy.ui.presentation.screens.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.data.local.entity.Job
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    viewModel: UploadFileViewModel = hiltViewModel()
) {
    val state by viewModel.state

    CollectUiEvents(
        navHostController = navController,
        events = viewModel.uiEvent,
        hostState = snackbarHostState
    )

    val sheetState = rememberModalBottomSheetState()
    if (state.jobSheetOpened) {
        AddJobBottomSheet(sheetState, viewModel, state)
    }
    LazyColumn(Modifier.fillMaxSize()) {
        itemsIndexed(state.jobList) { index, item ->
            JobItemLayout(job = item, navController = navController)
            if (index < state.jobList.size - 1) {
                HorizontalDivider(Modifier.fillMaxWidth())
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobBottomSheet(
    sheetState: SheetState,
    viewModel: UploadFileViewModel,
    state: UploadState
) {
    var jobDescription = remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isCreatingJobData) {
                viewModel.setJobSheet(false)
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Add New Job",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedTextField(
                value = jobDescription.value,
                onValueChange = { jobDescription.value = it },
                label = { Text("Enter detailed job description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 150.dp)
                    .padding(bottom = 16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 5,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {

                    }
                )
            )
            Button(
                onClick = { viewModel.createJob(jobDescription.value) },
                enabled = !state.isCreatingJobData,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = if (state.isCreatingJobData) "Creating Job" else "Create Job")
            }
        }
    }
}


@Composable
fun JobItemLayout(job: Job, navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(AppRoutes.JobDetailsScreen(jobId = job.id))
            }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = job.role,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Experience: ${job.experience}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Skills: ${job.skills.joinToString(", ")}",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}