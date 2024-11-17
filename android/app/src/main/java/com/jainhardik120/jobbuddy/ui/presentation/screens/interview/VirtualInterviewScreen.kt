package com.jainhardik120.jobbuddy.ui.presentation.screens.interview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import dev.jeziellago.compose.markdowntext.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualInterviewScreen(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState
) {
    val viewModel: VirtualInterviewViewModel = hiltViewModel()

    val state by viewModel.state
    CollectUiEvents(
        navHostController = navController,
        viewModel.uiEvent,
        hostState = snackbarHostState
    )
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        if (state.isGeneratingQuestions) {
            CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            Text(
                text = "Generating questions, please wait...",
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (state.questions.isEmpty()) {
            Button(onClick = {
                viewModel.generateQuestions()
            }) {
                Text("Generate Questions")
            }
        } else {
            val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
            if (currentQuestion != null) {
                Text(
                    text = currentQuestion,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(
                    onClick = {
                        if (state.isRecording) {
                            viewModel.stopRecording()
                        } else {
                            viewModel.startRecording()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (state.isRecording) "Stop Recording" else "Start Recording")
                }
                val currentAnswer =
                    state.questionAnswerResponses.getOrNull(state.currentQuestionIndex)?.answer
                if (currentAnswer.isNullOrEmpty()) {
                    Text(
                        text = state.currentPartialText.ifEmpty { "Waiting for your answer..." },
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Text(
                        text = currentAnswer,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                if (currentAnswer != null) {
                    Button(
                        onClick = { viewModel.moveToNextQuestion() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Next Question")
                    }
                }
            }
            if (state.currentQuestionIndex == state.questions.size) {
                if (state.isEvaluating) {
                    CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    Text(
                        text = "Evaluating your responses, please wait...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else if (state.evaluationResult.isNotEmpty()) {
                    MarkdownText(
                        state.evaluationResult,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Button(
                        onClick = { viewModel.getEvaluationResult() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Evaluate")
                    }
                }
            }
        }
    }
}