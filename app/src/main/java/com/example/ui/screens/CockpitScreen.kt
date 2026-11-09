package com.example.ui.screens

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WorkoutProgram
import com.example.ui.ActiveWorkoutHudState
import com.example.ui.components.DialGaugeWidget
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsAmberContainer
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsOutline
import com.example.ui.theme.RamsSageContainer
import com.example.ui.theme.RamsSageRecovery
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsSurfaceElevated
import com.example.ui.theme.RamsTextMuted
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary
import com.example.ui.theme.RamsWarmYellow

@Composable
fun CockpitScreen(
    programs: List<WorkoutProgram>,
    selectedProgram: WorkoutProgram,
    hudState: ActiveWorkoutHudState,
    onSelectProgram: (String) -> Unit,
    onRequestStartWorkout: () -> Unit,
    onOpenNewWorkoutBuilder: () -> Unit,
    onOpenEditWorkoutBuilder: (WorkoutProgram) -> Unit,
    onTogglePauseResume: () -> Unit,
    onAdjustTime: (Int) -> Unit,
    onSkipNextStep: () -> Unit,
    onFinishWorkout: () -> Unit,
    onToggleSoundCues: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleMetronomeClick: () -> Unit
) {
    if (hudState.isActive) {
        ActiveCockpitHudView(
            hudState = hudState,
            onTogglePauseResume = onTogglePauseResume,
            onAdjustTime = onAdjustTime,
            onSkipNextStep = onSkipNextStep,
            onFinishWorkout = onFinishWorkout,
            onToggleSoundCues = onToggleSoundCues,
            onToggleVibration = onToggleVibration,
            onToggleMetronomeClick = onToggleMetronomeClick
        )
    } else {
        PreFlightCockpitSelectorView(
            programs = programs,
            selectedProgram = selectedProgram,
            onSelectProgram = onSelectProgram,
            onRequestStartWorkout = onRequestStartWorkout,
            onOpenNewWorkoutBuilder = onOpenNewWorkoutBuilder,
            onOpenEditWorkoutBuilder = onOpenEditWorkoutBuilder
        )
    }
}

