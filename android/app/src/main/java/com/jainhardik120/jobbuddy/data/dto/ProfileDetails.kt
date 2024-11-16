package com.jainhardik120.jobbuddy.data.dto

import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Achievement
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ContactDetail
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Education
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Experience
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.ProfileLink
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Project
import com.jainhardik120.jobbuddy.ui.presentation.screens.profileupdate.Skill
import kotlinx.serialization.Serializable

@Serializable
data class ProfileDetails(
    val achievements: List<Achievement>,
    val contactDetails: List<ContactDetail>,
    val education: List<Education>,
    val experience: List<Experience>,
    val profileLinks: List<ProfileLink>,
    val projects: List<Project>,
    val skills: List<Skill>
)