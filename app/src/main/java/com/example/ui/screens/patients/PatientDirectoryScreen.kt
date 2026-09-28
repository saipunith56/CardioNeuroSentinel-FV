package com.example.ui.screens.patients

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Patient
import com.example.ui.components.MetricStatBox
import com.example.ui.theme.MedicalBadgeBg
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
import com.example.ui.theme.MedicalSubtleBg
import com.example.ui.theme.MedicalSurface
import com.example.ui.theme.MedicalTeal
import com.example.ui.theme.MedicalTextMuted
import com.example.ui.theme.MedicalTextPrimary
import com.example.ui.theme.MedicalTextSecondary
import com.example.ui.theme.RiskRed
import com.example.ui.viewmodel.PatientViewModel

@Composable
fun PatientDirectoryScreen(
    viewModel: PatientViewModel,
    onNavigateBack: () -> Unit,
    onSelectPatientForScan: (Patient) -> Unit,
    onNavigateToPatientDetail: (Patient) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalPatients by viewModel.totalPatientsCount.collectAsState()
    val activeCases by viewModel.activeCasesCount.collectAsState()
    val patients by viewModel.filteredPatients.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MedicalBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_patient_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Patient")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MedicalTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Patient Directory",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                }
            }

            // Metric Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricStatBox(
                        title = "Total Patients",
                        value = totalPatients.toString(),
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    MetricStatBox(
                        title = "Active Cases",
                        value = activeCases.toString(),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("patient_search_input"),
                    placeholder = {
                        Text(
                            text = "Search name, MRN, condition...",
                            fontSize = 13.sp,
                            color = MedicalTextMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MedicalBlue
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MedicalSurface,
                        unfocusedContainerColor = MedicalSurface,
                        focusedBorderColor = MedicalBlue,
                        unfocusedBorderColor = MedicalCardBorder,
                        focusedTextColor = MedicalTextPrimary,
                        unfocusedTextColor = MedicalTextPrimary,
                        cursorColor = MedicalBlue
                    ),
                    singleLine = true
                )
            }

            // Section Heading: All Patients
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ShowChart,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "All Patients",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${patients.size} record",
                        fontSize = 12.sp,
                        color = MedicalTextSecondary
                    )
                }
            }

            // Patient Cards
            items(patients) { patient ->
                PatientDirectoryCard(
                    patient = patient,
                    onInspect = { onNavigateToPatientDetail(patient) },
                    onNewScan = { onSelectPatientForScan(patient) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, mrn, age, gender, bp, chol, bloodGroup, condition ->
                viewModel.createPatient(name, mrn, age, gender, bp, chol, bloodGroup, condition)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun PatientDirectoryCard(
    patient: Patient,
    onInspect: () -> Unit,
    onNewScan: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MedicalCardBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onInspect)
            .testTag("patient_card_${patient.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MedicalSurface),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Avatar, Name/MRN, Inspect Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MedicalBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = patient.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalTextPrimary
                    )
                    Text(
                        text = "${patient.gender}, ${patient.age} y/o  •  ${patient.mrn}",
                        fontSize = 11.sp,
                        color = MedicalTextSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MedicalBadgeBg)
                        .clickable(onClick = onInspect),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Inspect Patient",
                        tint = MedicalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Metric Boxes: Blood Pressure, Cholesterol, Blood Group
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MedicalSubtleBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Blood Pressure", fontSize = 10.sp, color = MedicalTextSecondary)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = patient.bloodPressure,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiskRed
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MedicalSubtleBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Cholesterol", fontSize = 10.sp, color = MedicalTextSecondary)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "${patient.cholesterol} mg/dL",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalBlue
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MedicalSubtleBg)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(text = "Blood Group", fontSize = 10.sp, color = MedicalTextSecondary)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = patient.bloodGroup,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MedicalTeal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Condition Tag and Action Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Condition Tag with Heart Icon
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, MedicalCardBorder, RoundedCornerShape(8.dp))
                        .background(MedicalSubtleBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MedicalBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = patient.conditionTag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MedicalTextPrimary
                        )
                    }
                }

                // New Scan Action
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onNewScan)
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("new_scan_button_${patient.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New Scan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MedicalBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MedicalBlue,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
