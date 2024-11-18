package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.FlashCard
import com.jainhardik120.jobbuddy.data.local.entity.StudyPlan
import com.jainhardik120.jobbuddy.data.local.entity.toJobPosting
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.UiEvent
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CoroutineScope
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
                saveFileToDownloads(
                    context,
                    viewModelScope,
                    response
                ) { uri, mimeType ->
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, mimeType)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Open with"))
                }
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

    fun bookMarkFlashCard(card: FlashCard) {
        viewModelScope.launch {
            jbDatabase.dao.upsertFlashCard(card)
        }
    }
}

fun saveFileToDownloads(
    context: Context,
    scope: CoroutineScope,
    response: HttpResponse,
    onSave: (Uri?, String?) -> Unit
) {
    val mimeType = response.headers["Content-Type"]
    val contentDisposition = response.headers["Content-Disposition"]
    val fileName =
        contentDisposition?.substringAfter("filename=")?.replace("\"", "") ?: "default_filename"
    val result = getDownloadsFileOutputStream(context, fileName, mimeType.toString())
    val outputStream = result?.first
    val fileUri = result?.second
    if (outputStream != null) {
        scope.launch {
            response.bodyAsChannel().apply {
                outputStream.use { outputStream ->
                    while (!isClosedForRead) {
                        val buffer = readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                        while (!buffer.exhausted()) {
                            outputStream.write(buffer.readByteArray())
                        }
                    }
                    onSave(fileUri, mimeType)
                }
            }
        }
    }
}

@SuppressLint("Recycle")
fun getDownloadsFileOutputStream(
    context: Context,
    fileName: String,
    mimeType: String
): Pair<OutputStream?, Uri?>? {
    val resolver = context.contentResolver
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
    }
    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
    return uri?.let { resolver.openOutputStream(it) to uri }
}