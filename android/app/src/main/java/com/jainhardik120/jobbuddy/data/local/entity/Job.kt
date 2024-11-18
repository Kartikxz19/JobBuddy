package com.jainhardik120.jobbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawDescription: String,
    val role: String,
    val experience: String,
    val skills: List<String>,
    val description: String,
    val company : String
)