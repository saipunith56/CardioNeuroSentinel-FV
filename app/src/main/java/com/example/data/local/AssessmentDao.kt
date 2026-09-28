package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Assessment
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {
    @Query("SELECT * FROM assessments ORDER BY timestamp DESC")
    fun getAllAssessments(): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getAssessmentsForPatient(patientId: Long): Flow<List<Assessment>>

    @Query("SELECT * FROM assessments WHERE id = :id LIMIT 1")
    suspend fun getAssessmentById(id: Long): Assessment?

    @Query("SELECT * FROM assessments ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAssessment(): Flow<Assessment?>

    @Query("SELECT COUNT(*) FROM assessments")
    fun getAssessmentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assessments WHERE clinicalRiskCategory = 'HIGH'")
    fun getHighRiskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assessments WHERE clinicalRiskCategory = 'MODERATE'")
    fun getModerateRiskCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM assessments WHERE clinicalRiskCategory = 'LOW'")
    fun getLowRiskCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: Assessment): Long

    @Query("DELETE FROM assessments WHERE id = :id")
    suspend fun deleteAssessmentById(id: Long)

    @Query("DELETE FROM assessments WHERE patientId = :patientId")
    suspend fun deleteAssessmentsForPatient(patientId: Long)
}
