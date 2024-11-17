package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobDetailsScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState

) {
    val viewModel: JobDetailsViewModel = hiltViewModel()
    val state by viewModel.state
    CollectUiEvents(navController, viewModel.uiEvent, snackbarHostState)

    val sheetState = rememberModalBottomSheetState()

    if (state.openEvaluationSheet) {
        ModalBottomSheet({
            viewModel.setEvaluationSheet(false)
        }, sheetState = sheetState, dragHandle = {}, shape = RoundedCornerShape(0.dp)) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) {
                MarkdownText(state.profileEvaluation)
                Button({ viewModel.setEvaluationSheet(false) }) {
                    Text("Close sheet")
                }
            }
        }
    }
    val context = LocalContext.current
    LazyColumn(
        Modifier.Companion
            .fillMaxSize()
    ) {
        item {
            Button({ viewModel.generateStudyPlan() }) { Text("Generate Study Plan") }
        }
        item {
            Button({ viewModel.takeInterviewButton() }) { Text("Take virtual interview") }
        }
        item {
            Button({ viewModel.setEvaluationSheet(true) }) {
                Text("Check Profile Score")
            }
        }
        item {
            Button({
                viewModel.generateTailoredResume(context)
            }) { Text("Generate Tailored Resume") }
        }
        itemsIndexed(state.studyPlan) { index, item ->
            Column(Modifier.fillMaxWidth()) {
                Text(item.skill)
                item.flashCards.forEach {
                    Text(it.question)
                    Text(it.answer)
                }
            }
        }
    }
}