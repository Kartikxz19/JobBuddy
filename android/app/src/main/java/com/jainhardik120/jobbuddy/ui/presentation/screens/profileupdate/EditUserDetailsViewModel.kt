package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.jainhardik120.jobbuddy.data.dto.ProfileDetails
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.data.remote.Resume
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.UiEvent
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
class EditUserDetailsViewModel @Inject constructor(
    private val api: JobBuddyAPI
) : BaseViewModel() {
    private val _state = mutableStateOf(EditUserDetailsState())
    val state: State<EditUserDetailsState> = _state

    private var uploadJob: Job? = null

    init {
        getInitialData()
        updateResumeList()
    }

    private fun updateResumeList() {
        makeApiCall({
            api.listUploadedResumes()
        }) {
            _state.value = _state.value.copy(
                userResumes = it.resumes
            )
        }
    }

    fun uploadFile(contentUri: Uri) {
        uploadJob = api.uploadResume(contentUri)
            .onStart {
                _state.value = _state.value.copy(
                    isUploading = true,
                    progress = 0f
                )
            }
            .onEach { progressUpdate ->
                _state.value = _state.value.copy(
                    progress = progressUpdate.bytesSent / progressUpdate.totalBytes.toFloat()
                )
            }
            .onCompletion { cause ->
                if (cause == null) {
                    _state.value = _state.value.copy(
                        isUploading = false
                    )
                    updateResumeList()
                } else if (cause is CancellationException) {
                    _state.value = _state.value.copy(
                        isUploading = false,
                        progress = 0f
                    )
                    sendUiEvent(UiEvent.ShowToast("Upload was cancelled"))
                }
            }
            .catch { cause ->
                val message = when (cause) {
                    is OutOfMemoryError -> "File too large!"
                    is FileNotFoundException -> "File not found!"
                    is UnresolvedAddressException -> "No internet!"
                    else -> "Something went wrong!"
                }
                sendUiEvent(UiEvent.ShowToast(message))
                _state.value = _state.value.copy(
                    isUploading = false
                )
            }
            .launchIn(viewModelScope)
    }

    fun cancelUpload() {
        uploadJob?.cancel()
    }

    private fun getInitialData() {
        makeApiCall({
            api.getProfileDetails()
        }) { response ->
            _state.value = EditUserDetailsState(
                skills = response.skills.map { it.toInputModel() },
                projects = response.projects.map { it.toInputModel() },
                experience = response.experience.map { it.toInputModel() },
                achievements = response.achievements.map { it.toInputModel() },
                education = response.education.map { it.toInputModel() },
                profileLinks = response.profileLinks.map { it.toInputModel() },
                contactDetails = response.contactDetails.map { it.toInputModel() }
            )
        }
    }

    private fun saveUserDetails() {
        makeApiCall({
            api.updateProfileDetails(
                data = ProfileDetails(
                    skills = _state.value.skills.map { it.toData() },
                    projects = _state.value.projects.map { it.toData() },
                    experience = _state.value.experience.map { it.toData() },
                    achievements = _state.value.achievements.map { it.toData() },
                    education = _state.value.education.map { it.toData() },
                    profileLinks = _state.value.profileLinks.map { it.toData() },
                    contactDetails = _state.value.contactDetails.map { it.toData() }
                )
            )
        }) { response ->
            sendUiEvent(UiEvent.ShowToast(response.message))
        }
    }

    fun onEvent(event: EditUserDetailsEvent) {
        when (event) {
            is EditUserDetailsEvent.Added -> updateList(_state.value, event.item)
            is EditUserDetailsEvent.Removed -> removeFromList(_state.value, event.index)
            is EditUserDetailsEvent.Updated -> updateItemAtIndex(_state.value, event)
            is EditUserDetailsEvent.SaveClicked -> saveUserDetails()
        }
    }

    private fun updateList(state: EditUserDetailsState, item: InputModel) {
        _state.value = when (item) {
            is InputModel.Skill -> state.copy(skills = state.skills + item)
            is InputModel.Project -> state.copy(projects = state.projects + item)
            is InputModel.Experience -> state.copy(experience = state.experience + item)
            is InputModel.Achievement -> state.copy(achievements = state.achievements + item)
            is InputModel.Education -> state.copy(education = state.education + item)
            is InputModel.ProfileLink -> state.copy(profileLinks = state.profileLinks + item)
            is InputModel.ContactDetail -> state.copy(contactDetails = state.contactDetails + item)
        }
    }

    private fun removeFromList(state: EditUserDetailsState, index: Int) {
        _state.value = when (index) {
            in state.skills.indices -> state.copy(
                skills = state.skills.toMutableList().also { it.removeAt(index) }.toList()
            )

            in state.projects.indices -> state.copy(
                projects = state.projects.toMutableList().also { it.removeAt(index) }.toList()
            )

            in state.experience.indices -> state.copy(
                experience = state.experience.toMutableList().also { it.removeAt(index) }.toList()
            )

            in state.achievements.indices -> state.copy(
                achievements = state.achievements.toMutableList().also { it.removeAt(index) }
                    .toList()
            )

            in state.education.indices -> state.copy(
                education = state.education.toMutableList().also { it.removeAt(index) }.toList()
            )

            in state.profileLinks.indices -> state.copy(
                profileLinks = state.profileLinks.toMutableList().also { it.removeAt(index) }
                    .toList()
            )

            in state.contactDetails.indices -> state.copy(
                contactDetails = state.contactDetails.toMutableList().also { it.removeAt(index) }
                    .toList()
            )

            else -> state
        }
    }

    private fun updateItemAtIndex(
        state: EditUserDetailsState,
        event: EditUserDetailsEvent.Updated
    ) {
        _state.value = when (event.item) {
            is InputModel.Project -> state.copy(projects = state.projects.toMutableList().also {
                it[event.index] = event.item
            }.toList())

            is InputModel.Experience -> state.copy(
                experience = state.experience.toMutableList().also {
                    it[event.index] = event.item
                }.toList()
            )

            is InputModel.Achievement -> state.copy(
                achievements = state.achievements.toMutableList().also {
                    it[event.index] = event.item
                }.toList()
            )

            is InputModel.Education -> state.copy(education = state.education.toMutableList().also {
                it[event.index] = event.item
            }.toList())

            is InputModel.ProfileLink -> state.copy(
                profileLinks = state.profileLinks.toMutableList().also {
                    it[event.index] = event.item
                }.toList()
            )

            is InputModel.ContactDetail -> state.copy(
                contactDetails = state.contactDetails.toMutableList().also {
                    it[event.index] = event.item
                }.toList()
            )

            is InputModel.Skill -> state.copy(skills = state.skills.toMutableList().also {
                it[event.index] = event.item
            }.toList())
        }
    }

}

sealed class EditUserDetailsEvent {
    data class Added(val item: InputModel) : EditUserDetailsEvent()
    data class Removed(val index: Int) : EditUserDetailsEvent()
    data class Updated(val index: Int, val item: InputModel) : EditUserDetailsEvent()
    object SaveClicked : EditUserDetailsEvent()
}

data class EditUserDetailsState(
    val skills: List<InputModel.Skill> = emptyList(),
    val projects: List<InputModel.Project> = emptyList(),
    val experience: List<InputModel.Experience> = emptyList(),
    val achievements: List<InputModel.Achievement> = emptyList(),
    val education: List<InputModel.Education> = emptyList(),
    val profileLinks: List<InputModel.ProfileLink> = emptyList(),
    val contactDetails: List<InputModel.ContactDetail> = emptyList(),
    val userResumes: List<Resume> = emptyList(),
    val isUploading: Boolean = false,
    val progress: Float = 0f
)

