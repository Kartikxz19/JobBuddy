package com.jainhardik120.jobbuddy.ui.presentation.screens.home

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.jainhardik120.jobbuddy.data.KeyValueStorage
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.Job
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UploadFileViewModel @Inject constructor(
    private val api: JobBuddyAPI,
    private val jbDatabase: JBDatabase,
    private val keyValueStorage: KeyValueStorage
) : BaseViewModel() {

    private val _state = mutableStateOf(UploadState())
    val state: State<UploadState> = _state

    init {
        jbDatabase.dao.getAllJobs().onEach {
            _state.value = _state.value.copy(jobList = it)
        }.launchIn(viewModelScope)
    }

    fun setJobSheet(boolean: Boolean) {
        _state.value = _state.value.copy(jobSheetOpened = boolean)
    }

    fun logout() {
        keyValueStorage.removeValue(KeyValueStorage.TOKEN_KEY)
    }

    fun createJob(jobDescription: String) {
        makeApiCall({
            api.simplifyJobData(jobDescription)
        }, preExecuting = {
            _state.value = _state.value.copy(isCreatingJobData = true)
        }, onDoneExecuting = {
            _state.value = _state.value.copy(isCreatingJobData = false)
        }) { response ->
            viewModelScope.launch {
                jbDatabase.dao.upsertJob(
                    Job(
                        rawDescription = jobDescription,
                        role = response.role,
                        experience = response.experience,
                        skills = response.skills,
                        description = response.description
                    )
                )
            }
        }
    }
}