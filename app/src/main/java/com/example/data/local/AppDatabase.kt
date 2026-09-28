package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import com.example.data.model.ModelExecutionStatus
import com.example.data.model.Patient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Patient::class, Assessment::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun assessmentDao(): AssessmentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cardioneuro_sentinel.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed EXACTLY TWO initial demonstration patients as requested
                        CoroutineScope(Dispatchers.IO).launch {
                            val database = getInstance(context)
                            val johan = Patient(
                                mrn = "MRN-988713",
                                name = "johan libert",
                                age = 30,
                                gender = "Male",
                                bloodPressure = "115/76",
                                cholesterol = 170,
                                bloodGroup = "O+",
                                conditionTag = "Hypertension & Atherosclerosis",
                                activeCase = true
                            )
                            val pId1 = database.patientDao().insertPatient(johan)

                            val elena = Patient(
                                mrn = "MRN-419208",
                                name = "elena rostova",
                                age = 58,
                                gender = "Female",
                                bloodPressure = "138/88",
                                cholesterol = 210,
                                bloodGroup = "A+",
                                conditionTag = "Transient Ischemic Attack Monitoring",
                                activeCase = true
                            )
                            database.patientDao().insertPatient(elena)

                            // Initial seed assessment: EXACTLY ONE completed assessment (for Johan Libert)
                            // Dashboard calculates 2 Patients, 1 Completed Assessment, 1 Moderate Case, 0 High, 0 Low.
                            val initialAssessment = Assessment(
                                patientId = pId1,
                                patientName = "johan libert",
                                mrn = "MRN-988713",
                                timestamp = System.currentTimeMillis() - 3600000,
                                age = 30.0,
                                bmi = 22.5,
                                systolicBp = 115.0,
                                diastolicBp = 76.0,
                                heartRate = 88.0,
                                cholesterol = 170.0,
                                fastingGlucose = 115.0,
                                troponin = 0.0,
                                nihss = 0,
                                isSmoker = true,
                                familyCvHistory = true,
                                familyStrokeHistory = true,
                                chiefComplaint = "Acute palpitations, dizziness, and mild facial numbness",
                                mriSourceType = "PRESET_DWI",
                                mriUri = "sample_mri",
                                mriUserDeclared = true,
                                ecgSourceType = "IMAGE",
                                ecgUri = "sample_ecg",
                                eegSourceType = "IMAGE",
                                eegUri = "sample_eeg",
                                vitalsStatus = ModalityValidationStatus.REAL_ENTERED,
                                mriValidationStatus = ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED,
                                ecgValidationStatus = ModalityValidationStatus.UNSUPPORTED,
                                eegValidationStatus = ModalityValidationStatus.UNSUPPORTED,
                                ds1Status = ModelExecutionStatus.EXECUTED,
                                ds1HemorrhagicProb = 0.139,
                                ds1IschemicProb = 0.847,
                                ds1NormalProb = 0.014,
                                ds1SummaryLabel = "Evaluated MRI Scan: Low Cerebrovascular Stroke Risk Profile [ON-DEVICE AI INFERENCE]",
                                ds2Status = ModelExecutionStatus.EXECUTED,
                                ds2Probability = 0.16,
                                ds2ImputationBacked = true,
                                ds3Status = ModelExecutionStatus.EXECUTED,
                                ds3Probability = 0.007,
                                ds4Status = ModelExecutionStatus.NOT_EXECUTED,
                                ds4RejectionReason = "ECG Image uploaded - serialized waveform reconstruction/inference is unavailable. Quantitative ECG model expects a raw 12-lead signal (10s @100Hz). Heuristic/model inference was not executed.",
                                ds5Status = ModelExecutionStatus.NOT_EXECUTED,
                                ds5RejectionReason = "EEG Image uploaded - serialized signal reconstruction/inference is unavailable. Quantitative EEG model expects a raw 23-channel EEG signal (1s @256Hz). Signal model inference was not executed.",
                                combinedRiskScorePct = null,
                                fusionStatusMessage = "NOT AVAILABLE — No validated joint fusion model",
                                toastSubtype = "Stroke of Undetermined Etiology (SUE - Incomplete Imaging) [RULE_BASED/HEURISTIC]",
                                gnnCrosstalk = null,
                                clinicalRiskCategory = "MODERATE",
                                syncId = "893892015"
                            )
                            database.assessmentDao().insertAssessment(initialAssessment)
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
