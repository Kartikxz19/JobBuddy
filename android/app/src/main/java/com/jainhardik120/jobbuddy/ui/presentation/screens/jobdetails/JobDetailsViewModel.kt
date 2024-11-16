package com.jainhardik120.jobbuddy.ui.presentation.screens.jobdetails

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.jainhardik120.jobbuddy.data.dto.JobPosting
import com.jainhardik120.jobbuddy.data.local.JBDatabase
import com.jainhardik120.jobbuddy.data.local.entity.Job
import com.jainhardik120.jobbuddy.data.local.entity.StudyPlan
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.presentation.AppRoutes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
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

    fun generateTailoredResume() {
        _state.value.jobData?.let {
            makeApiCall({
                api.generateTailoredResume(jobData = it.toJobPosting())
            }) {

            }
        }
    }

}

data class JobDetailsScreenState(
    val studyPlan: List<StudyPlan> = emptyList(),
    val jobData: Job? = null
)

fun Job.toJobPosting(): JobPosting {
    return JobPosting(role, experience, skills, description)
}