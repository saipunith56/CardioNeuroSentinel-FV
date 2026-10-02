package com.example.domain.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object FederatedPrivacyManager {

    private val _privacyEpsilon = MutableStateFlow(0.50f)
    val privacyEpsilon: StateFlow<Float> = _privacyEpsilon.asStateFlow()

    private val _federatedNodeValue = MutableStateFlow("ε = 0.50")
    val federatedNodeValue: StateFlow<String> = _federatedNodeValue.asStateFlow()

    fun updateEpsilon(newEpsilon: Float) {
        val clamped = newEpsilon.coerceIn(0.1f, 2.0f)
        _privacyEpsilon.value = clamped
        val formatted = String.format(Locale.US, "ε = %.2f", clamped)
        _federatedNodeValue.value = formatted
    }

    fun getFormattedEpsilon(): String {
        return String.format(Locale.US, "%.2f", _privacyEpsilon.value)
    }

    fun getPrivacyLevelLabel(): String {
        return if (_privacyEpsilon.value <= 0.50f) "High Anonymization" else "Standard Privacy"
    }
}
