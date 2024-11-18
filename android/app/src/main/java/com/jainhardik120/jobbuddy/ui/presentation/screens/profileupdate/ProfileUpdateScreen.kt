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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.data.remote.toResumeId
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

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
    val context = LocalContext.current
    var showBottomSheet = remember { mutableStateOf(false) }
    var forGeneratingProfile = remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    if (showBottomSheet.value) {
        ResumesModalSheet(
            state = state,
            viewModel = viewModel,
            sheetState = sheetState,
            setBottomSheetValue = {
                showBottomSheet.value = it
            },
            forGeneratingProfile = forGeneratingProfile.value,
            onSelectFile = {
                viewModel.generateProfileFromResume(it)
            },
            onDownloadFile = {
                viewModel.downloadFile(it, context)
            }
        )
    }

    LazyColumn(
        Modifier
            .fillMaxSize()
    ) {
        item {
            Button({
                forGeneratingProfile.value = false
                showBottomSheet.value = true
            }) {
                Text("Manage Resumes")
            }
            Button({
                forGeneratingProfile.value = true
                showBottomSheet.value = true
            }) {
                Text("Generate Profile using resume")
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
@Composable
fun ResumesModalSheet(
    state: EditUserDetailsState,
    viewModel: EditUserDetailsViewModel,
    sheetState: SheetState,
    setBottomSheetValue: (Boolean) -> Unit,
    forGeneratingProfile: Boolean,
    onSelectFile: (String) -> Unit,
    onDownloadFile: (String) -> Unit
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { contentUri ->
        contentUri?.let {
            viewModel.uploadFile(contentUri)
        }
    }
    ModalBottomSheet(
        onDismissRequest = {
            if (!state.isUploading) {
                setBottomSheetValue(false)
            }
        }, sheetState = sheetState
    ) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            itemsIndexed(if (forGeneratingProfile) state.userResumes.filter { !it.generated } else state.userResumes) { index, item ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.toResumeId())
                    Spacer(Modifier.weight(1f))
                    var expanded by remember { mutableStateOf(false) }
                    if (forGeneratingProfile) {
                        Button({ onSelectFile(item.toResumeId()) }) {
                            Text("Use")
                        }
                    }
                    FilledTonalIconButton({ onDownloadFile(item.toResumeId() + ".pdf") }) {
                        Icon(Icons.Default.KeyboardArrowDown, "Download Icon")
                    }
                    Box(
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart)
                    ) {
                        FilledTonalIconButton(onClick = { expanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Localized description"
                            )
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }) {
                            if (item.generated) {
                                DropdownMenuItem(
                                    text = { Text("Download Latex") },
                                    onClick = { onDownloadFile(item.toResumeId() + ".tex") }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Delete Resume") },
                                onClick = {

                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Outlined.Delete,
                                        contentDescription = null
                                    )
                                }
                            )
                        }

                    }
                }
                HorizontalDivider(Modifier.fillMaxWidth())
            }
            item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("Upload resume", style = MaterialTheme.typography.headlineMedium)
                        Button(onClick = {
                            if (!state.isUploading) {
                                filePickerLauncher.launch("*/*")
                            } else {
                                viewModel.cancelUpload()

                            }
                        }) {
                            Text(text = if (!state.isUploading) "Pick a file" else "Cancel upload")
                        }
                    }
                    if (state.isUploading) {
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
                                .height(8.dp)
                        )
                    }
                }
            }
        }
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