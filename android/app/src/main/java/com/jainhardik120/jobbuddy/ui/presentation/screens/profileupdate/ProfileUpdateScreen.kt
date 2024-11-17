package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileUpdateScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val viewModel: EditUserDetailsViewModel = hiltViewModel()
    val state by viewModel.state


    CollectUiEvents(
        navHostController = navController,
        events = viewModel.uiEvent,
        hostState = snackbarHostState
    )


    val onEvent: (EditUserDetailsEvent) -> Unit = viewModel::onEvent
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { contentUri ->
        contentUri?.let {
            viewModel.uploadFile(contentUri)
        }
    }
    var showBottomSheet = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    if (showBottomSheet.value) {
        ModalBottomSheet(
            onDismissRequest = {
                if (!state.isUploading) {
                    showBottomSheet.value = false
                }
            }, sheetState = sheetState
        ) {
            LazyColumn {
                itemsIndexed(state.userResumes) { index, item ->
                    Text(item.resumePath)
                }
                item {
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
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        item {
            Button({
                showBottomSheet.value = true
            }) {
                Text("Manage Resumes")
            }
        }
        profileSection(
            dataList = state.skills,
            onEvent = onEvent,
            newItem = InputModel.Skill("", 0)
        )
        profileSection(
            dataList = state.projects, onEvent = onEvent,
            newItem = InputModel.Project(
                "",
                "",
                "",
                InputType.Date(0, 0),
                InputType.Date(0, 0),
                ""
            )
        )
        profileSection(
            dataList = state.experience, onEvent = onEvent,
            newItem = InputModel.Experience(
                "",
                "",
                InputType.Date(0, 0),
                InputType.Date(0, 0),
                ""
            )
        )
        profileSection(
            dataList = state.achievements, onEvent = onEvent,
            newItem = InputModel.Achievement("", "")
        )
        profileSection(
            dataList = state.education, onEvent = onEvent,
            newItem = InputModel.Education("", "", InputType.Date(0, 0), InputType.Date(0, 0))
        )
        profileSection(
            dataList = state.profileLinks, onEvent = onEvent,
            newItem = InputModel.ProfileLink("", "")
        )
        profileSection(
            dataList = state.contactDetails, onEvent = onEvent,
            newItem = InputModel.ContactDetail("", "")
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.profileSection(
    dataList: List<InputModel>, onEvent: (EditUserDetailsEvent) -> Unit, newItem: InputModel
) {
    itemsIndexed(dataList) { index, item ->
        var showBottomSheet = remember { mutableStateOf(false) }
        val sheetState = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()

        Column(modifier = Modifier.padding(16.dp)) {
            // Display the section name as a title
//            Text(
//                text = item.name,
//                style = MaterialTheme.typography.titleMedium,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    item.fields.entries.forEach { entry ->
                        Text("${entry.value.name} : ${entry.value.type.displayName()}")
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp)
                    ) {
                        IconButton(onClick = { showBottomSheet.value = true }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                }
            }

            if (showBottomSheet.value) {
                EditModal(
                    hideBottomSheet = { showBottomSheet.value = false },
                    scope = scope,
                    sheetState = sheetState,
                    fields = item.fields,
                    onUpdate = { updatedValues ->
                        onEvent(
                            EditUserDetailsEvent.Updated(index, item.apply {
                                fields = updatedValues
                            })
                        )
                    }, onRemove = {
                        onEvent(EditUserDetailsEvent.Removed(index = index, item = item))
                    }
                )
            }
        }
    }

    item {
        Button(onClick = {
            onEvent(
                EditUserDetailsEvent.Added(newItem)
            )
        }) {
            Text("Add New Item")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditModal(
    hideBottomSheet: () -> Unit,
    scope: CoroutineScope,
    sheetState: SheetState,
    fields: Map<String, InputFieldType>,
    onUpdate: (Map<String, InputFieldType>) -> Unit,
    onRemove: () -> Unit
) {
    val fieldStates = remember(fields) {
        fields.mapValues { (_, fieldType) ->
            mutableStateOf(
                when (fieldType.type) {
                    is InputType.Text -> fieldType.type.value
                    is InputType.Number -> fieldType.type.value.toString()
                    is InputType.Date -> {
                        val date = fieldType.type
                        "${date.month}/${date.year}"
                    }
                }
            )
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = hideBottomSheet
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            fields.entries.forEach { (key, fieldType) ->
                when (fieldType.type) {
                    is InputType.Text -> {
                        OutlinedTextField(
                            value = fieldStates[key]?.value ?: "",
                            onValueChange = { fieldStates[key]?.value = it },
                            label = { Text(fieldType.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    is InputType.Number -> {
                        OutlinedTextField(
                            value = fieldStates[key]?.value ?: "",
                            onValueChange = { value ->
                                if (value.toIntOrNull() != null) {
                                    fieldStates[key]?.value = value
                                }
                            },
                            label = { Text(fieldType.name) },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Number
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }

                    is InputType.Date -> {
                        OutlinedTextField(
                            value = fieldStates[key]?.value ?: "",
                            onValueChange = { fieldStates[key]?.value = it },
                            label = { Text(fieldType.name) },
                            placeholder = { Text("MM/YYYY") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        val updatedValue = fields.mapValues { (key, fieldType) ->
                            val updatedField = fieldStates[key]?.value
                            val newType = when (fieldType.type) {
                                is InputType.Text -> InputType.Text(updatedField ?: "")
                                is InputType.Number -> InputType.Number(
                                    updatedField?.toIntOrNull() ?: 0
                                )

                                is InputType.Date -> {
                                    val parts = updatedField?.split("/")
                                    val month = parts?.getOrNull(0)?.toIntOrNull() ?: 1
                                    val year =
                                        parts?.getOrNull(1)?.toIntOrNull() ?: Calendar.getInstance()
                                            .get(Calendar.YEAR)
                                    InputType.Date(month, year)
                                }
                            }
                            fieldType.copy(type = newType)
                        }
                        onUpdate(updatedValue)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                hideBottomSheet()
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
                OutlinedButton(
                    onClick = {
                        onRemove()
                        hideBottomSheet()
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}