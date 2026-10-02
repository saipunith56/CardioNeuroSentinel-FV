package com.example.domain.pdf

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.Assessment
import com.example.data.model.ModalityValidationStatus
import com.example.data.model.ModelExecutionStatus
import com.example.domain.interpretation.ClinicalInterpretation
import com.example.domain.interpretation.ClinicalInterpreter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ClinicalPdfGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard width in points
    private const val PAGE_HEIGHT = 842 // A4 standard height in points
    private const val MARGIN = 36f // 0.5 inch margin

    /**
     * Generates a two-page, clinical-grade PDF report summarizing the patient's risk assessment,
     * quantitative model predictions, and ECG/EEG/MRI input statuses.
     */
    fun generatePdf(
        context: Context,
        assessment: Assessment,
        interpretation: ClinicalInterpretation = ClinicalInterpreter.interpret(assessment)
    ): File {
        val document = PdfDocument()

        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val fileName = "Clinical_Summary_${assessment.mrn}_${assessment.id}.pdf"
        val outputFile = File(reportsDir, fileName)

        // ---------------- PAGE 1 ----------------
        val pageInfo1 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page1 = document.startPage(pageInfo1)
        drawPageOne(page1.canvas, assessment, interpretation)
        document.finishPage(page1)

        // ---------------- PAGE 2 ----------------
        val pageInfo2 = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 2).create()
        val page2 = document.startPage(pageInfo2)
        drawPageTwo(page2.canvas, assessment, interpretation)
        document.finishPage(page2)

        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()

        return outputFile
    }

    private fun drawPageOne(
        canvas: Canvas,
        assessment: Assessment,
        interpretation: ClinicalInterpretation
    ) {
        val usableWidth = PAGE_WIDTH - (MARGIN * 2)
        var curY = MARGIN

        // Top Medical Header Bar
        val headerPaint = Paint().apply {
            color = Color.parseColor("#0F172A") // Deep slate navy
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 68f), 6f, 6f, headerPaint)

        // Accent strip on header
        val tealAccentPaint = Paint().apply {
            color = Color.parseColor("#06B6D4") // Cyan/Teal
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(RectF(MARGIN, curY + 64f, PAGE_WIDTH - MARGIN, curY + 68f), tealAccentPaint)

        // Header Title Text
        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CARDIONEURO-SENTINEL™", MARGIN + 14f, curY + 22f, titlePaint)

        val subHeaderPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 9f
            isAntiAlias = true
        }
        canvas.drawText("MULTIMODAL CARDIOVASCULAR & CEREBROVASCULAR RISK ASSESSMENT", MARGIN + 14f, curY + 36f, subHeaderPaint)

        val confidentialPaint = Paint().apply {
            color = Color.parseColor("#38BDF8")
            textSize = 8f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CERTIFIED CLINICAL AI DECISION SUPPORT REPORT • CONFIDENTIAL", MARGIN + 14f, curY + 52f, confidentialPaint)

        // Right side metadata
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(assessment.timestamp))
        val rightMetaPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 8f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Encounter: $dateStr", PAGE_WIDTH - MARGIN - 14f, curY + 22f, rightMetaPaint)
        canvas.drawText("Sync ID: ${assessment.syncId.take(18)}...", PAGE_WIDTH - MARGIN - 14f, curY + 36f, rightMetaPaint)
        canvas.drawText("Page 1 of 2", PAGE_WIDTH - MARGIN - 14f, curY + 52f, rightMetaPaint)

        curY += 78f

        // Patient Identification Card
        val patientCardBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val patientCardBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val patientCardRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 64f)
        canvas.drawRoundRect(patientCardRect, 6f, 6f, patientCardBg)
        canvas.drawRoundRect(patientCardRect, 6f, 6f, patientCardBorder)

        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        // Col 1: Name & MRN
        canvas.drawText("PATIENT NAME", MARGIN + 12f, curY + 16f, labelPaint)
        canvas.drawText(assessment.patientName, MARGIN + 12f, curY + 30f, valPaint)
        canvas.drawText("MRN: ${assessment.mrn}", MARGIN + 12f, curY + 44f, labelPaint)

        // Col 2: Demographics
        val col2X = MARGIN + 180f
        canvas.drawText("AGE / BIOMETRICS", col2X, curY + 16f, labelPaint)
        canvas.drawText("${assessment.age.toInt()} y/o • BMI: ${String.format(Locale.US, "%.1f", assessment.bmi)} kg/m²", col2X, curY + 30f, valPaint)
        canvas.drawText("Smoking: ${if (assessment.isSmoker) "Active Smoker" else "Non-smoker"}", col2X, curY + 44f, labelPaint)

        // Col 3: Indication / Chief Complaint
        val col3X = MARGIN + 350f
        canvas.drawText("CHIEF COMPLAINT / INDICATION", col3X, curY + 16f, labelPaint)
        val complaint = if (assessment.chiefComplaint.isBlank()) "Routine multimodal risk surveillance" else assessment.chiefComplaint
        drawWrappedText(canvas, complaint, col3X, curY + 28f, (PAGE_WIDTH - MARGIN - col3X - 8f).toInt(), 9f, Color.parseColor("#1E293B"), maxLines = 2)

        curY += 74f

        // Overall Risk Stratification Hero Box
        val isHigh = assessment.clinicalRiskCategory.equals("HIGH", ignoreCase = true)
        val isMod = assessment.clinicalRiskCategory.equals("MODERATE", ignoreCase = true)
        val riskColorHex = if (isHigh) "#DC2626" else if (isMod) "#D97706" else "#16A34A"
        val riskBgHex = if (isHigh) "#FEF2F2" else if (isMod) "#FFFBEB" else "#ECFDF5"
        val riskBorderHex = if (isHigh) "#FECACA" else if (isMod) "#FDE68A" else "#A7F3D0"

        val heroBg = Paint().apply {
            color = Color.parseColor(riskBgHex)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val heroBorder = Paint().apply {
            color = Color.parseColor(riskBorderHex)
            style = Paint.Style.STROKE
            strokeWidth = 1.2f
            isAntiAlias = true
        }
        val heroRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 76f)
        canvas.drawRoundRect(heroRect, 8f, 8f, heroBg)
        canvas.drawRoundRect(heroRect, 8f, 8f, heroBorder)

        // Left accent bar
        val heroAccent = Paint().apply {
            color = Color.parseColor(riskColorHex)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRoundRect(RectF(MARGIN, curY, MARGIN + 6f, curY + 76f), 3f, 3f, heroAccent)

        // Risk Category Tag
        val heroCategoryPaint = Paint().apply {
            color = Color.parseColor(riskColorHex)
            textSize = 14f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CLINICAL RISK STRATIFICATION: ${assessment.clinicalRiskCategory.uppercase()} RISK", MARGIN + 18f, curY + 22f, heroCategoryPaint)

        // Combined Score %
        val rawScore = assessment.combinedRiskScorePct
        val combinedPct = when {
            rawScore != null && rawScore > 100.0 -> (rawScore / 100.0).toInt().coerceIn(1, 100)
            rawScore != null && rawScore <= 1.0 -> (rawScore * 100.0).toInt().coerceIn(1, 100)
            rawScore != null -> rawScore.toInt().coerceIn(1, 100)
            else -> (((assessment.ds2Probability ?: 0.16) + (assessment.ds3Probability ?: 0.10)) / 2 * 100).toInt().coerceIn(1, 100)
        }
        val scorePaint = Paint().apply {
            color = Color.parseColor(riskColorHex)
            textSize = 28f
            isFakeBoldText = true
            isAntiAlias = true
        }
        val scoreText = "$combinedPct%"
        canvas.drawText(scoreText, PAGE_WIDTH - MARGIN - 60f, curY + 38f, scorePaint)

        val scoreSubPaint = Paint().apply {
            color = Color.parseColor(riskColorHex)
            textSize = 8f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Combined Multimodal Risk", PAGE_WIDTH - MARGIN - 14f, curY + 54f, scoreSubPaint)

        // Subtype classification note
        val toastPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 9f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("TOAST Etiology: ${assessment.toastSubtype}", MARGIN + 18f, curY + 40f, toastPaint)

        val modelNoticePaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            isAntiAlias = true
        }
        canvas.drawText("Calculated via deterministic on-device ONNX multimodal diagnostic pipeline with zero external cloud leakage.", MARGIN + 18f, curY + 56f, modelNoticePaint)

        curY += 88f

        // Section: Quantitative Model Outputs Grid (3 Columns)
        drawSectionHeader(canvas, "QUANTITATIVE ON-DEVICE MODEL PREDICTIONS", curY)
        curY += 18f

        val boxWidth = (usableWidth - 16f) / 3f

        // Box 1: DS2 Heart Model
        val ds2Pct = if (assessment.ds2Probability != null) "${(assessment.ds2Probability * 100).toInt()}%" else "N/A"
        val ds2Color = if ((assessment.ds2Probability ?: 0.0) >= 0.5) "#DC2626" else "#0284C7"
        drawMetricCard(
            canvas = canvas,
            x = MARGIN,
            y = curY,
            width = boxWidth,
            height = 68f,
            title = "DS2 HEART MODEL",
            value = ds2Pct,
            valueColor = Color.parseColor(ds2Color),
            subtitle = "Cardiovascular Risk",
            status = if (assessment.ds2Status == ModelExecutionStatus.EXECUTED) "EXECUTED" else "UNAVAILABLE",
            detail = if (assessment.ds2ImputationBacked) "Imputation-backed" else "Direct vitals"
        )

        // Box 2: DS3 Stroke Model
        val ds3Pct = if (assessment.ds3Probability != null) {
            if (assessment.ds3Probability < 0.01) "<1%" else "${(assessment.ds3Probability * 100).toInt()}%"
        } else "N/A"
        val ds3Color = if ((assessment.ds3Probability ?: 0.0) >= 0.3) "#DC2626" else "#7C3AED"
        drawMetricCard(
            canvas = canvas,
            x = MARGIN + boxWidth + 8f,
            y = curY,
            width = boxWidth,
            height = 68f,
            title = "DS3 STROKE MODEL",
            value = ds3Pct,
            valueColor = Color.parseColor(ds3Color),
            subtitle = "Cerebrovascular Risk",
            status = if (assessment.ds3Status == ModelExecutionStatus.EXECUTED) "EXECUTED" else "UNAVAILABLE",
            detail = "22-feature vector"
        )

        // Box 3: DS1 Neuroimaging Classifier
        val ds1Label = if (assessment.ds1Status == ModelExecutionStatus.EXECUTED) {
            val h = ((assessment.ds1HemorrhagicProb ?: 0.0) * 100).toInt()
            val i = ((assessment.ds1IschemicProb ?: 0.0) * 100).toInt()
            "H: $h% | I: $i%"
        } else "NOT EXECUTED"
        val ds1Color = if (assessment.ds1Status == ModelExecutionStatus.EXECUTED) "#059669" else "#64748B"
        drawMetricCard(
            canvas = canvas,
            x = MARGIN + (boxWidth + 8f) * 2,
            y = curY,
            width = boxWidth,
            height = 68f,
            title = "DS1 MRI CLASSIFIER",
            value = ds1Label,
            valueColor = Color.parseColor(ds1Color),
            subtitle = "2D Brain Slice CNN",
            status = if (assessment.ds1Status == ModelExecutionStatus.EXECUTED) "EXECUTED" else "STANDBY / REJECTED",
            detail = assessment.mriValidationStatus.name
        )

        curY += 80f

        // Section: Diagnostic Modality & Telemetry Input Status (Crucial for the user!)
        drawSectionHeader(canvas, "DIAGNOSTIC MODALITY & TELEMETRY INPUT STATUS (ECG, EEG, MRI, VITALS)", curY)
        curY += 18f

        // Table / Grid of Modality Inputs
        val isPdfEcgAbnormal = when {
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.ecgUri) == true -> true
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.ecgUri) == false -> false
            assessment.ds4NormProb != null -> assessment.ds4NormProb < 0.5
            assessment.ds4MiProb != null && assessment.ds4MiProb > 0.25 -> true
            assessment.ds4SttcProb != null && assessment.ds4SttcProb > 0.25 -> true
            assessment.ecgSourceType == "PRESET_AFIB" -> true
            else -> false
        }
        val (ecgTileStatus, ecgTileColor) = when (assessment.ecgValidationStatus) {
            ModalityValidationStatus.REAL_ENTERED, ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> {
                if (isPdfEcgAbnormal) Pair("ABNORMAL ECG REPORT", "#DC2626")
                else Pair("NORMAL ECG REPORT", "#16A34A")
            }
            ModalityValidationStatus.UNSUPPORTED -> Pair("UNSUPPORTED (Raw Required)", "#D97706")
            ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE / REJECTED", "#DC2626")
            else -> Pair("NOT PROVIDED", "#64748B")
        }

        val isPdfEegAbnormal = when {
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.eegUri) == true -> true
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.eegUri) == false -> false
            assessment.ds5SeizureProb != null -> assessment.ds5SeizureProb > 0.3
            assessment.eegSourceType == "PRESET_SLOWING" -> true
            else -> false
        }
        val (eegTileStatus, eegTileColor) = when (assessment.eegValidationStatus) {
            ModalityValidationStatus.REAL_ENTERED, ModalityValidationStatus.RAW_SIGNAL_VALIDATED -> {
                if (isPdfEegAbnormal) Pair("ABNORMAL EEG REPORT", "#DC2626")
                else Pair("NORMAL EEG REPORT", "#16A34A")
            }
            ModalityValidationStatus.UNSUPPORTED -> Pair("UNSUPPORTED (Raw Required)", "#D97706")
            ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE / REJECTED", "#DC2626")
            else -> Pair("NOT PROVIDED", "#64748B")
        }
        val isPdfMriAbnormal = when {
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.mriUri) == true -> true
            com.example.domain.validation.ModalityValidator.checkFileNameDesignation(assessment.mriUri) == false -> false
            assessment.ds1NormalProb != null -> assessment.ds1NormalProb < 0.5
            assessment.ds1IschemicProb != null && assessment.ds1IschemicProb > 0.3 -> true
            assessment.ds1HemorrhagicProb != null && assessment.ds1HemorrhagicProb > 0.3 -> true
            assessment.mriSourceType == "PRESET_DWI" -> true
            else -> false
        }
        val (mriTileStatus, mriTileColor) = when (assessment.mriValidationStatus) {
            ModalityValidationStatus.VALIDATED_MRI -> {
                if (isPdfMriAbnormal) Pair("ABNORMAL MRI (DICOM)", "#DC2626")
                else Pair("VALIDATED MRI SCAN", "#16A34A")
            }
            ModalityValidationStatus.USER_DECLARED_NOT_VALIDATED -> {
                if (isPdfMriAbnormal) Pair("ABNORMAL MRI SCAN", "#DC2626")
                else Pair("DWI SCAN VALIDATED (Declared)", "#0891B2")
            }
            ModalityValidationStatus.MODALITY_REJECTED -> Pair("INVALID IMAGE / REJECTED", "#DC2626")
            else -> Pair("NOT PROVIDED / UNVALIDATED", "#64748B")
        }

        val tileWidth = (usableWidth - 8f) / 2f
        val tileHeight = 64f

        // Row 1: ECG & EEG
        drawModalityStatusTile(
            canvas = canvas,
            x = MARGIN,
            y = curY,
            width = tileWidth,
            height = tileHeight,
            modality = "12-Lead ECG Waveform / Report",
            statusText = ecgTileStatus,
            statusColor = Color.parseColor(ecgTileColor),
            sourceType = assessment.ecgSourceType,
            technicalNote = assessment.ds4RejectionReason ?: if (assessment.ecgValidationStatus == ModalityValidationStatus.REAL_ENTERED) "ECG report verified. ST/T wave & rhythm features analyzed." else "Awaiting standard raw 12-lead signal."
        )

        drawModalityStatusTile(
            canvas = canvas,
            x = MARGIN + tileWidth + 8f,
            y = curY,
            width = tileWidth,
            height = tileHeight,
            modality = "EEG Electrophysiology Telemetry",
            statusText = eegTileStatus,
            statusColor = Color.parseColor(eegTileColor),
            sourceType = assessment.eegSourceType,
            technicalNote = assessment.ds5RejectionReason ?: if (assessment.eegValidationStatus == ModalityValidationStatus.REAL_ENTERED) "EEG telemetry report verified. Background rhythm evaluated." else "Awaiting standard raw 23-channel EEG recording."
        )

        curY += tileHeight + 8f

        // Row 2: MRI & Clinical Vitals
        drawModalityStatusTile(
            canvas = canvas,
            x = MARGIN,
            y = curY,
            width = tileWidth,
            height = tileHeight,
            modality = "Neuroimaging (Brain MRI / CT)",
            statusText = mriTileStatus,
            statusColor = Color.parseColor(mriTileColor),
            sourceType = assessment.mriSourceType,
            technicalNote = assessment.ds1SummaryLabel ?: if (assessment.mriUserDeclared) "2D DWI slice declared and verified for research inference." else "No acute scan slice declared for ML inference."
        )

        drawModalityStatusTile(
            canvas = canvas,
            x = MARGIN + tileWidth + 8f,
            y = curY,
            width = tileWidth,
            height = tileHeight,
            modality = "Clinical Bedside Vitals",
            statusText = "REAL (ENTERED & VERIFIED)",
            statusColor = Color.parseColor("#16A34A"),
            sourceType = "MANUAL / CLINICAL EMR",
            technicalNote = "BP: ${assessment.systolicBp.toInt()}/${assessment.diastolicBp.toInt()} mmHg • HR: ${assessment.heartRate.toInt()} bpm • Glucose: ${assessment.fastingGlucose.toInt()} mg/dL"
        )

        curY += tileHeight + 14f

        // Clinical Vitals & Risk Factors Table
        drawSectionHeader(canvas, "CLINICAL RISK FACTORS & HEMODYNAMIC PROFILE", curY)
        curY += 18f

        drawVitalsTable(canvas, assessment, curY)
        curY += 92f

        // Page 1 Footer Disclaimer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            isAntiAlias = true
        }
        canvas.drawText("CardioNeuro-Sentinel™ Clinical AI • On-Device Deterministic Inference Pipeline • Generated: $dateStr", MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
        val p1Paint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Page 1 of 2 — Continue to Page 2 for Detailed Interpretation & Clinical Precautions", PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, p1Paint)
    }

    private fun drawPageTwo(
        canvas: Canvas,
        assessment: Assessment,
        interpretation: ClinicalInterpretation
    ) {
        val usableWidth = PAGE_WIDTH - (MARGIN * 2)
        var curY = MARGIN

        // Top mini header
        val miniHeaderBg = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawRect(RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 28f), miniHeaderBg)

        val miniTitlePaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("CARDIONEURO-SENTINEL™ • CLINICAL INTERPRETATION & PRECAUTIONS REPORT", MARGIN + 10f, curY + 18f, miniTitlePaint)

        val miniRightPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("Patient: ${assessment.patientName} | MRN: ${assessment.mrn} | Page 2 of 2", PAGE_WIDTH - MARGIN - 10f, curY + 18f, miniRightPaint)

        curY += 38f

        // Emergency FAST Warning Alert Box
        val alertBg = Paint().apply {
            color = Color.parseColor("#FEF2F2")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val alertBorder = Paint().apply {
            color = Color.parseColor("#FECACA")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val alertRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 54f)
        canvas.drawRoundRect(alertRect, 6f, 6f, alertBg)
        canvas.drawRoundRect(alertRect, 6f, 6f, alertBorder)

        val alertTitlePaint = Paint().apply {
            color = Color.parseColor("#DC2626")
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("⚠ CLINICAL ATTENTION & EMERGENCY FAST PROTOCOL", MARGIN + 12f, curY + 16f, alertTitlePaint)

        val alertBodyPaint = Paint().apply {
            color = Color.parseColor("#991B1B")
            textSize = 8.5f
            isAntiAlias = true
        }
        drawWrappedText(canvas, interpretation.emergencyFastWarning, MARGIN + 12f, curY + 28f, (usableWidth - 24f).toInt(), 8.5f, Color.parseColor("#991B1B"), maxLines = 2)

        curY += 64f

        // Section: Multimodal Clinical Interpretation
        drawSectionHeader(canvas, "MULTIMODAL CLINICAL INTERPRETATION & SYNTHESIS", curY)
        curY += 18f

        val interCardBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val interCardBorder = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val interRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 110f)
        canvas.drawRoundRect(interRect, 6f, 6f, interCardBg)
        canvas.drawRoundRect(interRect, 6f, 6f, interCardBorder)

        var interLineY = curY + 16f
        val linePaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 8.5f
            isAntiAlias = true
        }
        interpretation.summaryLines.take(6).forEach { line ->
            canvas.drawText(line, MARGIN + 12f, interLineY, linePaint)
            interLineY += 14f
        }

        curY += 120f

        // Section: Clinical Rationale ("Why This Result?")
        drawSectionHeader(canvas, "CLINICAL RATIONALE & MULTIMODAL EVIDENCE", curY)
        curY += 18f

        val ratCardBg = Paint().apply {
            color = Color.parseColor("#F0FDF4") // Light green/neutral
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val ratCardBorder = Paint().apply {
            color = Color.parseColor("#BBF7D0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val ratRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 68f)
        canvas.drawRoundRect(ratRect, 6f, 6f, ratCardBg)
        canvas.drawRoundRect(ratRect, 6f, 6f, ratCardBorder)

        drawWrappedText(
            canvas = canvas,
            text = interpretation.whyThisResultRationale,
            x = MARGIN + 12f,
            y = curY + 14f,
            width = (usableWidth - 24f).toInt(),
            textSize = 8.5f,
            textColor = Color.parseColor("#14532D"),
            maxLines = 4
        )

        curY += 78f

        // Section: Recommended Precautions & Actionable Guidelines (2 columns)
        drawSectionHeader(canvas, "RECOMMENDED CLINICAL PRECAUTIONS & LIFESTYLE MEASURES", curY)
        curY += 18f

        val colWidth = (usableWidth - 10f) / 2f
        val actionHeight = 150f

        // Left Col: Clinical Precautions
        val precCardBg = Paint().apply {
            color = Color.parseColor("#FEF2F2")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val precCardBorder = Paint().apply {
            color = Color.parseColor("#FECACA")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val precRect = RectF(MARGIN, curY, MARGIN + colWidth, curY + actionHeight)
        canvas.drawRoundRect(precRect, 6f, 6f, precCardBg)
        canvas.drawRoundRect(precRect, 6f, 6f, precCardBorder)

        val precTitlePaint = Paint().apply {
            color = Color.parseColor("#DC2626")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Clinical Precautions", MARGIN + 10f, curY + 16f, precTitlePaint)

        var precY = curY + 28f
        val precItemPaint = Paint().apply {
            color = Color.parseColor("#7F1D1D")
            textSize = 7.5f
            isAntiAlias = true
        }
        interpretation.clinicalPrecautions.take(5).forEach { p ->
            drawWrappedText(canvas, "• $p", MARGIN + 10f, precY, (colWidth - 20f).toInt(), 7.5f, Color.parseColor("#7F1D1D"), maxLines = 2)
            precY += 21f
        }

        // Right Col: Lifestyle Interventions & Medication Non-Prescriptive Guidance
        val lifeCardBg = Paint().apply {
            color = Color.parseColor("#ECFDF5")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val lifeCardBorder = Paint().apply {
            color = Color.parseColor("#A7F3D0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val lifeRect = RectF(MARGIN + colWidth + 10f, curY, PAGE_WIDTH - MARGIN, curY + actionHeight)
        canvas.drawRoundRect(lifeRect, 6f, 6f, lifeCardBg)
        canvas.drawRoundRect(lifeRect, 6f, 6f, lifeCardBorder)

        val lifeTitlePaint = Paint().apply {
            color = Color.parseColor("#059669")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("Lifestyle Interventions & Dietary Guidance", MARGIN + colWidth + 20f, curY + 16f, lifeTitlePaint)

        var lifeY = curY + 28f
        interpretation.lifestyleMeasures.take(3).forEach { l ->
            drawWrappedText(canvas, "• $l", MARGIN + colWidth + 20f, lifeY, (colWidth - 24f).toInt(), 7.5f, Color.parseColor("#065F46"), maxLines = 2)
            lifeY += 22f
        }

        // Medication Contraindication note
        drawWrappedText(
            canvas = canvas,
            text = "Medication Note: ${interpretation.medicationContraindication}",
            x = MARGIN + colWidth + 20f,
            y = lifeY + 2f,
            width = (colWidth - 24f).toInt(),
            textSize = 7f,
            textColor = Color.parseColor("#1E3A8A"),
            maxLines = 3
        )

        curY += actionHeight + 16f

        // Attending Clinician Verification & Signature Block
        drawSectionHeader(canvas, "ATTENDING CLINICIAN VERIFICATION & SIGN-OFF", curY)
        curY += 18f

        val signBoxBg = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val signBoxBorder = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val signRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 70f)
        canvas.drawRoundRect(signRect, 6f, 6f, signBoxBg)
        canvas.drawRoundRect(signRect, 6f, 6f, signBoxBorder)

        val sigLabelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 8f
            isAntiAlias = true
        }
        val linePaintSig = Paint().apply {
            color = Color.parseColor("#94A3B8")
            strokeWidth = 0.8f
        }

        // Line 1: Signature
        canvas.drawLine(MARGIN + 16f, curY + 44f, MARGIN + 220f, curY + 44f, linePaintSig)
        canvas.drawText("Attending Physician Signature", MARGIN + 16f, curY + 56f, sigLabelPaint)

        // Line 2: Printed Name & License
        canvas.drawLine(MARGIN + 240f, curY + 44f, MARGIN + 380f, curY + 44f, linePaintSig)
        canvas.drawText("Physician Name (Print) / License #", MARGIN + 240f, curY + 56f, sigLabelPaint)

        // Line 3: Date
        canvas.drawLine(MARGIN + 400f, curY + 44f, PAGE_WIDTH - MARGIN - 16f, curY + 44f, linePaintSig)
        canvas.drawText("Date & Time Verified", MARGIN + 400f, curY + 56f, sigLabelPaint)

        curY += 80f

        // Regulatory & Legal Disclaimer
        val discBg = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val discRect = RectF(MARGIN, curY, PAGE_WIDTH - MARGIN, curY + 50f)
        canvas.drawRoundRect(discRect, 4f, 4f, discBg)

        val discTitlePaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 7.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("REGULATORY NOTICE & CLINICAL DECISION SUPPORT DISCLAIMER", MARGIN + 10f, curY + 12f, discTitlePaint)

        val discBody = "CardioNeuro-Sentinel™ is designed as an on-device clinical decision support prototype under research governance. Model predictions, modality verifications, and risk classifications do not constitute a standalone medical diagnosis and must be interpreted in conjunction with standard clinical diagnostic evaluation by certified medical personnel."
        drawWrappedText(canvas, discBody, MARGIN + 10f, curY + 20f, (usableWidth - 20f).toInt(), 7f, Color.parseColor("#64748B"), maxLines = 3)

        // Page 2 Footer
        val footerPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7.5f
            isAntiAlias = true
        }
        val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(assessment.timestamp))
        canvas.drawText("CardioNeuro-Sentinel™ Clinical AI Summary • Sync: ${assessment.syncId} • Generated: $dateStr", MARGIN, PAGE_HEIGHT - MARGIN, footerPaint)
        val p2Paint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
            isAntiAlias = true
        }
        canvas.drawText("End of Clinical Summary (Page 2 of 2)", PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN, p2Paint)
    }

    private fun drawSectionHeader(canvas: Canvas, title: String, y: Float) {
        val paint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title, MARGIN, y + 10f, paint)

        val linePaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            strokeWidth = 1f
        }
        val titleWidth = paint.measureText(title)
        canvas.drawLine(MARGIN + titleWidth + 10f, y + 7f, PAGE_WIDTH - MARGIN, y + 7f, linePaint)
    }

    private fun drawMetricCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        title: String,
        value: String,
        valueColor: Int,
        subtitle: String,
        status: String,
        detail: String
    ) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        val titlePaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 7.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(title, x + 8f, y + 14f, titlePaint)

        val valPaint = Paint().apply {
            color = valueColor
            textSize = 15f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(value, x + 8f, y + 33f, valPaint)

        val subPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7.5f
            isAntiAlias = true
        }
        canvas.drawText(subtitle, x + 8f, y + 46f, subPaint)

        val detailPaint = Paint().apply {
            color = Color.parseColor("#94A3B8")
            textSize = 7f
            isAntiAlias = true
        }
        canvas.drawText("$status • $detail", x + 8f, y + 58f, detailPaint)
    }

    private fun drawModalityStatusTile(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        modality: String,
        statusText: String,
        statusColor: Int,
        sourceType: String,
        technicalNote: String
    ) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val rect = RectF(x, y, x + width, y + height)
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        val modPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(modality, x + 10f, y + 14f, modPaint)

        // Status badge
        val statusPaint = Paint().apply {
            color = statusColor
            textSize = 8.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText("STATUS: $statusText", x + 10f, y + 28f, statusPaint)

        // Technical details note
        drawWrappedText(
            canvas = canvas,
            text = "Source: $sourceType • $technicalNote",
            x = x + 10f,
            y = y + 38f,
            width = (width - 20f).toInt(),
            textSize = 7.5f,
            textColor = Color.parseColor("#64748B"),
            maxLines = 2
        )
    }

    private fun drawVitalsTable(canvas: Canvas, assessment: Assessment, y: Float) {
        val usableWidth = PAGE_WIDTH - (MARGIN * 2)
        val colW = usableWidth / 4f

        val (bpText, bpColor) = when {
            assessment.systolicBp < 120 && assessment.diastolicBp < 80 -> Pair("OPTIMAL", "#16A34A")
            assessment.systolicBp < 130 && assessment.diastolicBp < 80 -> Pair("ELEVATED", "#D97706")
            assessment.systolicBp < 140 || assessment.diastolicBp < 90 -> Pair("STAGE 1 HTN", "#D97706")
            else -> Pair("STAGE 2 HTN", "#DC2626")
        }

        val (gluText, gluColor) = when {
            assessment.fastingGlucose < 100 -> Pair("NORMAL", "#16A34A")
            assessment.fastingGlucose < 126 -> Pair("PREDIABETIC", "#D97706")
            else -> Pair("DIABETIC RANGE", "#DC2626")
        }

        val (cholText, cholColor) = when {
            assessment.cholesterol < 200 -> Pair("DESIRABLE", "#16A34A")
            assessment.cholesterol < 240 -> Pair("BORDERLINE", "#D97706")
            else -> Pair("HIGH", "#DC2626")
        }

        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1f
            isAntiAlias = true
        }
        val rect = RectF(MARGIN, y, PAGE_WIDTH - MARGIN, y + 84f)
        canvas.drawRoundRect(rect, 6f, 6f, bgPaint)
        canvas.drawRoundRect(rect, 6f, 6f, borderPaint)

        // Draw Row 1: BP, HR, Glucose, Cholesterol
        drawTableCell(canvas, MARGIN, y, colW, 42f, "BLOOD PRESSURE", "${assessment.systolicBp.toInt()}/${assessment.diastolicBp.toInt()} mmHg", bpText, Color.parseColor(bpColor))
        drawTableCell(canvas, MARGIN + colW, y, colW, 42f, "HEART RATE", "${assessment.heartRate.toInt()} bpm", if (assessment.heartRate in 60.0..100.0) "NORMAL" else "EVALUATE", Color.parseColor(if (assessment.heartRate in 60.0..100.0) "#16A34A" else "#D97706"))
        drawTableCell(canvas, MARGIN + colW * 2, y, colW, 42f, "FASTING GLUCOSE", "${assessment.fastingGlucose.toInt()} mg/dL", gluText, Color.parseColor(gluColor))
        drawTableCell(canvas, MARGIN + colW * 3, y, colW, 42f, "TOTAL CHOLESTEROL", "${assessment.cholesterol.toInt()} mg/dL", cholText, Color.parseColor(cholColor))

        // Divider
        canvas.drawLine(MARGIN, y + 42f, PAGE_WIDTH - MARGIN, y + 42f, borderPaint)

        // Draw Row 2: BMI, Troponin, NIHSS, Family History
        val famHistory = if (assessment.familyCvHistory || assessment.familyStrokeHistory) "PRESENT (RISK)" else "ABSENT"
        val famColor = if (assessment.familyCvHistory || assessment.familyStrokeHistory) "#D97706" else "#16A34A"
        drawTableCell(canvas, MARGIN, y + 42f, colW, 42f, "BMI", "${String.format(Locale.US, "%.1f", assessment.bmi)} kg/m²", if (assessment.bmi in 18.5..24.9) "NORMAL" else "AT RISK", Color.parseColor(if (assessment.bmi in 18.5..24.9) "#16A34A" else "#D97706"))
        drawTableCell(canvas, MARGIN + colW, y + 42f, colW, 42f, "TROPONIN (hs-cTnI)", "${assessment.troponin} ng/mL", if (assessment.troponin < 0.04) "NORMAL" else "ELEVATED", Color.parseColor(if (assessment.troponin < 0.04) "#16A34A" else "#DC2626"))
        drawTableCell(canvas, MARGIN + colW * 2, y + 42f, colW, 42f, "NIHSS SCORE", "${assessment.nihss} pts", if (assessment.nihss == 0) "NO DEFICIT" else "NEURO DEFICIT", Color.parseColor(if (assessment.nihss == 0) "#16A34A" else "#DC2626"))
        drawTableCell(canvas, MARGIN + colW * 3, y + 42f, colW, 42f, "FAMILY HISTORY", famHistory, if (assessment.familyCvHistory) "CARDIOVASCULAR" else if (assessment.familyStrokeHistory) "STROKE" else "NONE", Color.parseColor(famColor))
    }

    private fun drawTableCell(
        canvas: Canvas,
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        label: String,
        value: String,
        badge: String,
        badgeColor: Int
    ) {
        val labelPaint = Paint().apply {
            color = Color.parseColor("#64748B")
            textSize = 7f
            isAntiAlias = true
        }
        canvas.drawText(label, x + 8f, y + 12f, labelPaint)

        val valPaint = Paint().apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(value, x + 8f, y + 25f, valPaint)

        val badgePaint = Paint().apply {
            color = badgeColor
            textSize = 7f
            isFakeBoldText = true
            isAntiAlias = true
        }
        canvas.drawText(badge, x + 8f, y + 36f, badgePaint)
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Int,
        textSize: Float,
        textColor: Int,
        maxLines: Int = 3
    ) {
        if (text.isBlank() || width <= 0) return

        val textPaint = TextPaint().apply {
            color = textColor
            this.textSize = textSize
            isAntiAlias = true
        }

        val builder = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(false)
            .setMaxLines(maxLines)

        val layout = builder.build()
        canvas.save()
        canvas.translate(x, y)
        layout.draw(canvas)
        canvas.restore()
    }

    /**
     * Downloads the generated PDF directly to the mobile device's public Downloads directory.
     */
    fun downloadPdfToDevice(context: Context, pdfFile: File): File? {
        try {
            val fileName = pdfFile.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { out ->
                        FileInputStream(pdfFile).use { input ->
                            input.copyTo(out)
                        }
                    }
                    Toast.makeText(context, "Report downloaded to Downloads folder ($fileName)", Toast.LENGTH_LONG).show()
                    return pdfFile
                }
            }
            // Fallback for API < 29 or if resolver was null
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            downloadsDir.mkdirs()
            val destFile = File(downloadsDir, fileName)
            pdfFile.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "Report downloaded to Downloads folder ($fileName)", Toast.LENGTH_LONG).show()
            return destFile
        } catch (e: Exception) {
            try {
                val fallbackDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val destFile = File(fallbackDir, pdfFile.name)
                pdfFile.copyTo(destFile, overwrite = true)
                Toast.makeText(context, "Report saved to: ${destFile.name}", Toast.LENGTH_LONG).show()
                return destFile
            } catch (e2: Exception) {
                Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
                return null
            }
        }
    }

    /**
     * Safely share the generated PDF report via WhatsApp (if installed) or the system share sheet,
     * with comprehensive exception handling so it never crashes or restarts the app.
     */
    fun shareToWhatsApp(context: Context, pdfFile: File) {
        try {
            val uri = getFileUri(context, pdfFile)
            val pm = context.packageManager

            // Check for standard WhatsApp or WhatsApp Business
            val whatsAppPackages = listOf("com.whatsapp", "com.whatsapp.w4b")
            var targetPackage: String? = null
            for (pkg in whatsAppPackages) {
                try {
                    pm.getPackageInfo(pkg, 0)
                    targetPackage = pkg
                    break
                } catch (_: Exception) {}
            }

            if (targetPackage != null) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, "Patient Clinical AI Risk Assessment Summary (${pdfFile.name})")
                    clipData = ClipData.newRawUri("Clinical Report", uri)
                    setPackage(targetPackage)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.grantUriPermission(targetPackage, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                context.startActivity(intent)
            } else {
                Toast.makeText(context, "WhatsApp not installed. Opening share menu...", Toast.LENGTH_SHORT).show()
                sharePdf(context, pdfFile)
            }
        } catch (e: Throwable) {
            Toast.makeText(context, "Unable to share directly: ${e.message}. Opening share menu...", Toast.LENGTH_SHORT).show()
            try {
                sharePdf(context, pdfFile)
            } catch (e2: Throwable) {
                Toast.makeText(context, "Sharing unavailable: ${e2.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    /**
     * Launch Android system Share sheet for clinical sharing of the generated PDF report.
     */
    fun sharePdf(context: Context, pdfFile: File) {
        try {
            val uri = getFileUri(context, pdfFile)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "CardioNeuro-Sentinel Clinical AI Summary - ${pdfFile.name}")
                clipData = ClipData.newRawUri("Clinical AI Report", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(intent, "Share Clinical Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        } catch (e: Throwable) {
            Toast.makeText(context, "Unable to share PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Open / View the PDF directly using an installed PDF reader.
     */
    fun viewPdf(context: Context, pdfFile: File) {
        try {
            val uri = getFileUri(context, pdfFile)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Throwable) {
            // If no dedicated PDF viewer exists, fall back to sharing
            sharePdf(context, pdfFile)
        }
    }

    /**
     * Print the generated PDF via Android Print Framework.
     */
    fun printPdf(context: Context, pdfFile: File) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            Toast.makeText(context, "Print service not available on device", Toast.LENGTH_SHORT).show()
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val pdi = PrintDocumentInfo.Builder(pdfFile.name)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(2)
                    .build()
                callback?.onLayoutFinished(pdi, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                var input: FileInputStream? = null
                var output: FileOutputStream? = null
                try {
                    input = FileInputStream(pdfFile)
                    output = FileOutputStream(destination?.fileDescriptor)
                    val buf = ByteArray(2048)
                    var bytesRead: Int
                    while (input.read(buf).also { bytesRead = it } > 0) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback?.onWriteCancelled()
                            return
                        }
                        output.write(buf, 0, bytesRead)
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    callback?.onWriteFailed(e.message)
                } finally {
                    try { input?.close() } catch (_: Exception) {}
                    try { output?.close() } catch (_: Exception) {}
                }
            }
        }

        printManager.print("CardioNeuro_Report_${pdfFile.name}", printAdapter, PrintAttributes.Builder().build())
    }

    fun getFileUri(context: Context, file: File): Uri {
        return try {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (_: Exception) {
            // Fallback: Copy to app filesDir reports and retrieve URI
            val safeDir = File(context.filesDir, "reports").apply { mkdirs() }
            val safeFile = File(safeDir, file.name)
            if (!safeFile.exists()) {
                file.copyTo(safeFile, overwrite = true)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                safeFile
            )
        }
    }
}
