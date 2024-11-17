package com.jainhardik120.jobbuddy.ui.presentation.screens.interview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jainhardik120.jobbuddy.data.dto.InterviewEvaluationRequest
import com.jainhardik120.jobbuddy.data.dto.QuestionResponse
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.toJobPosting
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class VirtualInterviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val jbDatabase: JBDatabase,
    private val api: JobBuddyAPI,
    @ApplicationContext private val context: Context
) : BaseViewModel() {

    private val screenDetails: AppRoutes.VirtualInterviewScreen = savedStateHandle.toRoute()

    private val _state = mutableStateOf(VirtualInterviewState())
    val state: State<VirtualInterviewState> = _state

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                jobData = jbDatabase.dao.getJobDetails(screenDetails.jobId)
            )
        }
    }

    companion object {
        private const val TAG = "VirtualInterviewViewModel"
    }


    private val speechRecognizer: SpeechRecognizer =
        SpeechRecognizer.createOnDeviceSpeechRecognizer(context)

    private val speechRecognizerListener: RecognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d(TAG, "onReadyForSpeech: Ready to listen")
        }

        override fun onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech: User started speaking")
            _state.value =
                _state.value.copy(isRecording = true) // Ensure state reflects recording has started
        }

        override fun onRmsChanged(rmsdB: Float) {
            Log.d(TAG, "onRmsChanged: RMS dB value is $rmsdB")
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            Log.d(TAG, "onBufferReceived: Received audio buffer")
        }

        override fun onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech: Speech ended")
            _state.value =
                _state.value.copy(isRecording = false) // Update state when speech ends naturally
        }

        override fun onError(error: Int) {
            Log.e(TAG, "onError: Speech recognition error occurred with error code $error")
            _state.value =
                _state.value.copy(isRecording = false) // Update state in case of an error
        }

        override fun onResults(results: Bundle?) {
            Log.d(TAG, "onResults: Recognition results available")
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    questionAnswerResponses = _state.value.questionAnswerResponses + QuestionResponse(
                        question = _state.value.questions.getOrNull(_state.value.currentQuestionIndex)
                            ?: "NA",
                        answer = matches[0]
                    ),
                    currentPartialText = "" // Clear partial text when final result is received
                )
            }
            _state.value =
                _state.value.copy(isRecording = false) // Update state after receiving results
        }

        override fun onPartialResults(partialResults: Bundle?) {
            Log.d(TAG, "onPartialResults: Partial recognition results available")
            val partialMatches =
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!partialMatches.isNullOrEmpty()) {
                _state.value = _state.value.copy(
                    currentPartialText = partialMatches[0] // Display the first partial result
                )
            }
        }


        override fun onEvent(eventType: Int, params: Bundle?) {
            Log.d(TAG, "onEvent: Event occurred, type: $eventType")
        }
    }

    fun generateQuestions() {
        _state.value.jobData?.let {
            makeApiCall({
                api.getInterviewQuestions(it.toJobPosting())
            }, preExecuting = {
                _state.value = _state.value.copy(isGeneratingQuestions = true)
            }, onDoneExecuting = {
                _state.value = _state.value.copy(isGeneratingQuestions = false)
            }) { response ->
                _state.value = _state.value.copy(
                    questions = response.questions
                )
            }
        }
    }

    fun startRecording() {
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
        speechRecognizer.stopListening()
    }

    fun moveToNextQuestion() {
        _state.value = _state.value.copy(
            currentQuestionIndex = _state.value.currentQuestionIndex + 1
        )
        startRecording()
    }

    fun getEvaluationResult() {
        _state.value.jobData?.let {
            makeApiCall({
                api.evaluateInterview(
                    InterviewEvaluationRequest(
                        job = it.toJobPosting(),
                        questionsResponses = _state.value.questionAnswerResponses
                    )
                )
            }, preExecuting = {
                _state.value = _state.value.copy(isEvaluating = true)
            }, onDoneExecuting = {
                _state.value = _state.value.copy(isEvaluating = false)
            }) { response ->
                _state.value = _state.value.copy(evaluationResult = response.evaluation)
            }
        }
    }
}
