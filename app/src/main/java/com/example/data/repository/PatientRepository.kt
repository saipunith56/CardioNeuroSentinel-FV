package com.example.data.repository

import com.example.data.local.AssessmentDao
import com.example.data.local.PatientDao
import com.example.data.model.Patient
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao,
    private val assessmentDao: AssessmentDao? = null
) {
    val allPatients: Flow<List<Patient>> = patientDao.getAllPatients()
    val patientCount: Flow<Int> = patientDao.getPatientCount()
    val activeCaseCount: Flow<Int> = patientDao.getActiveCaseCount()

    suspend fun getPatientById(id: Long): Patient? = patientDao.getPatientById(id)
    suspend fun getPatientByMrn(mrn: String): Patient? = patientDao.getPatientByMrn(mrn)
    suspend fun insertPatient(patient: Patient): Long = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun deletePatient(id: Long) {
        patientDao.deletePatientById(id)
        assessmentDao?.deleteAssessmentsForPatient(id)
    }
}
