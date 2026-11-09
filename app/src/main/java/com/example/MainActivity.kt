package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CockpitViewModel
import com.example.ui.components.TicketBottomSheet
import com.example.ui.components.WorkoutBuilderScreen
import com.example.ui.screens.CalisthenicsScreen
import com.example.ui.screens.CockpitScreen
import com.example.ui.screens.HistoryCalibrationScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsSurfaceElevated
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary

enum class CockpitTab {
    COCKPIT,
    CALISTHENICS,
    HISTORY
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val vm: CockpitViewModel = viewModel(
                    factory = CockpitViewModel.provideFactory(application)
                )
                OvicxCockpitApp(viewModel = vm)
            }
        }
    }
}

@Composable
fun OvicxCockpitApp(viewModel: CockpitViewModel) {
    var currentTab by rememberSaveable { mutableStateOf(CockpitTab.COCKPIT) }

    val programs by viewModel.programsFlow.collectAsStateWithLifecycle()
    val selectedProgram by viewModel.selectedProgramFlow.collectAsStateWithLifecycle()
    val hudState by viewModel.hudState.collectAsStateWithLifecycle()
    val ticketState by viewModel.ticketState.collectAsStateWithLifecycle()
    val editingProgram by viewModel.editingProgram.collectAsStateWithLifecycle()

    val calisthenicsSummaries by viewModel.calisthenicsSummaryFlow.collectAsStateWithLifecycle()
    val todayCalisthenicsLogs by viewModel.todayCalisthenicsLogsFlow.collectAsStateWithLifecycle()
    val bandSummaries by viewModel.bandSummariesFlow.collectAsStateWithLifecycle()
    val daysSinceAnyBand by viewModel.daysSinceAnyBandSessionFlow.collectAsStateWithLifecycle()
    val calendar28Days by viewModel.calendar28DaysFlow.collectAsStateWithLifecycle()
    val selectedCalendarDay by viewModel.selectedCalendarEpochDay.collectAsStateWithLifecycle()

    // Handle back button on secondary tabs
    if (currentTab != CockpitTab.COCKPIT) {
        BackHandler {
            currentTab = CockpitTab.COCKPIT
        }
    }

    // If custom workout builder is open, display it full screen
    val programBeingEdited = editingProgram
    if (programBeingEdited != null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = RamsBackground
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                WorkoutBuilderScreen(
                    initialProgram = programBeingEdited,
                    onSave = { viewModel.saveCustomWorkout(it) },
                    onDelete = { viewModel.deleteCustomWorkout(it) },
                    onCancel = { viewModel.closeWorkoutBuilder() }
                )
            }
        }
        return
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = RamsBackground,
        bottomBar = {
            // Hide bottom navigation during active spin-bike HUD so wet hands never accidentally switch tabs
            if (!hudState.isActive) {
                NavigationBar(
                    containerColor = RamsSurface,
                    tonalElevation = 0.dp
                ) {
                    NavigationBarItem(
                        selected = currentTab == CockpitTab.COCKPIT,
                        onClick = { currentTab = CockpitTab.COCKPIT },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = stringResource(R.string.nav_cockpit)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_cockpit),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RamsAmberAttack,
                            selectedTextColor = RamsTextPrimary,
                            indicatorColor = RamsSurfaceElevated,
                            unselectedIconColor = RamsTextSecondary,
                            unselectedTextColor = RamsTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_cockpit")
                    )

                    NavigationBarItem(
                        selected = currentTab == CockpitTab.CALISTHENICS,
                        onClick = { currentTab = CockpitTab.CALISTHENICS },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.FitnessCenter,
                                contentDescription = stringResource(R.string.nav_calisthenics)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_calisthenics),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RamsAmberAttack,
                            selectedTextColor = RamsTextPrimary,
                            indicatorColor = RamsSurfaceElevated,
                            unselectedIconColor = RamsTextSecondary,
                            unselectedTextColor = RamsTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_calisthenics")
                    )

                    NavigationBarItem(
                        selected = currentTab == CockpitTab.HISTORY,
                        onClick = { currentTab = CockpitTab.HISTORY },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = stringResource(R.string.nav_history)
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.nav_history),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RamsAmberAttack,
                            selectedTextColor = RamsTextPrimary,
                            indicatorColor = RamsSurfaceElevated,
                            unselectedIconColor = RamsTextSecondary,
                            unselectedTextColor = RamsTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_history")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                CockpitTab.COCKPIT -> {
                    CockpitScreen(
                        programs = programs,
                        selectedProgram = selectedProgram,
                        hudState = hudState,
                        onSelectProgram = { viewModel.selectProgram(it) },
                        onRequestStartWorkout = { viewModel.requestStartWorkoutWithTicket() },
                        onOpenNewWorkoutBuilder = { viewModel.openNewCustomWorkoutBuilder() },
                        onOpenEditWorkoutBuilder = { viewModel.openDuplicateOrEditProgram(it) },
                        onTogglePauseResume = { viewModel.togglePauseResume() },
                        onAdjustTime = { viewModel.adjustCurrentStepTime(it) },
                        onSkipNextStep = { viewModel.skipToNextStep() },
                        onFinishWorkout = { viewModel.finishWorkoutEarly(saveToLog = true) },
                        onToggleSoundCues = { viewModel.toggleSoundCues() },
                        onToggleVibration = { viewModel.toggleVibration() },
                        onToggleMetronomeClick = { viewModel.toggleMetronomeClick() }
                    )
                }

                CockpitTab.CALISTHENICS -> {
                    CalisthenicsScreen(
                        summaries = calisthenicsSummaries,
                        todayLogs = todayCalisthenicsLogs,
                        bandSummaries = bandSummaries,
                        daysSinceAnyBandSession = daysSinceAnyBand,
                        onQuickAddReps = { ex, reps -> viewModel.logQuickCalisthenics(ex, reps) },
                        onDeleteCalisthenicsLog = { viewModel.deleteCalisthenicsEntry(it) },
                        onUpdateCustomSteps = { ex, a, b, c ->
                            viewModel.updateCustomStepsForExercise(ex, a, b, c)
                        },
                        onLogBandMaxSet = { type, band, reps ->
                            viewModel.logBandMaxSet(type, band, reps)
                        },
                        onDeleteBandSet = { viewModel.deleteBandMaxSet(it) }
                    )
                }

                CockpitTab.HISTORY -> {
                    HistoryCalibrationScreen(
                        calendar28Days = calendar28Days,
                        selectedEpochDay = selectedCalendarDay,
                        onSelectDay = { viewModel.selectCalendarDay(it) }
                    )
                }
            }

            // Pre-workout / Post-workout "Входной / Выходной билет" Bottom Sheet
            TicketBottomSheet(
                ticketState = ticketState,
                onAddReps = { exercise, reps ->
                    viewModel.addTicketCalisthenics(exercise, reps)
                },
                onDismissAndProceed = {
                    viewModel.dismissTicketAndProceed()
                }
            )
        }
    }
}
