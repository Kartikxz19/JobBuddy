package com.jainhardik120.jobbuddy.ui.presentation.screens.interview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.jainhardik120.jobbuddy.data.dto.QuestionResponse
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.CollectUiEvents
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VirtualInterviewScreen(
    navController : NavHostController
) {
    val viewModel: VirtualInterviewViewModel = hiltViewModel()

    val state by viewModel.state
    val snackBarHostState = remember {
        SnackbarHostState()
    }

    CollectUiEvents(
        navHostController = navController,
        viewModel.uiEvent,
        hostState = snackBarHostState
    )

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
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state.questions.isEmpty()) {
                TextField(
                    state.jobDescription, onValueChange = viewModel::updateJobDescription
                )
                Button(onClick = {
                    viewModel.generateQuestions()
                }) {
                    Text("Generate Questions")
                }
            } else {
                val currentQuestion = state.questions.getOrNull(state.currentQuestionIndex)
                if (currentQuestion != null) {
                    Text(
                        text = currentQuestion, modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Button(
                        onClick = {
                            if (state.isRecording) {
                                viewModel.stopRecording()
                            } else {
                                viewModel.startRecording()
                            }
                        }, modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (state.isRecording) "Stop Recording" else "Start Recording")
                    }
                    val currentAnswer =
                        state.questionAnswerResponses.getOrNull(state.currentQuestionIndex)?.answer
                    if (currentAnswer.isNullOrEmpty()) {
                        Text(
                            text = "Waiting for your answer...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    } else {
                        Text(text = currentAnswer, modifier = Modifier.padding(top = 16.dp))
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


data class VirtualInterviewState(
    val jobDescription: String = "Senior Full Stack Developer position at a fast-growing tech company",
    val questions: List<String> = emptyList(),
    val questionAnswerResponses: List<QuestionResponse> = mutableListOf(),
    val currentQuestionIndex: Int = 0,
    val isRecording: Boolean = false
)

@HiltViewModel
class VirtualInterviewViewModel @Inject constructor(
    private val api: JobBuddyAPI, @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val _state = mutableStateOf(VirtualInterviewState())
    val state: State<VirtualInterviewState> = _state

    companion object {
        private const val TAG = "VirtualInterviewViewModel"
    }


    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)

    // Define RecognitionListener
    private val speechRecognizerListener: RecognitionListener = object : RecognitionListener {

        // Called when the speech recognizer is ready to start listening
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech: Ready to listen")
        }

        // Called when the user starts speaking
        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech: User started speaking")
        }

        // Called when the volume of the speech changes
        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged: RMS dB value is $rmsdB")
        }

        // Called when a new audio buffer is received
        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d(TAG, "onBufferReceived: Received audio buffer")
        }

        // Called when the speech ends (user stops speaking)
        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: Speech ended")
        }

        // Called if an error occurs during speech recognition
        override fun onError(error: Int) {
            Log.e(TAG, "onError: Speech recognition error occurred with error code $error")
        }

        // Called when speech recognition results are available
        override fun onResults(results: Bundle?) {
            Log.d(TAG, "onResults: Recognition results available")
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                Log.d(TAG, "onResults: Recognized text: ${matches[0]}")
                _state.value =
                    _state.value.copy(questionAnswerResponses = _state.value.questionAnswerResponses.toMutableList()
                        .apply {
                            val currentIndex = _state.value.currentQuestionIndex
                            this[currentIndex] = this[currentIndex].copy(answer = matches[0])
                        }
                    )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            Log.d(TAG, "onPartialResults: Partial recognition results available")
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "onEvent: Event occurred, type: $eventType")
        }
    }

    fun generateQuestions() {
//        makeApiCall({
//            api.getInterviewQuestions(_state.value.jobDescription)
//        }) { response ->
//            _state.value = _state.value.copy(
//                questions = response.questions
//            )
//        }
    }

    fun startRecording() {
        _state.value = _state.value.copy(isRecording = true)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer.setRecognitionListener(speechRecognizerListener)
        speechRecognizer.startListening(intent)
    }

    fun stopRecording() {
        _state.value = _state.value.copy(isRecording = false)
        speechRecognizer.stopListening()
    }

    fun moveToNextQuestion() {
        _state.value = _state.value.copy(
            currentQuestionIndex = _state.value.currentQuestionIndex + 1
        )
        startRecording()
    }

    fun updateJobDescription(newVal: String) {
        _state.value = _state.value.copy(
            jobDescription = newVal
        )
    }

    fun getEvaluationResult() {
//        makeApiCall({
//            api.evaluateInterview(
//                InterviewEvaluationRequest(
//                    _state.value.jobDescription, _state.value.questionAnswerResponses
//                )
//            )
//        }) { response ->
//            Log.d("TAG", "Evaluation result: ${response.evaluation}")
//        }
    }
}
