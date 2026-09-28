package com.example.domain.preprocessing

data class Ds2PreprocessResult(
    val standardizedFeatures: FloatArray,
    val rawFeatures: FloatArray,
    val isImputationBacked: Boolean,
    val imputedFeatureIndices: List<Int>
)

object Ds2HeartPreprocessor {
    val TRAINING_MEDIANS = floatArrayOf(
        55.0f,
        1.0f,
        4.0f,
        130.0f,
        223.0f,
        0.0f,
        0.0f,
        140.0f,
        0.0f,
        0.6f,
        2.0f,
        0.0f,
        6.0f
    )

    val MEANS = floatArrayOf(
        53.760870f,
        0.782609f,
        3.250000f,
        132.288820f,
        201.371118f,
        0.153727f,
        0.622671f,
        137.531056f,
        0.371118f,
        0.898913f,
        1.843168f,
        0.208075f,
        5.591615f
    )

    val SCALES = floatArrayOf(
        9.495600f,
        0.412471f,
        0.929795f,
        18.045417f,
        107.024801f,
        0.360687f,
        0.806908f,
        25.174743f,
        0.483104f,
        1.075943f,
        0.527445f,
        0.600360f,
        1.386265f
    )

    /**
     * Exact 13 features contract:
     * 0: age
     * 1: sex (1.0 = male, 0.0 = female)
     * 2: cp (chest pain type 1.0 - 4.0)
     * 3: trestbps (resting blood pressure mmHg)
     * 4: chol (serum cholesterol mg/dL)
     * 5: fbs (fasting blood sugar > 120 mg/dL: 1.0 or 0.0)
     * 6: restecg (resting ecg 0.0, 1.0, 2.0)
     * 7: thalach (max heart rate)
     * 8: exang (exercise induced angina 1.0 or 0.0)
     * 9: oldpeak (ST depression)
     * 10: slope (ST slope 1.0, 2.0, 3.0)
     * 11: ca (vessels 0.0 - 3.0)
     * 12: thal (3.0 = normal, 6.0 = fixed defect, 7.0 = reversible defect)
     */
    fun preprocess(
        age: Float?,
        sex: Float?,
        cp: Float?,
        trestbps: Float?,
        chol: Float?,
        fbs: Float?,
        restecg: Float?,
        thalach: Float?,
        exang: Float?,
        oldpeak: Float?,
        slope: Float?,
        ca: Float?,
        thal: Float?
    ): Ds2PreprocessResult {
        val raw = FloatArray(13)
        val standardized = FloatArray(13)
        val imputedIndices = mutableListOf<Int>()

        val inputs = arrayOf(
            age, sex, cp, trestbps, chol, fbs,
            restecg, thalach, exang, oldpeak, slope, ca, thal
        )

        for (i in 0 until 13) {
            val inputValue = inputs[i]
            if (inputValue == null) {
                // Exact median imputation per contract
                raw[i] = TRAINING_MEDIANS[i]
                imputedIndices.add(i)
            } else {
                raw[i] = inputValue
            }
            standardized[i] = (raw[i] - MEANS[i]) / SCALES[i]
        }

        return Ds2PreprocessResult(
            standardizedFeatures = standardized,
            rawFeatures = raw,
            isImputationBacked = imputedIndices.isNotEmpty(),
            imputedFeatureIndices = imputedIndices
        )
    }
}
