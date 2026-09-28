package com.example

import com.example.ui.viewmodel.ModalityCategory
import com.example.ui.viewmodel.MultimodalInput
import com.example.ui.viewmodel.RiskAssessmentViewModel
import com.example.ui.viewmodel.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RiskAssessmentViewModelTest {

    private lateinit var viewModel: RiskAssessmentViewModel

    @Before
    fun setUp() {
        viewModel = RiskAssessmentViewModel()
    }

    @Test
    fun testDefaultStateIsAllNormalLowRisk() {
        val state = viewModel.uiState.value

        assertEquals(0, state.abnormalCount)
        assertEquals(9, state.totalCount)
        assertEquals(RiskLevel.LOW, state.riskLevel)
        assertTrue("Overall risk percentage should be < 30%", state.overallRiskPct < 30.0)
        assertTrue("Cardio risk percentage should be < 30%", state.cardioRiskPct < 30.0)
        assertTrue("Cerebrovascular risk percentage should be < 30%", state.cerebrovascularRiskPct < 30.0)
    }

    @Test
    fun testSingleAbnormalInputRemainsLowRisk() {
        // Toggle only 1 input (e.g. ECG)
        viewModel.setInputStatus("ecg", isAbnormal = true)
        val state = viewModel.uiState.value

        assertEquals(1, state.abnormalCount)
        assertEquals(RiskLevel.LOW, state.riskLevel)
        assertTrue("1 abnormal input should still be in Low Risk bracket (<30%)", state.overallRiskPct < 30.0)
    }

    @Test
    fun testTwoAbnormalInputsCalculateModerateRisk() {
        // Clinical rule: if 2 of the data are more than normal -> Moderate risk
        viewModel.setModerateRiskPreset()
        val state = viewModel.uiState.value

        assertEquals(2, state.abnormalCount)
        assertEquals(RiskLevel.MODERATE, state.riskLevel)
        assertTrue("Moderate risk percentage should be between 30% and 65%", state.overallRiskPct in 30.0..64.9)
    }

    @Test
    fun testMoreThanHalfAbnormalInputsCalculateHighRisk() {
        // Clinical rule: if more than half of the data given are more than normal -> High risk
        viewModel.setHighRiskPreset()
        val state = viewModel.uiState.value

        assertEquals(5, state.abnormalCount)
        assertTrue(state.abnormalCount > (state.totalCount / 2))
        assertEquals(RiskLevel.HIGH, state.riskLevel)
        assertTrue("High risk percentage should be >= 65%", state.overallRiskPct >= 65.0)
    }

    @Test
    fun testToggleInputStatus() {
        // Initial state
        assertFalse(viewModel.uiState.value.inputs.first { it.id == "mri" }.isAbnormal)

        // Toggle MRI to abnormal
        viewModel.toggleInputStatus("mri")
        assertTrue(viewModel.uiState.value.inputs.first { it.id == "mri" }.isAbnormal)
        assertEquals(1, viewModel.uiState.value.abnormalCount)

        // Toggle MRI back to normal
        viewModel.toggleInputStatus("mri")
        assertFalse(viewModel.uiState.value.inputs.first { it.id == "mri" }.isAbnormal)
        assertEquals(0, viewModel.uiState.value.abnormalCount)
    }

    @Test
    fun testWeightedScoreCardioVsCerebrovascularDifferentiation() {
        // Cardiac dominant: ECG, BP, Cholesterol, Smoking
        viewModel.setCardiacDominantPreset()
        val cardioState = viewModel.uiState.value
        assertTrue("Cardio risk should be higher than cerebrovascular risk in cardiac profile",
            cardioState.cardioRiskPct > cardioState.cerebrovascularRiskPct)

        // Cerebrovascular dominant: MRI, EEG, BP, Glucose
        viewModel.setCerebrovascularDominantPreset()
        val neuroState = viewModel.uiState.value
        assertTrue("Cerebrovascular risk should be higher than cardio risk in stroke profile",
            neuroState.cerebrovascularRiskPct > neuroState.cardioRiskPct)
    }

    @Test
    fun testCompanionCalculationFunctionDirectly() {
        val testInputs = listOf(
            MultimodalInput(
                id = "ecg",
                name = "ECG",
                shortName = "ECG",
                category = ModalityCategory.CARDIAC,
                isAbnormal = true,
                normalFinding = "Normal",
                abnormalFinding = "Abnormal",
                weight = 1.8,
                cardiotropicWeight = 2.0,
                neurotropicWeight = 1.0
            ),
            MultimodalInput(
                id = "mri",
                name = "MRI",
                shortName = "MRI",
                category = ModalityCategory.CEREBROVASCULAR,
                isAbnormal = true,
                normalFinding = "Normal",
                abnormalFinding = "Abnormal",
                weight = 2.0,
                cardiotropicWeight = 0.5,
                neurotropicWeight = 2.5
            ),
            MultimodalInput(
                id = "bp",
                name = "BP",
                shortName = "BP",
                category = ModalityCategory.SYSTEMIC,
                isAbnormal = false,
                normalFinding = "Normal",
                abnormalFinding = "Abnormal",
                weight = 1.5,
                cardiotropicWeight = 1.5,
                neurotropicWeight = 1.5
            ),
            MultimodalInput(
                id = "glucose",
                name = "Glucose",
                shortName = "Glucose",
                category = ModalityCategory.SYSTEMIC,
                isAbnormal = false,
                normalFinding = "Normal",
                abnormalFinding = "Abnormal",
                weight = 1.3,
                cardiotropicWeight = 1.0,
                neurotropicWeight = 1.5
            )
        )

        // 2 abnormal out of 4 (half) -> Moderate risk
        val (overall, cardio, neuro) = RiskAssessmentViewModel.calculateRisk(testInputs)
        assertTrue(overall in 30.0..64.9)
    }
}
