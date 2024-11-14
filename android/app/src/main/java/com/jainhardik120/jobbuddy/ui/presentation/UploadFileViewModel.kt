package com.jainhardik120.jobbuddy.ui.presentation

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.data.remote.ResumeScoreRequest
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import okio.FileNotFoundException
import javax.inject.Inject

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val api: JobBuddyAPI
) : BaseViewModel() {
    var state = mutableStateOf(UploadState())
        private set

    private var uploadJob: Job? = null

    companion object{
        private const val TAG = "Upload file view model"
    }


    fun checkStatus(){
        makeApiCall({
            api.getResumeStatus()
        }){result->
            Log.d(TAG, "checkStatus: $result")
        }
    }

    fun uploadFile(contentUri: Uri) {
        uploadJob = api.uploadResume(contentUri)
            .onStart {
                state.value = state.value.copy(
                    isUploading = true,
                    isUploadComplete = false,
                    errorMessage = null,
                    progress = 0f
                )
            }
            .onEach { progressUpdate ->
                state.value = state.value.copy(
                    progress = progressUpdate.bytesSent / progressUpdate.totalBytes.toFloat()
                )
            }
            .onCompletion { cause ->
                if (cause == null) {
                    state.value = state.value.copy(
                        isUploading = false,
                        isUploadComplete = true
                    )
                } else if (cause is CancellationException) {
                    state.value = state.value.copy(
                        isUploading = false,
                        errorMessage = "The upload was cancelled!",
                        isUploadComplete = false,
                        progress = 0f
                    )
                }
            }
            .catch { cause ->
                val message = when (cause) {
                    is OutOfMemoryError -> "File too large!"
                    is FileNotFoundException -> "File not found!"
                    is UnresolvedAddressException -> "No internet!"
                    else -> "Something went wrong!"
                }
                state.value = state.value.copy(
                    isUploading = false,
                    errorMessage = message
                )
            }
            .launchIn(viewModelScope)
    }

    fun cancelUpload() {
        uploadJob?.cancel()
    }

    fun checkResumeScore(value  : String) {
        makeApiCall({
            api.checkResumeScore(ResumeScoreRequest(jobDescription = value))
        }){response->
            state.value = state.value.copy(
                markdownText = response.message
            )
            Log.d(TAG, "checkResumeScore: ${response.message}")
        }
    }
}