package com.example.domain.validation

import com.example.data.model.ModalityValidationStatus

data class SignalValidationResult(
    val status: ModalityValidationStatus,
    val isExecutable: Boolean,
    val message: String
)

object SignalValidator {

    const val ECG_IMAGE_UNSUPPORTED_MSG =
        "ECG Image uploaded - serialized waveform reconstruction/inference is unavailable. " +
        "Quantitative ECG model expects a raw 12-lead signal (10s @100Hz). Heuristic/model inference was not executed."

    const val EEG_IMAGE_UNSUPPORTED_MSG =
        "EEG Image uploaded - serialized signal reconstruction/inference is unavailable. " +
        "Quantitative EEG model expects a raw 23-channel EEG signal (1s @256Hz). Signal model inference was not executed."

    /**
     * Validates an ECG source. Images (photos, scans, screenshots) are strictly unsupported.
     */
    fun validateEcgSource(sourceType: String, rawDataPresent: Boolean = false): SignalValidationResult {
        return when (sourceType) {
            "IMAGE", "PHOTO", "SCREENSHOT", "PRESET_AFIB" -> {
                SignalValidationResult(
                    status = ModalityValidationStatus.UNSUPPORTED,
                    isExecutable = false,
                    message = ECG_IMAGE_UNSUPPORTED_MSG
                )
            }
            "RAW_12LEAD" -> {
                if (rawDataPresent) {
                    SignalValidationResult(
                        status = ModalityValidationStatus.RAW_SIGNAL_VALIDATED,
                        isExecutable = true,
                        message = "Raw 12-lead digital ECG telemetry validated (1000 samples @ 100Hz)."
                    )
                } else {
                    SignalValidationResult(
                        status = ModalityValidationStatus.UNSUPPORTED,
                        isExecutable = false,
                        message = "Raw signal buffer missing or empty."
                    )
                }
            }
            else -> {
                SignalValidationResult(
                    status = ModalityValidationStatus.NOT_PROVIDED,
                    isExecutable = false,
                    message = "ECG telemetry not provided."
                )
            }
        }
    }

    /**
     * Validates an EEG source. Images are strictly unsupported.
     */
    fun validateEegSource(sourceType: String, rawDataPresent: Boolean = false): SignalValidationResult {
        return when (sourceType) {
            "IMAGE", "PHOTO", "SCREENSHOT", "PRESET_SLOWING" -> {
                SignalValidationResult(
                    status = ModalityValidationStatus.UNSUPPORTED,
                    isExecutable = false,
                    message = EEG_IMAGE_UNSUPPORTED_MSG
                )
            }
            "RAW_23CHANNEL" -> {
                if (rawDataPresent) {
                    SignalValidationResult(
                        status = ModalityValidationStatus.RAW_SIGNAL_VALIDATED,
                        isExecutable = true,
                        message = "Raw 23-channel digital EEG telemetry validated (256 samples @ 256Hz)."
                    )
                } else {
                    SignalValidationResult(
                        status = ModalityValidationStatus.UNSUPPORTED,
                        isExecutable = false,
                        message = "Raw EEG signal buffer missing or empty."
                    )
                }
            }
            else -> {
                SignalValidationResult(
                    status = ModalityValidationStatus.NOT_PROVIDED,
                    isExecutable = false,
                    message = "EEG telemetry not provided."
                )
            }
        }
    }
}
