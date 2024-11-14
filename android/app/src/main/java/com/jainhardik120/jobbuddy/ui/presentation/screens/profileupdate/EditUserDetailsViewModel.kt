package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.jainhardik120.jobbuddy.data.remote.JobBuddyAPI
import com.jainhardik120.jobbuddy.data.remote.ProfileDetails
import com.jainhardik120.jobbuddy.ui.BaseViewModel
import com.jainhardik120.jobbuddy.ui.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.serialization.Serializable
import javax.inject.Inject


@Serializable
data class Skill(
    val skill: String,
    val level: Int
)

@Serializable
data class Project(
    val name: String,
    val techStack: String,
    val demoLink: String,
    val startDate: InputType.Date,
    val endDate: InputType.Date,
    val description: String
)

@Serializable
data class Experience(
    val title: String,
    val company: String,
    val startDate: InputType.Date,
    val endDate: InputType.Date,
    val description: String
)

@Serializable
data class Achievement(
    val title: String,
    val description: String
)


@Serializable
data class Education(
    val institution: String,
    val degree: String,
    val startDate: InputType.Date,
    val endDate: InputType.Date
)

@Serializable
data class ProfileLink(
    val platform: String,
    val url: String
)

@Serializable
data class ContactDetail(
    val type: String,
    val value: String
)

fun Skill.toInputModel(): InputModel.Skill {
    return InputModel.Skill(skill = this.skill, level = this.level)
}

fun Project.toInputModel(): InputModel.Project {
    return InputModel.Project(
        name = this.name,
        techStack = this.techStack,
        demoLink = this.demoLink,
        startDate = this.startDate,
        endDate = this.endDate,
        description = this.description
    )
}

fun Experience.toInputModel(): InputModel.Experience {
    return InputModel.Experience(
        title = this.title,
        company = this.company,
        startDate = this.startDate,
        endDate = this.endDate,
        description = this.description
    )
}

fun Achievement.toInputModel(): InputModel.Achievement {
    return InputModel.Achievement(
        title = this.title,
        description = this.description
    )
}

fun Education.toInputModel(): InputModel.Education {
    return InputModel.Education(
        institution = this.institution,
        degree = this.degree,
        startDate = this.startDate,
        endDate = this.endDate
    )
}

fun ProfileLink.toInputModel(): InputModel.ProfileLink {
    return InputModel.ProfileLink(
        platform = this.platform,
        url = this.url
    )
}

fun ContactDetail.toInputModel(): InputModel.ContactDetail {
    return InputModel.ContactDetail(
        type = this.type,
        value = this.value
    )
}

fun InputModel.Skill.toData(): Skill {
    return Skill(
        (this.fields["skill"]?.type as InputType.Text).value,
        (this.fields["level"]?.type as InputType.Number).value
    )
}

// Mapper functions for Project
fun InputModel.Project.toData(): Project {
    return Project(
        (this.fields["name"]?.type as InputType.Text).value,
        (this.fields["techStack"]?.type as InputType.Text).value,
        (this.fields["demoLink"]?.type as InputType.Text).value,
        (this.fields["startDate"]?.type as InputType.Date),
        (this.fields["endDate"]?.type as InputType.Date),
        (this.fields["description"]?.type as InputType.Text).value
    )
}

// Mapper functions for Experience
fun InputModel.Experience.toData(): Experience {
    return Experience(
        (this.fields["title"]?.type as InputType.Text).value,
        (this.fields["company"]?.type as InputType.Text).value,
        (this.fields["startDate"]?.type as InputType.Date),
        (this.fields["endDate"]?.type as InputType.Date),
        (this.fields["description"]?.type as InputType.Text).value
    )
}

// Mapper functions for Achievement
fun InputModel.Achievement.toData(): Achievement {
    return Achievement(
        (this.fields["title"]?.type as InputType.Text).value,
        (this.fields["description"]?.type as InputType.Text).value
    )
}

// Mapper functions for Education
fun InputModel.Education.toData(): Education {
    return Education(
        (this.fields["institution"]?.type as InputType.Text).value,
        (this.fields["degree"]?.type as InputType.Text).value,
        (this.fields["startDate"]?.type as InputType.Date),
        (this.fields["endDate"]?.type as InputType.Date)
    )
}

// Mapper functions for ProfileLink
fun InputModel.ProfileLink.toData(): ProfileLink {
    return ProfileLink(
        (this.fields["platform"]?.type as InputType.Text).value,
        (this.fields["url"]?.type as InputType.Text).value
    )
}

// Mapper functions for ContactDetail
fun InputModel.ContactDetail.toData(): ContactDetail {
    return ContactDetail(
        (this.fields["type"]?.type as InputType.Text).value,
        (this.fields["value"]?.type as InputType.Text).value
    )
}

@HiltViewModel
class EditUserDetailsViewModel @Inject constructor(
    private val api: JobBuddyAPI
) : BaseViewModel() {
    private val _state = mutableStateOf(EditUserDetailsState())
    val state: State<EditUserDetailsState> = _state

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


    init {
        getInitialData()
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
        }) {response->
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
    val contactDetails: List<InputModel.ContactDetail> = emptyList()
)

