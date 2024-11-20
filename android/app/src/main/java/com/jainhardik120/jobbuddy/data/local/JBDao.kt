package com.jainhardik120.jobbuddy.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.jainhardik120.jobbuddy.data.local.entity.FlashCard
import com.jainhardik120.jobbuddy.data.local.entity.Job
import com.jainhardik120.jobbuddy.data.local.entity.StudyPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface JBDao {

    @Query("SELECT * FROM job")
    fun getAllJobs(): Flow<List<Job>>

    @Query("SELECT * FROM job WHERE id=:jobId")
    suspend fun getJobDetails(jobId: Int): Job

    @Upsert
    suspend fun upsertJob(job: Job)

    @Query("DELETE FROM study_plan WHERE jobId=:jobId")
    suspend fun deleteStudyPlan(jobId: Int)

    @Query("SELECT * FROM study_plan WHERE jobId=:jobId")
    fun getJobStudyPlan(jobId: Int): Flow<List<StudyPlan>>

    @Upsert
    suspend fun upsertJobStudyPlan(studyPlan: StudyPlan)

    @Upsert
    suspend fun upsertFlashCard(flashCard: FlashCard)

    @Query("SELECT * FROM bookmark_flashcards")
    fun getFlashCards() : Flow<List<FlashCard>>
}