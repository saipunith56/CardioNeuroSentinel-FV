package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.Patient
import com.example.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientViewModel(
    private val patientRepository: PatientRepository
) : ViewModel() {

    val searchQuery = MutableStateFlow("")
    val selectedPatient = MutableStateFlow<Patient?>(null)

    val allPatients: StateFlow<List<Patient>> = patientRepository.allPatients
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPatientsCount: StateFlow<Int> = patientRepository.patientCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val activeCasesCount: StateFlow<Int> = patientRepository.activeCaseCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val filteredPatients: StateFlow<List<Patient>> = combine(allPatients, searchQuery) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter {
                it.name.lowercase().contains(q) ||
                it.mrn.lowercase().contains(q) ||
                it.conditionTag.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectPatient(patient: Patient) {
        selectedPatient.value = patient
    }

    fun deletePatient(patientId: Long) {
        viewModelScope.launch {
            patientRepository.deletePatient(patientId)
            if (selectedPatient.value?.id == patientId) {
                selectedPatient.value = null
            }
        }
    }

    fun createPatient(
        name: String,
        mrn: String,
        age: Int,
        gender: String,
        bloodPressure: String,
        cholesterol: Int,
        bloodGroup: String,
        conditionTag: String
    ) {
        viewModelScope.launch {
            val newPatient = Patient(
                name = name,
                mrn = mrn,
                age = age,
                gender = gender,
                bloodPressure = bloodPressure,
                cholesterol = cholesterol,
                bloodGroup = bloodGroup,
                conditionTag = conditionTag,
                activeCase = true
            )
            patientRepository.insertPatient(newPatient)
        }
    }

    class Factory(private val patientRepository: PatientRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PatientViewModel(patientRepository) as T
        }
    }
}
