package com.jainhardik120.jobbuddy.data.local

import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.jainhardik120.jobbuddy.data.local.entity.*

@androidx.room.Database(
    entities = [Job::class, StudyPlan::class, FlashCard::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class JBDatabase : RoomDatabase() {
    abstract val dao: JBDao
}