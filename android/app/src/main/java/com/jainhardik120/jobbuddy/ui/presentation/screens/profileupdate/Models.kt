package com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate

import kotlinx.serialization.Serializable

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
