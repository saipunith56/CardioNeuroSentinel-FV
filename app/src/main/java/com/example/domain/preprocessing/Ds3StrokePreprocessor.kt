package com.example.domain.preprocessing

object Ds3StrokePreprocessor {
    const val AGE_MEAN = 43.250604f
    const val AGE_SCALE = 22.681957f

    const val GLUCOSE_MEAN = 105.468747f
    const val GLUCOSE_SCALE = 44.364742f

    const val BMI_MEAN = 28.759200f
    const val BMI_SCALE = 7.695628f

    fun preprocess(
        age: Float,
        avgGlucoseLevel: Float,
        bmi: Float,
        gender: String, // "Female", "Male"
        hypertension: Int, // 0 or 1
        heartDisease: Int, // 0 or 1
        everMarried: String, // "No", "Yes"
        workType: String, // "Govt_job", "Never_worked", "Private", "Self-employed", "children"
        residenceType: String, // "Rural", "Urban"
        smokingStatus: String // "Unknown", "formerly smoked", "never smoked", "smokes"
    ): FloatArray {
        val features = FloatArray(22)

        // 3 Standardized Numerical Features
        features[0] = (age - AGE_MEAN) / AGE_SCALE
        features[1] = (avgGlucoseLevel - GLUCOSE_MEAN) / GLUCOSE_SCALE
        features[2] = (bmi - BMI_MEAN) / BMI_SCALE

        // Categorical One-Hot Encoding in Exact Order:
        // gender: Female, Male
        features[3] = if (gender.equals("Female", ignoreCase = true)) 1.0f else 0.0f
        features[4] = if (gender.equals("Male", ignoreCase = true)) 1.0f else 0.0f

        // hypertension: 0, 1
        features[5] = if (hypertension == 0) 1.0f else 0.0f
        features[6] = if (hypertension == 1) 1.0f else 0.0f

        // heart_disease: 0, 1
        features[7] = if (heartDisease == 0) 1.0f else 0.0f
        features[8] = if (heartDisease == 1) 1.0f else 0.0f

        // ever_married: No, Yes
        features[9] = if (everMarried.equals("No", ignoreCase = true)) 1.0f else 0.0f
        features[10] = if (everMarried.equals("Yes", ignoreCase = true)) 1.0f else 0.0f

        // work_type: Govt_job, Never_worked, Private, Self-employed, children
        features[11] = if (workType.equals("Govt_job", ignoreCase = true)) 1.0f else 0.0f
        features[12] = if (workType.equals("Never_worked", ignoreCase = true)) 1.0f else 0.0f
        features[13] = if (workType.equals("Private", ignoreCase = true)) 1.0f else 0.0f
        features[14] = if (workType.equals("Self-employed", ignoreCase = true)) 1.0f else 0.0f
        features[15] = if (workType.equals("children", ignoreCase = true)) 1.0f else 0.0f

        // Residence_type: Rural, Urban
        features[16] = if (residenceType.equals("Rural", ignoreCase = true)) 1.0f else 0.0f
        features[17] = if (residenceType.equals("Urban", ignoreCase = true)) 1.0f else 0.0f

        // smoking_status: Unknown, formerly smoked, never smoked, smokes
        features[18] = if (smokingStatus.equals("Unknown", ignoreCase = true)) 1.0f else 0.0f
        features[19] = if (smokingStatus.equals("formerly smoked", ignoreCase = true)) 1.0f else 0.0f
        features[20] = if (smokingStatus.equals("never smoked", ignoreCase = true)) 1.0f else 0.0f
        features[21] = if (smokingStatus.equals("smokes", ignoreCase = true)) 1.0f else 0.0f

        return features
    }
}