@Composable
private fun ActiveCockpitHudView(
    hudState: ActiveWorkoutHudState,
    onTogglePauseResume: () -> Unit,
    onAdjustTime: (Int) -> Unit,
    onSkipNextStep: () -> Unit,
    onFinishWorkout: () -> Unit,
    onToggleSoundCues: () -> Unit,
    onToggleVibration: () -> Unit,
    onToggleMetronomeClick: () -> Unit
) {
    val context = LocalContext.current
    // Keep screen on during active spin-bike workout
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    val currentStep = hudState.currentStep
    val nextStep = hudState.nextStep
    val isAttack = currentStep.phaseType.isHighIntensity
    val phaseAccent = if (isAttack) RamsAmberAttack else RamsSageRecovery

    val pulseScale by animateFloatAsState(
        targetValue = if (hudState.isRightFootBeat) 1.24f else 0.88f,
        animationSpec = tween(durationMillis = 160),
        label = "rpm_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RamsBackground)
            .padding(horizontal = 18.dp, vertical = 10.dp)
            .testTag("active_cockpit_hud"),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 1. TOP INSTRUMENT HEADER: Workout Progress & Audio/Tactile Toggles
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = hudState.program.title.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${currentStep.roundBadgeText}  •  Шаг ${hudState.currentStepIndex + 1}/${hudState.flattenedSteps.size}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RamsTextPrimary
                    )
                }

                // Quick Toggles: Sound Cues, Vibration, finish button
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HudHeaderChip(
                        active = hudState.soundCuesEnabled,
                        label = "ЗВУК",
                        onClick = onToggleSoundCues
                    )
                    HudHeaderChip(
                        active = hudState.vibrationEnabled,
                        label = "ВИБРО",
                        onClick = onToggleVibration
                    )
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(RamsSurface)
                            .clickable(onClick = onFinishWorkout)
                            .padding(horizontal = 10.dp)
                            .testTag("finish_workout_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Стоп",
                                tint = RamsAmberAttack,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "СТОП",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = RamsTextPrimary
                            )
                        }
                    }
                }
            }

            // Overall Workout Linear Progress Bar
            LinearProgressIndicator(
                progress = { hudState.overallProgressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = phaseAccent,
                trackColor = RamsSurface
            )
        }

        // 2. MAIN VISUAL ANCHOR IN CENTER: Huge Monospace Tabular Countdown + Phase Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(phaseAccent)
                )
                Text(
                    text = currentStep.title.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = phaseAccent
                )
                if (hudState.isPaused) {
                    Text(
                        text = "• ПАУЗА",
                        style = MaterialTheme.typography.labelLarge,
                        color = RamsWarmYellow
                    )
                }
            }

            // Giant Tabular Timer (tnum prevents jitter on second changes)
            Text(
                text = formatMmSs(hudState.stepRemainingSec),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 84.sp,
                    lineHeight = 86.sp,
                    fontFeatureSettings = "tnum"
                ),
                color = if (hudState.isLastThreeSeconds) RamsAmberAttack else RamsTextPrimary,
                modifier = Modifier.testTag("main_phase_countdown")
            )

            // Current Hand Position & Coach Note directly below the main anchor
            Box(
                modifier = Modifier
                    .background(RamsSurface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentStep.handPosition.label,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = RamsTextPrimary
                    )
                    if (currentStep.coachNote.isNotBlank()) {
                        Text(
                            text = currentStep.coachNote,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RamsTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 3. CANVAS DIAL WIDGET ("Положение рычажка")
        DialGaugeWidget(
            currentDial = currentStep.dialPosition,
            previousDial = currentStep.previousDialPosition,
            deltaInstruction = currentStep.deltaInstruction,
            isAttackPhase = isAttack,
            highlightDelta = hudState.showDeltaHighlightBanner,
            dialSize = 142.dp
        )

        // 4. CADENCE (RPM) & METRONOME BAR + FIXED-HEIGHT NEXT PHASE PREVIEW (minHeight = 64.dp)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val isUrgentCountdown = hudState.isLastFiveSeconds
            val bannerBg = when {
                nextStep == null -> RamsSurface
                hudState.isLastThreeSeconds -> RamsAmberContainer
                isUrgentCountdown -> RamsSageContainer
                else -> RamsSurface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 64.dp)
                    .background(bannerBg, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("next_phase_banner"),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (nextStep != null) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                hudState.isLastThreeSeconds -> "ПРИГОТОВЬСЯ: ${hudState.stepRemainingSec}... СТАРТ!"
                                isUrgentCountdown -> "СЛЕДУЮЩАЯ ФАЗА ЧЕРЕЗ ${hudState.stepRemainingSec} СЕК"
                                else -> "СЛЕДУЮЩИЙ ШАГ ПО РАСПИСАНИЮ"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isUrgentCountdown) RamsAmberAttack else RamsTextSecondary
                        )
                        Text(
                            text = "${nextStep.title} • ${nextStep.dialPosition.shortLabel} • ${nextStep.rpmRangeText}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isUrgentCountdown) RamsTextPrimary else RamsTextSecondary
                        )
                    }
                    Text(
                        text = nextStep.handPosition.label.substringBefore(" •"),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isUrgentCountdown) RamsAmberAttack else RamsTextMuted
                    )
                } else {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isUrgentCountdown) {
                                "ФИНИШ ЧЕРЕЗ ${hudState.stepRemainingSec} СЕК"
                            } else {
                                "ФИНАЛЬНЫЙ ОТРЕЗОК ТРЕНИРОВКИ"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isUrgentCountdown) RamsSageRecovery else RamsTextSecondary
                        )
                        Text(
                            text = "После завершения откроется итог заезда и Выходной билет",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RamsTextSecondary
                        )
                    }
                }
            }

            // Target RPM & Visual + Audio Metronome Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RamsSurface, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ЦЕЛЕВОЙ КАДЕНС",
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsTextSecondary
                    )
                    Text(
                        text = currentStep.rpmRangeText,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = RamsTextPrimary
                    )
                }

                // Pulsing Left / Right Foot Visual Metronome (RPM * 2)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FootPulseIndicator(
                        label = "ЛЕВ",
                        isActive = !hudState.isRightFootBeat && !hudState.isPaused,
                        accentColor = phaseAccent,
                        scale = if (!hudState.isRightFootBeat) pulseScale else 1f
                    )
                    FootPulseIndicator(
                        label = "ПРАВ",
                        isActive = hudState.isRightFootBeat && !hudState.isPaused,
                        accentColor = phaseAccent,
                        scale = if (hudState.isRightFootBeat) pulseScale else 1f
                    )
                }

                // Quiet Metronome Click Toggle
                Box(
                    modifier = Modifier
                        .height(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (hudState.metronomeClickEnabled) RamsSageContainer else RamsSurfaceElevated)
                        .clickable(onClick = onToggleMetronomeClick)
                        .padding(horizontal = 12.dp)
                        .testTag("metronome_click_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (hudState.metronomeClickEnabled) {
                                Icons.AutoMirrored.Filled.VolumeUp
                            } else {
                                Icons.AutoMirrored.Filled.VolumeOff
                            },
                            contentDescription = "Клик метронома",
                            tint = if (hudState.metronomeClickEnabled) RamsSageRecovery else RamsTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hudState.metronomeClickEnabled) "КЛИК ВКЛ" else "КЛИК ВЫКЛ",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (hudState.metronomeClickEnabled) RamsSageRecovery else RamsTextSecondary
                        )
                    }
                }
            }
        }

        // 5. BOTTOM HARDWARE CONTROL DECK (Height >= 64.dp for wet hands on the handlebar)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // -10 sec
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(66.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RamsSurface)
                    .clickable { onAdjustTime(-10) }
                    .testTag("btn_minus_10"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-10 сек",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = RamsTextPrimary
                )
            }

            // Pause / Resume (Main wide tactile switch)
            Box(
                modifier = Modifier
                    .weight(1.4f)
                    .height(66.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (hudState.isPaused) RamsSageRecovery else phaseAccent)
                    .clickable(onClick = onTogglePauseResume)
                    .testTag("btn_pause_resume"),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (hudState.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = null,
                        tint = RamsBackground,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (hudState.isPaused) "ПРОДОЛЖИТЬ" else "ПАУЗА",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = RamsBackground
                    )
                }
            }

            // +10 sec
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(66.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RamsSurface)
                    .clickable { onAdjustTime(10) }
                    .testTag("btn_plus_10"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+10 сек",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = RamsTextPrimary
                )
            }

            // Next Step
            Box(
                modifier = Modifier
                    .weight(0.9f)
                    .height(66.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(RamsSurfaceElevated)
                    .clickable(onClick = onSkipNextStep)
                    .testTag("btn_next_step"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Следующий шаг",
                        tint = RamsTextPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = "ШАГ →",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RamsTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun FootPulseIndicator(
    label: String,
    isActive: Boolean,
    accentColor: androidx.compose.ui.graphics.Color,
    scale: Float
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (isActive) accentColor else RamsOutline)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (isActive) RamsTextPrimary else RamsTextMuted
        )
    }
}

