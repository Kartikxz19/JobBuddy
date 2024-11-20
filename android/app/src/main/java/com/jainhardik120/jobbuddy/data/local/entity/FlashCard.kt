package com.jainhardik120.jobbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "bookmark_flashcards")
@Serializable
data class FlashCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val skill: String,
    val answer: String,
    val question: String
)