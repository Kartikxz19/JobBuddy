package com.jainhardik120.jobbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.jainhardik120.jobbuddy.data.dto.JobPosting
import com.jainhardik120.jobbuddy.data.remote.FlashCard
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "job")
data class Job(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rawDescription: String,
    val role: String,
    val experience: String,
    val skills: List<String>,
    val description: String
)

fun Job.toJobPosting(): JobPosting {
    return JobPosting(role, experience, skills, description)
}

@Entity(tableName = "study_plan")
data class StudyPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jobId: Int,
    val skill: String,
    val status: String,
    val flashCards: List<FlashCard>
)

class Converters {

    @TypeConverter
    fun fromSkillsList(skills: List<String>): String {
        return Json.encodeToString(skills)
    }

    @TypeConverter
    fun toSkillsList(skillsJson: String): List<String> {
        return Json.decodeFromString(skillsJson)
    }

    @TypeConverter
    fun fromFlashCardsList(flashCards: List<FlashCard>): String {
        return Json.encodeToString(flashCards)
    }

    @TypeConverter
    fun toFlashCardsList(json: String): List<FlashCard> {
        return Json.decodeFromString(json)
    }

}