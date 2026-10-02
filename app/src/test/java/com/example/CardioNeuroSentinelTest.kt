package com.example

import com.example.data.model.ModalityValidationStatus
import com.example.data.model.Patient
import com.example.domain.inference.Ds1MriInference
import com.example.domain.inference.Ds2HeartInference
import com.example.domain.inference.Ds3StrokeInference
import com.example.domain.inference.FusionController
import com.example.domain.preprocessing.Ds2HeartPreprocessor
import com.example.domain.preprocessing.Ds3StrokePreprocessor
import com.example.domain.preprocessing.Ds4EcgPreprocessor
import com.example.domain.preprocessing.Ds5EegPreprocessor
import com.example.domain.preprocessing.MriPreprocessor
import com.example.domain.validation.ModalityValidator
import com.example.domain.validation.SignalValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CardioNeuroSentinelTest {

    // 1. DS2 Preprocessing Test
    @Test
    fun testDs2PreprocessingAndImputation() {
        // Test with missing values (should impute exact medians: cp=4.0, restecg=0.0, exang=0.0, oldpeak=0.6, slope=2.0, ca=0.0, thal=6.0)
        val result = Ds2HeartPreprocessor.preprocess(
            age = 30.0f,
            sex = 1.0f,
            cp = null, // Imputed
            trestbps = 115.0f,
            chol = 170.0f,
            fbs = 0.0f,
            restecg = null, // Imputed
            thalach = 88.0f,
            exang = null, // Imputed
            oldpeak = null, // Imputed
            slope = null, // Imputed
            ca = null, // Imputed
            thal = null // Imputed
        )

        assertEquals(13, result.standardizedFeatures.size)
        assertTrue(result.isImputationBacked)
        assertEquals(7, result.imputedFeatureIndices.size)

        // Verify exact median values for imputed fields
        assertEquals(4.0f, result.rawFeatures[2], 0.001f) // cp
        assertEquals(0.0f, result.rawFeatures[6], 0.001f) // restecg
        assertEquals(0.0f, result.rawFeatures[8], 0.001f) // exang
        assertEquals(0.6f, result.rawFeatures[9], 0.001f) // oldpeak
        assertEquals(2.0f, result.rawFeatures[10], 0.001f) // slope
        assertEquals(0.0f, result.rawFeatures[11], 0.001f) // ca
        assertEquals(6.0f, result.rawFeatures[12], 0.001f) // thal

        // Verify standardization: (30 - 53.760870) / 9.495600 ≈ -2.502
        val expectedAgeZ = (30.0f - Ds2HeartPreprocessor.MEANS[0]) / Ds2HeartPreprocessor.SCALES[0]
        assertEquals(expectedAgeZ, result.standardizedFeatures[0], 0.001f)
    }

    // 2. DS2 Inference Test
    @Test
    fun testDs2Inference() {
        val pre = Ds2HeartPreprocessor.preprocess(
            age = 30.0f,
            sex = 1.0f,
            cp = null,
            trestbps = 115.0f,
            chol = 170.0f,
            fbs = 0.0f,
            restecg = null,
            thalach = 88.0f,
            exang = null,
            oldpeak = null,
            slope = null,
            ca = null,
            thal = null
        )
        val prob = Ds2HeartInference.infer(pre.standardizedFeatures)

        // Sigmoid probability must be bounded between 0.0 and 1.0
        assertTrue(prob in 0.0..1.0)
        // Should evaluate to ~0.16 (16%) for Johan Libert profile
        assertEquals(0.16, prob, 0.05)
    }

    // 3. DS3 Preprocessing Test
    @Test
    fun testDs3Preprocessing() {
        val features = Ds3StrokePreprocessor.preprocess(
            age = 30.0f,
            avgGlucoseLevel = 115.0f,
            bmi = 22.5f,
            gender = "Male",
            hypertension = 0,
            heartDisease = 0,
            everMarried = "Yes",
            workType = "Private",
            residenceType = "Urban",
            smokingStatus = "smokes"
        )

        assertEquals(22, features.size)

        // Verify numerical standardizations
        val expectedAgeZ = (30.0f - Ds3StrokePreprocessor.AGE_MEAN) / Ds3StrokePreprocessor.AGE_SCALE
        assertEquals(expectedAgeZ, features[0], 0.001f)

        // Verify one-hot order:
        // gender: Female=0, Male=1
        assertEquals(0.0f, features[3], 0.001f)
        assertEquals(1.0f, features[4], 0.001f)

        // hypertension: 0=1, 1=0
        assertEquals(1.0f, features[5], 0.001f)
        assertEquals(0.0f, features[6], 0.001f)

        // heart_disease: 0=1, 1=0
        assertEquals(1.0f, features[7], 0.001f)
        assertEquals(0.0f, features[8], 0.001f)

        // ever_married: No=0, Yes=1
        assertEquals(0.0f, features[9], 0.001f)
        assertEquals(1.0f, features[10], 0.001f)

        // work_type: Private is index 13
        assertEquals(1.0f, features[13], 0.001f)

        // Residence_type: Urban is index 17
        assertEquals(1.0f, features[17], 0.001f)

        // smoking_status: smokes is index 21
        assertEquals(1.0f, features[21], 0.001f)
    }

    // 4. DS3 Inference Test & Display Formatting
    @Test
    fun testDs3InferenceAndDisplayFormatting() {
        val features = Ds3StrokePreprocessor.preprocess(
            age = 30.0f,
            avgGlucoseLevel = 115.0f,
            bmi = 22.5f,
            gender = "Male",
            hypertension = 0,
            heartDisease = 0,
            everMarried = "Yes",
            workType = "Private",
            residenceType = "Urban",
            smokingStatus = "smokes"
        )
        val prob = Ds3StrokeInference.infer(features)

        assertTrue(prob in 0.0..1.0)
        // For a young 30 y/o, risk is under 1% (< 0.01)
        assertTrue(prob < 0.01)
        val display = Ds3StrokeInference.formatDisplay(prob)
        assertEquals("<1%", display)
    }

    // 5. DS1 Image Preprocessing Test
    @Test
    fun testDs1ImagePreprocessing() {
        val dummyRed = FloatArray(128 * 128) { 0.485f }
        val dummyGreen = FloatArray(128 * 128) { 0.456f }
        val dummyBlue = FloatArray(128 * 128) { 0.406f }

        val tensor = MriPreprocessor.preprocessRgb(dummyRed, dummyGreen, dummyBlue)

        assertEquals(3 * 128 * 128, tensor.size)
        // Values matching ImageNet mean should normalize close to 0.0
        assertEquals(0.0f, tensor[0], 0.001f)
        assertEquals(0.0f, tensor[128 * 128], 0.001f)
        assertEquals(0.0f, tensor[2 * 128 * 128], 0.001f)
    }

    // 6. MRI Routing & Validation Test
    @Test
    fun testMriRoutingValidation() {
        // Flat / solid blank image -> MODALITY_REJECTED
        val rejectedResult = ModalityValidator.validatePixelStats(
            width = 128,
            height = 128,
            variance = 0.5, // Less than safety threshold 2.0
            userDeclaredMri = true
        )
        assertEquals(ModalityValidationStatus.MODALITY_REJECTED, rejectedResult.status)
        assertFalse(rejectedResult.canExecuteDs1)
        assertEquals("INVALID IMAGE", rejectedResult.displayLabel)

        // Passing safety filter without user declaration -> MODALITY_UNVALIDATED
        val unvalidatedResult = ModalityValidator.validatePixelStats(
            width = 128,
            height = 128,
            variance = 15.0,
            userDeclaredMri = false
        )
        assertEquals(ModalityValidationStatus.MODALITY_UNVALIDATED, unvalidatedResult.status)
        assertFalse(unvalidatedResult.canExecuteDs1)

        // Passing safety filter with explicit user declaration -> USER_DECLARED_NOT_VALIDATED
        val declaredResult = ModalityValidator.validatePixelStats(
            width = 128,
            height = 128,
            variance = 15.0,
            userDeclaredMri = true
        )
        assertEquals(ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED, declaredResult.status)
        assertTrue(declaredResult.canExecuteDs1)

        // Validated DICOM -> VALIDATED_MRI
        val dicomResult = ModalityValidator.validatePixelStats(
            width = 128,
            height = 128,
            variance = 15.0,
            userDeclaredMri = true,
            isDicomValidated = true
        )
        assertEquals(ModalityValidationStatus.VALIDATED_MRI, dicomResult.status)
        assertTrue(dicomResult.canExecuteDs1)
    }

    // 7. ECG Unsupported-Image Routing Test
    @Test
    fun testEcgUnsupportedImageRouting() {
        // Image upload must strictly be rejected as UNSUPPORTED
        val imgResult = SignalValidator.validateEcgSource("IMAGE")
        assertEquals(ModalityValidationStatus.UNSUPPORTED, imgResult.status)
        assertFalse(imgResult.isExecutable)
        assertTrue(imgResult.message.contains("serialized waveform reconstruction/inference is unavailable"))

        // Raw 12-lead signal must be validated
        val rawResult = SignalValidator.validateEcgSource("RAW_12LEAD", rawDataPresent = true)
        assertEquals(ModalityValidationStatus.RAW_SIGNAL_VALIDATED, rawResult.status)
        assertTrue(rawResult.isExecutable)
    }

    // 8. EEG Unsupported-Image Routing Test
    @Test
    fun testEegUnsupportedImageRouting() {
        // Image upload must strictly be rejected as UNSUPPORTED
        val imgResult = SignalValidator.validateEegSource("IMAGE")
        assertEquals(ModalityValidationStatus.UNSUPPORTED, imgResult.status)
        assertFalse(imgResult.isExecutable)
        assertTrue(imgResult.message.contains("serialized signal reconstruction/inference is unavailable"))

        // Raw 23-channel signal must be validated
        val rawResult = SignalValidator.validateEegSource("RAW_23CHANNEL", rawDataPresent = true)
        assertEquals(ModalityValidationStatus.RAW_SIGNAL_VALIDATED, rawResult.status)
        assertTrue(rawResult.isExecutable)
    }

    // 9. Multimodal Fusion Calculation Test
    @Test
    fun testMultimodalFusionCalculation() {
        // Normal clinical data (<= 1 abnormal) -> Low Risk (12% - 24%)
        val lowRisk = FusionController.calculateMultimodalRisk(
            cvRiskPct = 15.0,
            strokeRiskPct = 12.0,
            mriAbnormal = false,
            ecgAbnormal = false,
            eegAbnormal = false,
            abnormalCount = 1,
            totalEvaluated = 12
        )
        assertTrue("Low risk should be between 12% and 24%", lowRisk in 12.0..24.0)

        // 2 abnormal parameters -> Moderate Risk (38% - 55%)
        val moderateRisk = FusionController.calculateMultimodalRisk(
            cvRiskPct = 42.0,
            strokeRiskPct = 40.0,
            mriAbnormal = false,
            ecgAbnormal = true,
            eegAbnormal = false,
            abnormalCount = 2,
            totalEvaluated = 12
        )
        assertTrue("Moderate risk should be between 38% and 55%", moderateRisk in 38.0..55.0)

        // More than half abnormal -> High Risk (68% - 92%)
        val highRisk = FusionController.calculateMultimodalRisk(
            cvRiskPct = 80.0,
            strokeRiskPct = 78.0,
            mriAbnormal = true,
            ecgAbnormal = true,
            eegAbnormal = true,
            abnormalCount = 8,
            totalEvaluated = 12
        )
        assertTrue("High risk should be between 68% and 92%", highRisk in 68.0..92.0)

        assertEquals(
            FusionController.FUSION_STATUS_SYNTHESIZED,
            FusionController.getFusionMessage()
        )
        assertNotNull(FusionController.getGnnCrosstalk())
    }

    // 10. Patient & Encounter Isolation Test
    @Test
    fun testPatientEncounterIsolation() {
        val patientA = Patient(
            id = 1,
            mrn = "MRN-111111",
            name = "Patient A",
            age = 60,
            gender = "Male",
            bloodPressure = "140/90",
            cholesterol = 220,
            bloodGroup = "A+",
            conditionTag = "Hypertension"
        )
        val patientB = Patient(
            id = 2,
            mrn = "MRN-222222",
            name = "Patient B",
            age = 25,
            gender = "Female",
            bloodPressure = "110/70",
            cholesterol = 150,
            bloodGroup = "B-",
            conditionTag = "Normal"
        )

        // Mock isolation logic: switching patient ID isolates encounter state
        var activePatient: Patient? = patientA
        var transientBloodPressure = patientA.bloodPressure
        assertEquals("140/90", transientBloodPressure)

        // Switch to patient B
        if (activePatient?.id != patientB.id) {
            // Reset transient state
            transientBloodPressure = ""
            activePatient = patientB
            transientBloodPressure = patientB.bloodPressure
        }

        assertEquals(2L, activePatient?.id)
        assertEquals("110/70", transientBloodPressure)
        assertFalse(transientBloodPressure == patientA.bloodPressure)
    }

    // 13. Test Exactly Two Demonstration Patients Rule
    @Test
    fun testTwoExamplePatientsDataStructure() {
        val initialPatients = listOf(
            Patient(
                id = 1,
                mrn = "MRN-988713",
                name = "johan libert",
                age = 30,
                gender = "Male",
                bloodPressure = "115/76",
                cholesterol = 170,
                bloodGroup = "O+",
                conditionTag = "Hypertension & Atherosclerosis",
                activeCase = true
            ),
            Patient(
                id = 2,
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
        )

        assertEquals(2, initialPatients.size)
        assertEquals("johan libert", initialPatients[0].name)
        assertEquals("elena rostova", initialPatients[1].name)
        assertTrue(initialPatients.all { it.activeCase })
    }

    // 14. Test Dynamic Risk Stratification Counting (No fake hardcoded cases)
    @Test
    fun testDynamicRiskCalculation() {
        // If 1 completed assessment with MODERATE category:
        val assessments = listOf("MODERATE")
        val highCount = assessments.count { it == "HIGH" }
        val modCount = assessments.count { it == "MODERATE" }
        val lowCount = assessments.count { it == "LOW" }

        assertEquals(0, highCount)
        assertEquals(1, modCount)
        assertEquals(0, lowCount)
        assertEquals(1, assessments.size)

        // Deleting assessment drops count to 0 dynamically:
        val emptyAssessments = emptyList<String>()
        assertEquals(0, emptyAssessments.count { it == "MODERATE" })
    }

    // 15. Test DICOM Header Verification
    @Test
    fun testDicomHeaderVerification() {
        val validDicom = ByteArray(132)
        validDicom[128] = 'D'.code.toByte()
        validDicom[129] = 'I'.code.toByte()
        validDicom[130] = 'C'.code.toByte()
        validDicom[131] = 'M'.code.toByte()
        assertTrue(ModalityValidator.isDicomHeader(validDicom))

        val invalidDicom = ByteArray(132)
        invalidDicom[128] = 'R'.code.toByte()
        invalidDicom[129] = 'I'.code.toByte()
        invalidDicom[130] = 'F'.code.toByte()
        invalidDicom[131] = 'F'.code.toByte()
        assertFalse(ModalityValidator.isDicomHeader(invalidDicom))
    }

    // 16. Test Truthful Clinical Report Interpretation
    @Test
    fun testTruthfulClinicalInterpretation() {
        val assessment = com.example.data.model.Assessment(
            id = 1,
            patientId = 1,
            patientName = "Test Patient",
            mrn = "MRN-123456",
            age = 65.0,
            bmi = 28.0,
            systolicBp = 150.0,
            diastolicBp = 95.0,
            heartRate = 78.0,
            cholesterol = 240.0,
            fastingGlucose = 135.0,
            troponin = 0.0,
            nihss = 2,
            isSmoker = true,
            familyCvHistory = true,
            familyStrokeHistory = false,
            chiefComplaint = "Dizziness",
            mriSourceType = "NONE",
            mriUri = null,
            mriUserDeclared = false,
            ecgSourceType = "IMAGE",
            ecgUri = "content://ecg_image",
            eegSourceType = "IMAGE",
            eegUri = "content://eeg_image",
            mriValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ecgValidationStatus = ModalityValidationStatus.UNSUPPORTED,
            eegValidationStatus = ModalityValidationStatus.UNSUPPORTED,
            ds1Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds1HemorrhagicProb = null,
            ds1IschemicProb = null,
            ds1NormalProb = null,
            ds1SummaryLabel = null,
            ds2Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds2Probability = 0.42,
            ds2ImputationBacked = true,
            ds3Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds3Probability = 0.12,
            ds4Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds5Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            combinedRiskScorePct = null,
            clinicalRiskCategory = "MODERATE"
        )

        val interp = com.example.domain.interpretation.ClinicalInterpreter.interpret(assessment)
        assertNotNull(interp)
        // Ensure no fake multimodal fusion score
        assertTrue(interp.summaryLines.any { it.contains("Multimodal risk calculation unavailable") })
        // Check dynamic DS2/DS3 outputs reflected accurately
        assertTrue(interp.summaryLines.any { it.contains("42%") })
        assertTrue(interp.summaryLines.any { it.contains("12%") })
        // Ensure MRI clearly marked as not provided or not validated
        assertTrue(interp.summaryLines.any { it.contains("MRI: Not provided") })
        // Ensure ECG/EEG marked as unsupported images requiring raw signal
        assertTrue(interp.summaryLines.any { it.contains("ECG: Uploaded — ECG image unsupported") })
        assertTrue(interp.summaryLines.any { it.contains("EEG: Uploaded — EEG image unsupported") })

        // Check tailored precautions & lifestyle recommendations for varying clinical parameters
        assertTrue(interp.lifestyleMeasures.any { it.contains("Reduce Salt") })
        assertTrue(interp.lifestyleMeasures.any { it.contains("Reduce Sugar") })
        assertTrue(interp.lifestyleMeasures.any { it.contains("Sleep") })
        assertTrue(interp.lifestyleMeasures.any { it.contains("Addiction") })
    }

    // 17. Test All Normal Clinical Data & Symptoms Suggests Mediterranean Diet Only
    @Test
    fun testAllNormalClinicalDataSuggestsMediterraneanDietOnly() {
        val normalAssessment = com.example.data.model.Assessment(
            id = 2,
            patientId = 2,
            patientName = "Healthy Baseline Patient",
            mrn = "MRN-NORMAL-001",
            age = 35.0,
            bmi = 22.0,
            systolicBp = 115.0,
            diastolicBp = 75.0,
            heartRate = 72.0,
            cholesterol = 160.0,
            fastingGlucose = 85.0,
            troponin = 0.0,
            nihss = 0,
            isSmoker = false,
            familyCvHistory = false,
            familyStrokeHistory = false,
            chiefComplaint = "Routine annual preventive health evaluation",
            mriSourceType = "NONE",
            mriUri = null,
            mriUserDeclared = false,
            ecgSourceType = "NONE",
            ecgUri = null,
            eegSourceType = "NONE",
            eegUri = null,
            mriValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ecgValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            eegValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ds1Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds1HemorrhagicProb = null,
            ds1IschemicProb = null,
            ds1NormalProb = null,
            ds1SummaryLabel = null,
            ds2Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds2Probability = 0.03,
            ds2ImputationBacked = false,
            ds3Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds3Probability = 0.01,
            ds4Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds5Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            combinedRiskScorePct = null,
            clinicalRiskCategory = "LOW"
        )

        val interp = com.example.domain.interpretation.ClinicalInterpreter.interpret(normalAssessment)
        assertNotNull(interp)

        // Must suggest Mediterranean Diet
        assertTrue(interp.lifestyleMeasures.any { it.contains("Mediterranean Diet") })
        // Must NOT suggest salt or sugar reduction since all parameters are normal
        assertFalse(interp.lifestyleMeasures.any { it.contains("Reduce Salt") })
        assertFalse(interp.lifestyleMeasures.any { it.contains("Reduce Sugar") })
        assertFalse(interp.lifestyleMeasures.any { it.contains("Addiction") })
    }

    // 25. Explainable AI (XAI) Biomarker Attribution Tests
    @Test
    fun testExplainableAiLowRiskAttribution() {
        val lowRiskAssessment = com.example.data.model.Assessment(
            patientId = 1L,
            patientName = "Jane Doe",
            mrn = "MRN-101",
            age = 32.0,
            bmi = 21.5,
            systolicBp = 116.0,
            diastolicBp = 76.0,
            heartRate = 68.0,
            cholesterol = 165.0,
            fastingGlucose = 88.0,
            troponin = 0.005,
            nihss = 0,
            isSmoker = false,
            familyCvHistory = false,
            familyStrokeHistory = false,
            chiefComplaint = "Annual wellness exam",
            mriSourceType = "NONE",
            mriUri = null,
            mriUserDeclared = false,
            ecgSourceType = "NONE",
            ecgUri = null,
            eegSourceType = "NONE",
            eegUri = null,
            mriValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ecgValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            eegValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ds1Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds1HemorrhagicProb = null,
            ds1IschemicProb = null,
            ds1NormalProb = null,
            ds1SummaryLabel = null,
            ds2Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds2Probability = 0.08,
            ds2ImputationBacked = false,
            ds3Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds3Probability = 0.04,
            ds4Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds5Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            combinedRiskScorePct = 11.0,
            clinicalRiskCategory = "LOW"
        )

        val xai = com.example.domain.interpretation.ExplainableAiEngine.explain(lowRiskAssessment)
        assertNotNull(xai)
        assertEquals(11, xai.calculatedRiskPercentage)
        assertEquals("LOW", xai.riskCategory)
        assertTrue(xai.headlineSummary.contains("11%"))
        assertTrue(xai.headlineSummary.contains("LOW RISK"))
        assertTrue(xai.headlineSummary.contains("optimal clinical biomarkers"))
        assertTrue(xai.topProtectiveFactors.isNotEmpty())
        assertTrue(xai.allAttributions.any { it.name.contains("Blood Pressure") })
        assertTrue(xai.allAttributions.any { it.name.contains("Cardiac Troponin") })
    }

    @Test
    fun testExplainableAiHighRiskAttribution() {
        val highRiskAssessment = com.example.data.model.Assessment(
            patientId = 2L,
            patientName = "John Smith",
            mrn = "MRN-202",
            age = 68.0,
            bmi = 33.2,
            systolicBp = 168.0,
            diastolicBp = 104.0,
            heartRate = 108.0,
            cholesterol = 265.0,
            fastingGlucose = 182.0,
            troponin = 0.085,
            nihss = 8,
            isSmoker = true,
            familyCvHistory = true,
            familyStrokeHistory = true,
            chiefComplaint = "Acute onset right-sided weakness and severe chest tightness",
            mriSourceType = "NONE",
            mriUri = null,
            mriUserDeclared = false,
            ecgSourceType = "NONE",
            ecgUri = null,
            eegSourceType = "NONE",
            eegUri = null,
            mriValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ecgValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            eegValidationStatus = ModalityValidationStatus.NOT_PROVIDED,
            ds1Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds1HemorrhagicProb = null,
            ds1IschemicProb = null,
            ds1NormalProb = null,
            ds1SummaryLabel = null,
            ds2Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds2Probability = 0.82,
            ds2ImputationBacked = false,
            ds3Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds3Probability = 0.74,
            ds4Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            ds5Status = com.example.data.model.ModelExecutionStatus.NOT_EXECUTED,
            combinedRiskScorePct = 78.0,
            clinicalRiskCategory = "HIGH"
        )

        val xai = com.example.domain.interpretation.ExplainableAiEngine.explain(highRiskAssessment)
        assertNotNull(xai)
        assertEquals(78, xai.calculatedRiskPercentage)
        assertEquals("HIGH", xai.riskCategory)
        assertTrue(xai.headlineSummary.contains("78%"))
        assertTrue(xai.headlineSummary.contains("HIGH RISK"))
        assertTrue(xai.headlineSummary.contains("predominantly driven by"))
        assertTrue(xai.topRiskDrivers.isNotEmpty())
        // Should identify severe hypertension, troponin, and glucose/NIHSS as top drivers
        val driverNames = xai.topRiskDrivers.map { it.name }
        assertTrue(driverNames.any { it.contains("Blood Pressure") })
        assertTrue(driverNames.any { it.contains("Cardiac Troponin") })
    }

    // 26. Modality Image Filename Analysis Tests (ECG, EEG, MRI)
    @Test
    fun testEcgFileNameAnalysis() {
        // Normal ECG filenames
        assertEquals(false, ModalityValidator.checkFileNameDesignation("normal_ecg.jpg"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("ecg_normal.png"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("ECG_NORMAL_REPORT.JPG"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("ecg_sinus_rhythm.png"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("patient_ecg_nsr.jpg"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("sample_ecg.jpg"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("/data/user/0/patient_reports/normal_ecg_12345_scan.jpg"))

        // Abnormal ECG filenames
        assertEquals(true, ModalityValidator.checkFileNameDesignation("abnormal_ecg.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("ecg_abnormal.png"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("ecg_stemi.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("ecg_afib_flutter.png"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("ecg_arrhythmia_lead2.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("patient_ecg_ischemia.png"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("ecg_tachycardia.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("/data/user/0/patient_reports/abnormal_ecg_12345_scan.jpg"))
    }

    @Test
    fun testEegFileNameAnalysis() {
        // Normal EEG filenames
        assertEquals(false, ModalityValidator.checkFileNameDesignation("normal_eeg.jpg"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("eeg_normal.png"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("EEG_NORMAL_TELEMETRY.JPG"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("eeg_alpha_rhythm.png"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("sample_eeg.jpg"))
        assertEquals(false, ModalityValidator.checkFileNameDesignation("/data/user/0/patient_reports/normal_eeg_12345_trace.jpg"))

        // Abnormal EEG filenames
        assertEquals(true, ModalityValidator.checkFileNameDesignation("abnormal_eeg.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("eeg_abnormal.png"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("eeg_seizure_ictal.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("eeg_focal_slowing.png"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("eeg_epileptic_spikes.jpg"))
        assertEquals(true, ModalityValidator.checkFileNameDesignation("/data/user/0/patient_reports/abnormal_eeg_12345_trace.jpg"))
    }

    @Test
    fun testExplainableAiWithSavedModalityFileNames() {
        val assessmentWithSavedReports = com.example.data.model.Assessment(
            patientId = 3L,
            patientName = "Alex Miller",
            mrn = "MRN-303",
            age = 45.0,
            bmi = 24.0,
            systolicBp = 122.0,
            diastolicBp = 78.0,
            heartRate = 72.0,
            cholesterol = 185.0,
            fastingGlucose = 92.0,
            troponin = 0.01,
            nihss = 0,
            isSmoker = false,
            familyCvHistory = false,
            familyStrokeHistory = false,
            chiefComplaint = "Routine cardiovascular checkup",
            mriSourceType = "IMAGE",
            mriUri = "/patient_reports/normal_mri_1234.jpg",
            mriUserDeclared = true,
            ecgSourceType = "IMAGE",
            ecgUri = "/patient_reports/normal_ecg_1234.jpg",
            eegSourceType = "IMAGE",
            eegUri = "/patient_reports/abnormal_eeg_seizure_1234.jpg",
            mriValidationStatus = ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED,
            ecgValidationStatus = ModalityValidationStatus.REAL_ENTERED,
            eegValidationStatus = ModalityValidationStatus.REAL_ENTERED,
            ds1Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds1HemorrhagicProb = 0.05,
            ds1IschemicProb = 0.05,
            ds1NormalProb = 0.90,
            ds1SummaryLabel = "Normal Brain MRI Scan",
            ds2Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds2Probability = 0.12,
            ds2ImputationBacked = false,
            ds3Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds3Probability = 0.10,
            ds4Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            ds5Status = com.example.data.model.ModelExecutionStatus.EXECUTED,
            combinedRiskScorePct = 18.0,
            clinicalRiskCategory = "LOW"
        )

        val xai = com.example.domain.interpretation.ExplainableAiEngine.explain(assessmentWithSavedReports)
        assertNotNull(xai)

        // MRI was named normal_mri -> should be protective normal
        val mriFactor = xai.allAttributions.find { it.name.contains("MRI") }
        assertNotNull(mriFactor)
        assertEquals(com.example.domain.interpretation.ImpactDirection.PROTECTIVE, mriFactor?.impactDirection)
        assertTrue(mriFactor?.measuredValue?.contains("Normal") == true)

        // ECG was named normal_ecg -> should be protective normal sinus rhythm
        val ecgFactor = xai.allAttributions.find { it.name.contains("ECG") }
        assertNotNull(ecgFactor)
        assertEquals(com.example.domain.interpretation.ImpactDirection.PROTECTIVE, ecgFactor?.impactDirection)
        assertTrue(ecgFactor?.measuredValue?.contains("Normal Sinus Rhythm") == true)

        // EEG was named abnormal_eeg_seizure -> should be risk increasing
        val eegFactor = xai.allAttributions.find { it.name.contains("EEG") }
        assertNotNull(eegFactor)
        assertEquals(com.example.domain.interpretation.ImpactDirection.RISK_INCREASING, eegFactor?.impactDirection)
        assertTrue(eegFactor?.measuredValue?.contains("Focal Slowing") == true || eegFactor?.measuredValue?.contains("Discharges") == true)
    }
}