@Composable
private fun HudHeaderChip(
    active: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (active) RamsSageContainer else RamsSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = if (active) RamsSageRecovery else RamsTextMuted
        )
    }
}

@Composable
private fun PreFlightCockpitSelectorView(
    programs: List<WorkoutProgram>,
    selectedProgram: WorkoutProgram,
    onSelectProgram: (String) -> Unit,
    onRequestStartWorkout: () -> Unit,
    onOpenNewWorkoutBuilder: () -> Unit,
    onOpenEditWorkoutBuilder: (WorkoutProgram) -> Unit
) {
    val flattened = selectedProgram.flattenSteps()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RamsBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("preflight_cockpit_selector")
    ) {
        // Instrument Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "DFC OVICX Q200-O • БОРТОВОЙ КОМПЬЮТЕР",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsAmberAttack
                )
                Text(
                    text = "Выбор интервальной схемы",
                    style = MaterialTheme.typography.headlineLarge,
                    color = RamsTextPrimary
                )
            }

            OutlinedButton(
                onClick = onOpenNewWorkoutBuilder,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("create_custom_workout_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = RamsAmberAttack,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "СВОЯ СХЕМА",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = RamsTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(programs, key = { it.id }) { program ->
                val isSelected = program.id == selectedProgram.id
                ProgramCardItem(
                    program = program,
                    isSelected = isSelected,
                    onSelect = { onSelectProgram(program.id) },
                    onEditOrCopy = { onOpenEditWorkoutBuilder(program) }
                )
            }

            // Detailed Block Preview of the Selected Program
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RamsSurface, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "СТРУКТУРА: ${selectedProgram.title.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = RamsSageRecovery
                        )
                        Text(
                            text = "${flattened.size} отрезков • ${selectedProgram.totalMinutesRounded} мин",
                            style = MaterialTheme.typography.labelMedium,
                            color = RamsTextSecondary
                        )
                    }

                    selectedProgram.blocks.forEach { block ->
                        val summarySteps = block.steps.joinToString("  →  ") { s ->
                            "${s.title} (${formatCompactSec(s.durationSec)}, ${s.dialPosition.shortLabel}, ${s.minRpm}–${s.maxRpm} RPM)"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = if (block.repeatCount > 1) "${block.repeatCount}×" else "1×",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = if (block.repeatCount > 1) RamsAmberAttack else RamsSageRecovery,
                                modifier = Modifier.width(36.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = block.blockName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = RamsTextPrimary
                                )
                                Text(
                                    text = summarySteps,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = RamsTextSecondary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Primary 64dp Start Button at the bottom of the Cockpit
        Button(
            onClick = onRequestStartWorkout,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .testTag("start_workout_button"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RamsAmberAttack,
                contentColor = RamsBackground
            )
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "НАЧАТЬ: ${selectedProgram.title.uppercase()} (${selectedProgram.totalMinutesRounded} МИН)",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ProgramCardItem(
    program: WorkoutProgram,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEditOrCopy: () -> Unit
) {
    val bgColor = if (isSelected) RamsSurfaceElevated else RamsSurface
    val accentBarColor = if (isSelected) RamsAmberAttack else RamsOutline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .clickable(onClick = onSelect)
            .padding(14.dp)
            .testTag("program_card_${program.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left indicator bar (no Box-in-Box frames)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentBarColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = program.tag,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) RamsAmberAttack else RamsSageRecovery
                )
            }
            Text(
                text = program.title,
                style = MaterialTheme.typography.titleLarge,
                color = RamsTextPrimary
            )
            Text(
                text = program.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = RamsTextSecondary
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(RamsBackground)
                .clickable(onClick = onEditOrCopy)
                .testTag("edit_program_${program.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Настроить или скопировать схему",
                tint = RamsTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun formatMmSs(totalSeconds: Int): String {
    val safe = totalSeconds.coerceAtLeast(0)
    val m = safe / 60
    val s = safe % 60
    return "%02d:%02d".format(m, s)
}

private fun formatCompactSec(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return if (m > 0 && s > 0) "${m}м${s}с" else if (m > 0) "${m}м" else "${s}с"
}
