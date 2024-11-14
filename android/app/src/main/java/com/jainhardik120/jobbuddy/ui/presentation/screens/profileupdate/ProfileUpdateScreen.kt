package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun ProfileUpdateScreen(
    state: EditUserDetailsState, onEvent: (EditUserDetailsEvent) -> Unit
) {
    LazyColumn {
        profileSection(
            dataList = state.skills,
            onEvent = onEvent,
            newItem = InputModel.Skill("", 0)
        )
        profileSection(
            dataList = state.projects, onEvent = onEvent,
            newItem = InputModel.Project("", "", "", InputType.Date(0, 0), InputType.Date(0, 0), "")
        )
        profileSection(
            dataList = state.experience, onEvent = onEvent,
            newItem = InputModel.Experience("", "", InputType.Date(0, 0), InputType.Date(0, 0), "")
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
        Column {
            item.fields.entries.forEach { entry ->
                Text("${entry.value.name} : ${entry.value.type.displayName()}")
            }
            Button(onClick = {
                showBottomSheet.value = true
            }) {
                Text("Edit")
            }
            if (showBottomSheet.value) {
                EditModal(hideBottomSheet = { showBottomSheet.value = false },
                    scope = scope,
                    sheetState = sheetState,
                    fields = item.fields,
                    onUpdate = { updatedValues ->
                        onEvent(
                            EditUserDetailsEvent.Updated(index, item.apply {
                                fields = updatedValues
                            })
                        )
                    }
                )
            }
        }
    }
    item {
        Button(onClick = {
            onEvent(
                EditUserDetailsEvent.Added(
                    newItem
                )
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
    onUpdate: (Map<String, InputFieldType>) -> Unit
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

    ModalBottomSheet(sheetState = sheetState, onDismissRequest = hideBottomSheet, dragHandle = {

    }) {
        Column {
            fields.entries.forEach { (key, fieldType) ->
                when (fieldType.type) {
                    is InputType.Text -> {
                        TextField(value = fieldStates[key]?.value ?: "",
                            onValueChange = { fieldStates[key]?.value = it },
                            label = { Text(fieldType.name) })
                    }

                    is InputType.Number -> {
                        TextField(
                            value = fieldStates[key]?.value ?: "",
                            onValueChange = { value ->
                                // Validate and update only if it's a number
                                if (value.toIntOrNull() != null) {
                                    fieldStates[key]?.value = value
                                }
                            },
                            label = { Text(fieldType.name) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }

                    is InputType.Date -> {
                        TextField(value = fieldStates[key]?.value ?: "",
                            onValueChange = { fieldStates[key]?.value = it },
                            label = { Text(fieldType.name) },
                            // Add date picker or hint text as needed
                            placeholder = { Text("MM/YYYY") })
                    }
                }
            }
            Button(onClick = {
                val updatedValue: Map<String, InputFieldType> =
                    fields.mapValues { (key, fieldType) ->
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
            }) {
                Text("Update")
            }
        }
    }
}