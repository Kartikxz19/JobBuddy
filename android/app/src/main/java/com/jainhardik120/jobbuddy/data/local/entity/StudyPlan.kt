package com.jainhardik120.jobbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_plan")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val skill: String,
    val status: String,
    val flashCards: List<FlashCard>
)