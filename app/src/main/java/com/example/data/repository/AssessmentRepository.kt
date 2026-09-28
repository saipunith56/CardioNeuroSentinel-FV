package com.example.data.repository

import com.example.data.local.AssessmentDao
import com.example.data.model.Assessment
import kotlinx.coroutines.flow.Flow

class AssessmentRepository(private val assessmentDao: AssessmentDao) {
    val allAssessments: Flow<List<Assessment>> = assessmentDao.getAllAssessments()
    val latestAssessment: Flow<Assessment?> = assessmentDao.getLatestAssessment()
    val totalCount: Flow<Int> = assessmentDao.getAssessmentCount()
    val highRiskCount: Flow<Int> = assessmentDao.getHighRiskCount()
    val moderateRiskCount: Flow<Int> = assessmentDao.getModerateRiskCount()
    val lowRiskCount: Flow<Int> = assessmentDao.getLowRiskCount()

    fun getAssessmentsForPatient(patientId: Long): Flow<List<Assessment>> =
        assessmentDao.getAssessmentsForPatient(patientId)

    suspend fun getAssessmentById(id: Long): Assessment? = assessmentDao.getAssessmentById(id)
    suspend fun insertAssessment(assessment: Assessment): Long = assessmentDao.insertAssessment(assessment)
    suspend fun deleteAssessment(id: Long) = assessmentDao.deleteAssessmentById(id)
    suspend fun deleteAssessmentsForPatient(patientId: Long) = assessmentDao.deleteAssessmentsForPatient(patientId)
}
