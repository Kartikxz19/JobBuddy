package com.jainhardik120.jobbuddy.data.local.entity

import com.jainhardik120.jobbuddy.data.dto.JobPosting

fun Job.toJobPosting(): JobPosting {
    return JobPosting(role, experience, skills, description)
}