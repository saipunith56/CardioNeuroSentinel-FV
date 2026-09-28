package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ThemePreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.model.Patient
import com.example.data.repository.AssessmentRepository
import com.example.data.repository.PatientRepository
import com.example.ui.screens.assessment.ClinicalAiAssessmentScreen
import com.example.ui.screens.assessment.NewDiagnosticAssessmentScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.patients.PatientDetailScreen
import com.example.ui.screens.patients.PatientDirectoryScreen
import com.example.ui.screens.reports.ReportsScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.theme.MedicalBlue
import com.example.ui.theme.MedicalCardBorder
import com.example.ui.theme.MedicalSurface
import com.example.ui.theme.MedicalTextMuted
import com.example.ui.theme.MedicalTextPrimary
import com.example.ui.theme.MedicalTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AssessmentViewModel
import com.example.ui.viewmodel.DashboardViewModel
import com.example.ui.viewmodel.PatientViewModel
import com.example.ui.viewmodel.RiskAssessmentViewModel

sealed class Screen {
    object Dashboard : Screen()
    object Patients : Screen()
    object Predict : Screen()
    object Settings : Screen()
    data class PatientDetail(val patient: Patient) : Screen()
    data class AssessmentResult(val assessmentId: Long) : Screen()
    object Reports : Screen()
}

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getInstance(applicationContext) }
    private val assessmentRepository by lazy { AssessmentRepository(database.assessmentDao()) }
    private val patientRepository by lazy { PatientRepository(database.patientDao(), database.assessmentDao()) }

    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModel.Factory(patientRepository, assessmentRepository)
    }

    private val patientViewModel: PatientViewModel by viewModels {
        PatientViewModel.Factory(patientRepository)
    }

    private val assessmentViewModel: AssessmentViewModel by viewModels {
        AssessmentViewModel.Factory(assessmentRepository)
    }

    private val riskAssessmentViewModel: RiskAssessmentViewModel by viewModels {
        RiskAssessmentViewModel.Factory(assessmentRepository)
    }

    private val themePreferences by lazy { ThemePreferences.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDarkTheme = runBlocking {
            themePreferences.getInitialDarkTheme()
        }

        setContent {
            val storedDarkTheme by themePreferences.isDarkThemeFlow.collectAsStateWithLifecycle(
                initialValue = initialDarkTheme
            )
            var runtimeDarkTheme by remember { mutableStateOf<Boolean?>(null) }
            val isDarkTheme = runtimeDarkTheme ?: storedDarkTheme
            val coroutineScope = rememberCoroutineScope()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContainer(
                    dashboardViewModel = dashboardViewModel,
                    patientViewModel = patientViewModel,
                    assessmentViewModel = assessmentViewModel,
                    riskAssessmentViewModel = riskAssessmentViewModel,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { enabled ->
                        runtimeDarkTheme = enabled
                        coroutineScope.launch {
                            themePreferences.setDarkTheme(enabled)
                        }
                    },
                    themePreferences = themePreferences
                )
            }
        }
    }
}

