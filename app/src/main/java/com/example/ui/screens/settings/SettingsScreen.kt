package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ThemePreferences
import com.example.ui.components.HeroGradientCard
import com.example.ui.components.StatusPill
import com.example.ui.theme.MedicalBadgeBg
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
import com.example.ui.theme.MedicalSubtleBg
import com.example.ui.theme.MedicalSurface
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTextMuted
import com.example.ui.theme.MedicalTextPrimary
import com.example.ui.theme.MedicalTextSecondary
import com.example.ui.theme.RiskGreen
import com.example.ui.theme.RiskGreenBg
import com.example.ui.theme.RiskOrange
import com.example.ui.theme.RiskOrangeBg
import com.example.ui.theme.StatusPurple
import com.example.ui.theme.StatusPurpleBg
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    themePreferences: ThemePreferences? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember(themePreferences, context) {
        themePreferences ?: ThemePreferences.getInstance(context)
    }

    var privacyEpsilon by remember { mutableFloatStateOf(0.50f) }

    fun updateTheme(enableDark: Boolean) {
        onToggleDarkTheme(enableDark)
        coroutineScope.launch {
            prefs.setDarkTheme(enableDark)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MedicalTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "Settings & Governance",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "System appearance, on-device privacy budget & local ML engine",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                }
            }
        }

        // Hero Gradient Card
        item {
            HeroGradientCard(
                title = "CardioNeuro Sentinel Governance",
                subtitle = "Configure on-device display theme, differential privacy thresholds, and local ML verification policies.",
                trailingIcon = Icons.Default.Security
            )
        }

        // Section 1: Display & Interface Theme
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MedicalBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = null,
                                    tint = MedicalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Interface Theme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = if (isDarkTheme) "Dark Active" else "Light Active",
                            textColor = if (isDarkTheme) Color(0xFF38BDF8) else MedicalBlue,
                            bgColor = MedicalBadgeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Dual Visual Mode Selector Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThemeSelectorBox(
                            title = "Clinical Light",
                            subtitle = "Daylight high-contrast",
                            icon = Icons.Default.LightMode,
                            isSelected = !isDarkTheme,
                            onClick = { updateTheme(false) },
                            modifier = Modifier.weight(1f).testTag("select_light_theme")
                        )

                        ThemeSelectorBox(
                            title = "Clinical Dark",
                            subtitle = "Zero-glare nocturnal",
                            icon = Icons.Default.DarkMode,
                            isSelected = isDarkTheme,
                            onClick = { updateTheme(true) },
                            modifier = Modifier.weight(1f).testTag("select_dark_theme")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Switch Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MedicalSubtleBg)
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Dark Mode (Nocturnal Clinical)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isDarkTheme)
                                    "Active: Low-glare dark palette tailored for intensive care suites and nocturnal clinical rounds. All surfaces and text adapt for maximum eye comfort."
                                else
                                    "Inactive: High-contrast daylight palette for standard consultation rooms and daylight clinical review.",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { checked ->
                                updateTheme(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MedicalBlue,
                                uncheckedThumbColor = MedicalTextSecondary,
                                uncheckedTrackColor = MedicalCardBorder
                            ),
                            modifier = Modifier.testTag("theme_switch")
                        )
                    }
                }
            }
        }

        // Section 2: On-Device Differential Privacy Budget
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MedicalBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = MedicalTeal,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Differential Privacy Budget",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = "ε = ${String.format(Locale.US, "%.2f", privacyEpsilon)}",
                            textColor = MedicalTeal,
                            bgColor = MedicalBadgeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Noise Scale: γ = 0.02 (Calibrated Gaussian)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MedicalTextSecondary
                        )
                        Text(
                            text = if (privacyEpsilon <= 0.5f) "High Anonymization" else "Standard Privacy",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (privacyEpsilon <= 0.5f) RiskGreen else MedicalBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Slider(
                        value = privacyEpsilon,
                        onValueChange = { privacyEpsilon = it },
                        valueRange = 0.1f..2.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MedicalTeal,
                            activeTrackColor = MedicalTeal,
                            inactiveTrackColor = MedicalCardBorder
                        ),
                        modifier = Modifier.testTag("privacy_budget_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ε 0.1 (Maximum Anonymity)", fontSize = 10.sp, color = MedicalTextMuted)
                        Text("ε 2.0 (High Model Utility)", fontSize = 10.sp, color = MedicalTextMuted)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MedicalSubtleBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "All patient telemetry and records remain strictly on-device. Encrypted parameter gradients are clipped and perturbed with Gaussian noise before federated model consensus (Zero raw PHI transmission).",
                            fontSize = 10.sp,
                            color = MedicalTextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Section 3: Multimodal AI Architecture & On-Device Processing
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MedicalBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Memory,
                                    contentDescription = null,
                                    tint = MedicalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Multimodal AI Architecture",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MedicalTextPrimary
                                )
                                Text(
                                    text = "100% On-Device Neural & Ensembles",
                                    fontSize = 11.sp,
                                    color = MedicalTextSecondary
                                )
                            }
                        }
                        StatusPill(
                            text = "LOCAL ML ACTIVE",
                            textColor = RiskGreen,
                            bgColor = RiskGreenBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ArchitectureStatusRow(
                        title = "DS1 Neuroimaging Pipeline",
                        description = "2D-CNN MRI/CT DWI Acute Ischemia Slice Classifier",
                        status = "READY",
                        statusColor = RiskGreen
                    )
                    ArchitectureStatusRow(
                        title = "DS2 Cardiovascular Model",
                        description = "XGBoost Hemodynamic & Biomarker Risk Estimator",
                        status = "READY",
                        statusColor = RiskGreen
                    )
                    ArchitectureStatusRow(
                        title = "DS3 Cerebrovascular Model",
                        description = "Random Forest Stroke Probability & TOAST Classifier",
                        status = "READY",
                        statusColor = RiskGreen
                    )
                    ArchitectureStatusRow(
                        title = "Heart-Brain Crosstalk",
                        description = "Graph Neural Network (GNN) Bidirectional Coupling",
                        status = "READY",
                        statusColor = RiskGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MedicalSubtleBg)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "Local Model Governance: All neural inferences and clinical scoring operate completely on-device without cloud dependence or external telemetry. Predictions are reproducible and deterministic.",
                            fontSize = 10.sp,
                            color = MedicalTextSecondary,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        // Section 4: Federated Consortium Nodes
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Hub,
                    contentDescription = null,
                    tint = MedicalBlue,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Federated Node Consortium",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "Encrypted model consensus network",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                }
            }
        }

        // Consortium Node 1: Mayo Clinical Sentinel Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MedicalBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = MedicalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Mayo Clinical Sentinel Hub",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = "ONLINE",
                            textColor = RiskGreen,
                            bgColor = RiskGreenBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Local Epochs", fontSize = 10.sp, color = MedicalTextSecondary)
                            Text("142", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MedicalBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Last Weight Hash", fontSize = 10.sp, color = MedicalTextSecondary)
                            Text("0x8f1e...4a2c", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MedicalTextPrimary)
                        }
                    }
                }
            }
        }

        // Consortium Node 2: Charité Berlin Neuro-Cardio Unit
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MedicalBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Hub,
                                    contentDescription = null,
                                    tint = MedicalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Charité Berlin Neuro-Cardio Unit",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                        }
                        StatusPill(
                            text = "SYNCHRONIZING",
                            textColor = MedicalTeal,
                            bgColor = MedicalBadgeBg
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Local Epochs", fontSize = 10.sp, color = MedicalTextSecondary)
                            Text("198", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MedicalBlue)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Last Weight Hash", fontSize = 10.sp, color = MedicalTextSecondary)
                            Text("0x3b7d...9e10", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MedicalTextPrimary)
                        }
                    }
                }
            }
        }

        // Section 5: Local Clinical Storage & Compliance
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MedicalCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MedicalSurface),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MedicalBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = MedicalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Local Storage & Regulatory Compliance",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MedicalTextPrimary
                            )
                            Text(
                                text = "On-Device Room Database • SQLite Encrypted",
                                fontSize = 11.sp,
                                color = MedicalTextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "• Local Storage: All patient records, risk assessments, and clinical PDF summaries remain on-device in secure application sandboxes.",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Clinical Governance: CardioNeuro Sentinel operates as an on-device clinical decision support prototype under research governance. All outputs require certified physician verification.",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ThemeSelectorBox(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) MedicalBlue else MedicalCardBorder
    val bgColor = if (isSelected) MedicalBadgeBg else MedicalSubtleBg

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(if (isSelected) 1.8.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MedicalBlue else MedicalTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MedicalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MedicalTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = MedicalTextSecondary
            )
        }
    }
}

@Composable
private fun ArchitectureStatusRow(
    title: String,
    description: String,
    status: String,
    statusColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MedicalTextPrimary
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = MedicalTextSecondary
            )
        }
        Text(
            text = status,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = statusColor
        )
    }
}
