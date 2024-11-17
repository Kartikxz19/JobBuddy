package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.StudyPlan
import com.jainhardik120.jobbuddy.data.local.entity.toJobPosting
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.UiEvent
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import java.io.OutputStream
import javax.inject.Inject

@HiltViewModel
class JobDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val api: JobBuddyAPI,
    private val jbDatabase: JBDatabase
) : BaseViewModel() {

    private val screenDetails: AppRoutes.JobDetailsScreen = savedStateHandle.toRoute()

    private val _state = mutableStateOf(JobDetailsScreenState())
    val state: State<JobDetailsScreenState> = _state

    init {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                jobData = jbDatabase.dao.getJobDetails(screenDetails.jobId)
            )
        }
        jbDatabase.dao.getJobStudyPlan(screenDetails.jobId).onEach {
            _state.value = _state.value.copy(studyPlan = it)
        }.launchIn(viewModelScope)
    }

    fun generateStudyPlan() {
        _state.value.jobData?.let {
            makeApiCall({
                api.generateFlashCards(it.toJobPosting())
            }) { response ->
                viewModelScope.launch {
                    jbDatabase.dao.deleteStudyPlan(screenDetails.jobId)
                    response.studyPlan.forEach { studyPlan ->
                        jbDatabase.dao.upsertJobStudyPlan(
                            StudyPlan(
                                jobId = screenDetails.jobId,
                                skill = studyPlan.skill,
                                status = studyPlan.status,
                                flashCards = studyPlan.flashCards
                            )
                        )
                    }
                }
            }
        }
    }


    private fun getDownloadsFileOutputStream(context: Context, fileName: String): OutputStream? {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        return uri?.let { resolver.openOutputStream(it) }
    }


    fun generateTailoredResume(context: Context) {
        _state.value.jobData?.let {
            makeApiCall(
                {
                    api.generateTailoredResume(jobData = it.toJobPosting())
                },
                preExecuting = { sendUiEvent(UiEvent.ShowToast("Generating your resume")) },
                onError = {
                    sendUiEvent(
                        UiEvent.ShowToast(it.error)
                    )
                }) { response ->
                val outputStream = getDownloadsFileOutputStream(context, "tailored_resume.pdf")
                if (outputStream != null) {
                    viewModelScope.launch {
                        response.bodyAsChannel().apply {
                            outputStream.use { outputStream ->
                                while (!isClosedForRead) {
                                    val buffer = readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                                    while (!buffer.exhausted()) {
                                        outputStream.write(buffer.readByteArray())
                                    }
                                }
                            }
                        }
                    }
                }
                sendUiEvent(UiEvent.ShowToast("File Downloaded"))
            }
        }
    }

    fun setEvaluationSheet(boolean: Boolean) {
        if (!boolean) {
            _state.value = _state.value.copy(openEvaluationSheet = false)
        } else {
            if (_state.value.profileEvaluation.isNotEmpty()) {
                _state.value = _state.value.copy(openEvaluationSheet = true)
            } else {
                checkProfileScore()
            }
        }
    }

    fun takeInterviewButton() {
        sendUiEvent(UiEvent.Navigate(AppRoutes.VirtualInterviewScreen(jobId = screenDetails.jobId)))
    }

    fun checkProfileScore() {
        _state.value.jobData?.let {
            makeApiCall(
                {
                    api.checkResumeScore(it.toJobPosting())
                },
                preExecuting = { sendUiEvent(UiEvent.ShowToast("Evaluating your profile")) }) { response ->
                _state.value = _state.value.copy(profileEvaluation = response.message)
                setEvaluationSheet(true)
            }
        }
    }

}