@Composable
fun MainAppContainer(
    dashboardViewModel: DashboardViewModel,
    patientViewModel: PatientViewModel,
    assessmentViewModel: AssessmentViewModel,
    riskAssessmentViewModel: RiskAssessmentViewModel? = null,
    isDarkTheme: Boolean = false,
    onToggleDarkTheme: (Boolean) -> Unit = {},
    themePreferences: ThemePreferences? = null
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }
    var screenStack by remember { mutableStateOf(listOf<Screen>(Screen.Dashboard)) }

    fun navigateTo(screen: Screen) {
        screenStack = screenStack + screen
        currentScreen = screen
    }

    fun navigateBack() {
        if (screenStack.size > 1) {
            val newStack = screenStack.dropLast(1)
            screenStack = newStack
            currentScreen = newStack.last()
        } else {
            currentScreen = Screen.Dashboard
            screenStack = listOf(Screen.Dashboard)
        }
    }

    val isTopLevelScreen = currentScreen in listOf(
        Screen.Dashboard,
        Screen.Patients,
        Screen.Predict,
        Screen.Settings
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (isTopLevelScreen) {
                StitchBottomNavigation(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        screenStack = listOf(screen)
                        currentScreen = screen
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
                when (screen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            viewModel = dashboardViewModel,
                            riskViewModel = riskAssessmentViewModel,
                            onNavigateToPatients = {
                                navigateTo(Screen.Patients)
                            },
                            onNavigateToAssessment = { id ->
                                navigateTo(Screen.AssessmentResult(id))
                            },
                            onNavigateToReports = {
                                navigateTo(Screen.Reports)
                            }
                        )
                    }

                    is Screen.Patients -> {
                        BackHandler {
                            navigateTo(Screen.Dashboard)
                        }
                        PatientDirectoryScreen(
                            viewModel = patientViewModel,
                            onNavigateBack = {
                                navigateTo(Screen.Dashboard)
                            },
                            onSelectPatientForScan = { patient ->
                                assessmentViewModel.setPatient(patient)
                                navigateTo(Screen.Predict)
                            },
                            onNavigateToPatientDetail = { patient ->
                                navigateTo(Screen.PatientDetail(patient))
                            }
                        )
                    }

                    is Screen.Predict -> {
                        BackHandler {
                            navigateTo(Screen.Dashboard)
                        }
                        NewDiagnosticAssessmentScreen(
                            viewModel = assessmentViewModel,
                            onNavigateBack = {
                                navigateTo(Screen.Dashboard)
                            },
                            onAssessmentGenerated = { id ->
                                navigateTo(Screen.AssessmentResult(id))
                            }
                        )
                    }

                    is Screen.Settings -> {
                        BackHandler {
                            navigateTo(Screen.Dashboard)
                        }
                        SettingsScreen(
                            onNavigateBack = {
                                navigateTo(Screen.Dashboard)
                            },
                            isDarkTheme = isDarkTheme,
                            onToggleDarkTheme = onToggleDarkTheme,
                            themePreferences = themePreferences
                        )
                    }

                    is Screen.PatientDetail -> {
                        BackHandler {
                            navigateBack()
                        }
                        PatientDetailScreen(
                            patient = screen.patient,
                            onNavigateBack = { navigateBack() },
                            onStartNewAssessment = { patient ->
                                assessmentViewModel.setPatient(patient)
                                navigateTo(Screen.Predict)
                            },
                            onDeletePatient = { patientId ->
                                patientViewModel.deletePatient(patientId)
                                navigateBack()
                            }
                        )
                    }

                    is Screen.AssessmentResult -> {
                        BackHandler {
                            navigateBack()
                        }
                        ClinicalAiAssessmentScreen(
                            viewModel = assessmentViewModel,
                            assessmentId = screen.assessmentId,
                            onNavigateBack = { navigateBack() }
                        )
                    }

                    is Screen.Reports -> {
                        BackHandler {
                            navigateBack()
                        }
                        ReportsScreen(
                            viewModel = dashboardViewModel,
                            onNavigateBack = { navigateBack() },
                            onSelectAssessment = { id ->
                                navigateTo(Screen.AssessmentResult(id))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StitchBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .navigationBarsPadding()
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Dashboard",
                isSelected = currentScreen is Screen.Dashboard,
                testTag = "nav_dashboard",
                onClick = { onNavigate(Screen.Dashboard) }
            )
            BottomNavItem(
                icon = Icons.Default.People,
                label = "Patients",
                isSelected = currentScreen is Screen.Patients,
                testTag = "nav_patients",
                onClick = { onNavigate(Screen.Patients) }
            )
            BottomNavItem(
                icon = Icons.Default.Psychology,
                label = "Predict",
                isSelected = currentScreen is Screen.Predict,
                testTag = "nav_predict",
                onClick = { onNavigate(Screen.Predict) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentScreen is Screen.Settings,
                testTag = "nav_settings",
                onClick = { onNavigate(Screen.Settings) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    val activeContainer = MaterialTheme.colorScheme.secondaryContainer
    val activeColor = MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) activeContainer else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) activeColor else inactiveColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else inactiveColor
        )
    }
}
