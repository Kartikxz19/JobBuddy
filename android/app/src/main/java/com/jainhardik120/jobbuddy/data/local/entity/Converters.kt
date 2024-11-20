package com.jainhardik120.jobbuddy.data.local.entity

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    @TypeConverter
    fun fromSkillsList(skills: List<String>): String {
        return Json.Default.encodeToString(skills)
    }

    @TypeConverter
    fun toSkillsList(skillsJson: String): List<String> {
        return Json.Default.decodeFromString(skillsJson)
    }

    @TypeConverter
    fun fromFlashCardsList(flashCards: List<FlashCard>): String {
        return Json.Default.encodeToString(flashCards)
    }

    @TypeConverter
    fun toFlashCardsList(json: String): List<FlashCard> {
        return Json.Default.decodeFromString(json)
    }

}