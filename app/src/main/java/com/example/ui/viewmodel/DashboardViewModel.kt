package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Assessment
import com.example.data.repository.AssessmentRepository
import com.example.data.repository.PatientRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(
    private val patientRepository: PatientRepository,
    private val assessmentRepository: AssessmentRepository
) : ViewModel() {

    val patientCount: StateFlow<Int> = patientRepository.patientCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val activeCaseCount: StateFlow<Int> = patientRepository.activeCaseCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val totalAssessments: StateFlow<Int> = assessmentRepository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val highRiskCount: StateFlow<Int> = assessmentRepository.highRiskCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val moderateRiskCount: StateFlow<Int> = assessmentRepository.moderateRiskCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val lowRiskCount: StateFlow<Int> = assessmentRepository.lowRiskCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentAssessments: StateFlow<List<Assessment>> = assessmentRepository.allAssessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val federatedEpsilon: String = "ε = 0.5"
    val federatedStatus: String = "Active"

    class Factory(
        private val patientRepository: PatientRepository,
        private val assessmentRepository: AssessmentRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(patientRepository, assessmentRepository) as T
        }
    }
}
