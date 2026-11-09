package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CalisthenicsLogEntity
import com.example.data.model.BandExerciseType
import com.example.data.model.CalisthenicsExercise
import com.example.ui.BandExerciseSummary
import com.example.ui.CockpitViewModel
import com.example.ui.ExerciseVolumeSummary
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
fun CalisthenicsScreen(
    summaries: List<ExerciseVolumeSummary>,
    todayLogs: List<CalisthenicsLogEntity>,
    bandSummaries: List<BandExerciseSummary>,
    daysSinceAnyBandSession: Int?,
    onQuickAddReps: (CalisthenicsExercise, Int) -> Unit,
    onDeleteCalisthenicsLog: (Long) -> Unit,
    onUpdateCustomSteps: (CalisthenicsExercise, Int, Int, Int) -> Unit,
    onLogBandMaxSet: (BandExerciseType, String, Int) -> Unit,
    onDeleteBandSet: (Long) -> Unit
) {
    var stepDialogExercise by remember { mutableStateOf<ExerciseVolumeSummary?>(null) }

    val totalTodayReps = summaries.sumOf { it.todayReps }
    val totalWeekReps = summaries.sumOf { it.weekReps }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RamsBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("calisthenics_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Header & Daily / Weekly Volume Dashboard
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "АНТИ-ЗАБЫВАЙКА • КАЛИСТЕНИКА БЕЗ ЗАКИСЛЕНИЯ",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsSageRecovery
                )
                Text(
                    text = "Фоновый объём и резинки",
                    style = MaterialTheme.typography.headlineLarge,
                    color = RamsTextPrimary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    VolumeStatBox(
                        label = "СУММА ЗА СЕГОДНЯ",
                        value = "$totalTodayReps",
                        unit = "повторений",
                        accentColor = RamsSageRecovery,
                        modifier = Modifier.weight(1f)
                    )
                    VolumeStatBox(
                        label = "ОБЪЁМ ЗА 7 ДНЕЙ",
                        value = "$totalWeekReps",
                        unit = "повторений",
                        accentColor = RamsAmberAttack,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 2. 1-Tap Quick Counters for the 4 Movements
        items(summaries, key = { it.exercise.name }) { summary ->
            ExerciseQuickCounterRow(
                summary = summary,
                onQuickAdd = { reps -> onQuickAddReps(summary.exercise, reps) },
                onConfigureSteps = { stepDialogExercise = summary }
            )
        }

        // 3. Long Set with Resistance Band (1-2 times per week)
        item {
            Spacer(modifier = Modifier.height(4.dp))
            BandLongSetSection(
                bandSummaries = bandSummaries,
                daysSinceAnyBandSession = daysSinceAnyBandSession,
                onLogBandMaxSet = onLogBandMaxSet,
                onDeleteBandSet = onDeleteBandSet
            )
        }

        // 4. Recent Logs Today (with 1-tap undo/delete if tapped by mistake)
        if (todayLogs.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(RamsSurface, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ЖУРНАЛ ПОДХОДОВ ЗА СЕГОДНЯ",
                        style = MaterialTheme.typography.labelLarge,
                        color = RamsTextSecondary
                    )
                    todayLogs.take(6).forEach { log ->
                        val exTitle = runCatching {
                            CalisthenicsExercise.valueOf(log.exerciseName).title
                        }.getOrDefault(log.exerciseName)
                        val sourceLabel = when (log.sourceTag) {
                            "PRE_TICKET" -> "Входной билет"
                            "POST_TICKET" -> "Выходной билет"
                            else -> "Быстрый счётчик"
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$exTitle  •  +$log.reps повт. ($sourceLabel)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = RamsTextPrimary
                            )
                            IconButton(
                                onClick = { onDeleteCalisthenicsLog(log.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Отменить запись",
                                    tint = RamsTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Custom Step Config Dialog
    stepDialogExercise?.let { current ->
        StepConfigDialog(
            summary = current,
            onDismiss = { stepDialogExercise = null },
            onConfirm = { a, b, c ->
                onUpdateCustomSteps(current.exercise, a, b, c)
                stepDialogExercise = null
            }
        )
    }
}

@Composable
private fun VolumeStatBox(
    label: String,
    value: String,
    unit: String,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(RamsSurface, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = RamsTextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = JetBrainsMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontFeatureSettings = "tnum"
                ),
                color = accentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.bodyMedium,
                color = RamsTextSecondary,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
    }
}

@Composable
private fun ExerciseQuickCounterRow(
    summary: ExerciseVolumeSummary,
    onQuickAdd: (Int) -> Unit,
    onConfigureSteps: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsSurface, RoundedCornerShape(8.dp))
            .padding(14.dp)
            .testTag("calisthenics_card_${summary.exercise.name}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.exercise.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = RamsTextPrimary
                )
                Text(
                    text = summary.exercise.muscleHint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RamsTextSecondary
                )
            }

            IconButton(
                onClick = onConfigureSteps,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Настроить шаг добавления",
                    tint = RamsTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Today & Week totals in tabular monospace
            Column {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${summary.todayReps}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 30.sp,
                            fontFamily = JetBrainsMonoFamily,
                            fontWeight = FontWeight.Bold,
                            fontFeatureSettings = "tnum"
                        ),
                        color = RamsSageRecovery
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "сегодня",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RamsTextSecondary
                    )
                }
                Text(
                    text = "За 7 дней: ${summary.weekReps} повт.",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsTextMuted
                )
            }

            // 1-Tap Quick Add Buttons (+5 / +10 / +15 or customized)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(summary.customStepA, summary.customStepB, summary.customStepC).forEach { step ->
                    Box(
                        modifier = Modifier
                            .height(52.dp)
                            .width(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(RamsSurfaceElevated)
                            .clickable { onQuickAdd(step) }
                            .testTag("quick_add_${summary.exercise.name}_$step"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$step",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                fontWeight = FontWeight.Bold,
                                fontFeatureSettings = "tnum"
                            ),
                            color = RamsTextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BandLongSetSection(
    bandSummaries: List<BandExerciseSummary>,
    daysSinceAnyBandSession: Int?,
    onLogBandMaxSet: (BandExerciseType, String, Int) -> Unit,
    onDeleteBandSet: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsSurface, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .testTag("band_long_set_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header + Days Since Last Session Indicator
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ДЛИННЫЙ ПОДХОД С РЕЗИНКОЙ (1–2 РАЗА В НЕДЕЛЮ)",
                    style = MaterialTheme.typography.labelLarge,
                    color = RamsAmberAttack
                )
                Text(
                    text = "Один максимальный памповый подход для капилляризации",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RamsTextSecondary
                )
            }
        }

        // Prominent indicator: "Прошло X дней с прошлой сессии с резинкой"
        val statusBg = when {
            daysSinceAnyBandSession == null -> RamsAmberContainer
            daysSinceAnyBandSession >= 4 -> RamsAmberContainer
            else -> RamsSageContainer
        }
        val statusColor = when {
            daysSinceAnyBandSession == null -> RamsAmberAttack
            daysSinceAnyBandSession >= 4 -> RamsAmberAttack
            else -> RamsSageRecovery
        }
        val statusText = when (daysSinceAnyBandSession) {
            null -> "Сессий с резинкой ещё не записано — пора сделать первый подход"
            0 -> "Прошло 0 дней с прошлой сессии с резинкой (выполнено сегодня)"
            else -> "Прошло $daysSinceAnyBandSession дн. с прошлой сессии с резинкой"
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(statusBg, RoundedCornerShape(6.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .testTag("days_since_band_indicator")
        ) {
            Text(
                text = statusText.uppercase(),
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = statusColor
            )
        }

        bandSummaries.forEach { summary ->
            BandExerciseCard(
                summary = summary,
                onLogSet = { bandText, reps ->
                    onLogBandMaxSet(summary.exerciseType, bandText, reps)
                },
                onDeleteSet = onDeleteBandSet
            )
        }
    }
}

@Composable
private fun BandExerciseCard(
    summary: BandExerciseSummary,
    onLogSet: (String, Int) -> Unit,
    onDeleteSet: (Long) -> Unit
) {
    var bandText by remember(summary.exerciseType) {
        mutableStateOf(summary.latestSet?.bandDescription ?: summary.exerciseType.defaultBandHint)
    }
    var repsInput by remember(summary.exerciseType, summary.latestSet?.reps) {
        mutableIntStateOf((summary.latestSet?.reps ?: 25).coerceAtLeast(5))
    }

    val latest = summary.latestSet
    val best = summary.bestRecordSet
    val prevBest = summary.previousBestBeforeLatest

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsBackground, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.exerciseType.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RamsTextPrimary
                )
                if (latest != null) {
                    val (day, month, _) = CockpitViewModel.formatEpochDayParts(latest.epochDay)
                    Text(
                        text = "Последний: ${latest.reps} повт. (${latest.bandDescription}) • $day $month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RamsTextSecondary
                    )
                } else {
                    Text(
                        text = "Запиши свой максимальный подход с резинкой",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RamsTextSecondary
                    )
                }
            }

            if (best != null) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "РЕКОРД: ${best.reps}",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RamsSageRecovery
                    )
                    if (latest != null && prevBest != null) {
                        val diff = latest.reps - prevBest
                        val diffLabel = if (diff >= 0) "+$diff к рекорду ($prevBest)" else "$diff от рекорда ($prevBest)"
                        Text(
                            text = diffLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (diff >= 0) RamsAmberAttack else RamsWarmYellow
                        )
                    }
                }
            }
        }

        // Quick preset chips for band color/tension
        val presetBands = listOf(
            "Жёлтая 5–15 кг",
            "Красная 10–20 кг",
            "Фиолетовая 15–30 кг",
            "Чёрная 20–40 кг",
            "Зелёная 25–50 кг"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetBands.forEach { preset ->
                val selected = bandText == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) RamsAmberContainer else RamsSurfaceElevated)
                        .clickable { bandText = preset }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) RamsAmberAttack else RamsTextSecondary
                    )
                }
            }
        }

        OutlinedTextField(
            value = bandText,
            onValueChange = { bandText = it },
            label = { Text("Цвет / натяжение резинки") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RamsAmberAttack,
                unfocusedBorderColor = RamsOutline,
                focusedTextColor = RamsTextPrimary,
                unfocusedTextColor = RamsTextPrimary
            )
        )

        // Reps adjuster + Save button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BandStepBtn("-5") { repsInput = (repsInput - 5).coerceAtLeast(1) }
                BandStepBtn("-1") { repsInput = (repsInput - 1).coerceAtLeast(1) }
                Text(
                    text = "$repsInput повт.",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFamily,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    color = RamsTextPrimary
                )
                BandStepBtn("+1") { repsInput = (repsInput + 1).coerceAtMost(300) }
                BandStepBtn("+5") { repsInput = (repsInput + 5).coerceAtMost(300) }
            }

            Button(
                onClick = { onLogSet(bandText, repsInput) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RamsAmberAttack,
                    contentColor = RamsBackground
                ),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("save_band_${summary.exerciseType.name}")
            ) {
                Text(
                    text = "ЗАПИСАТЬ",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
private fun BandStepBtn(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(RamsSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = RamsTextPrimary
        )
    }
}

@Composable
private fun StepConfigDialog(
    summary: ExerciseVolumeSummary,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, Int) -> Unit
) {
    var stepA by remember { mutableStateOf(summary.customStepA.toString()) }
    var stepB by remember { mutableStateOf(summary.customStepB.toString()) }
    var stepC by remember { mutableStateOf(summary.customStepC.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = RamsSurface,
        titleContentColor = RamsTextPrimary,
        textContentColor = RamsTextSecondary,
        title = {
            Text("Настройка кнопок: ${summary.exercise.title}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Задай удобный шаг повторений для 3 быстрых кнопок:")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stepA,
                        onValueChange = { stepA = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Кнопка 1") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stepB,
                        onValueChange = { stepB = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Кнопка 2") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = stepC,
                        onValueChange = { stepC = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Кнопка 3") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val a = stepA.toIntOrNull() ?: 5
                    val b = stepB.toIntOrNull() ?: 10
                    val c = stepC.toIntOrNull() ?: 15
                    onConfirm(a, b, c)
                }
            ) {
                Text("СОХРАНИТЬ", color = RamsSageRecovery)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ОТМЕНА", color = RamsTextSecondary)
            }
        }
    )
}
