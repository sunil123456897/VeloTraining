package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BandExerciseType
import com.example.ui.DayActivityCell
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsAmberContainer
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsSageContainer
import com.example.ui.theme.RamsSageRecovery
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsSurfaceElevated
import com.example.ui.theme.RamsTextMuted
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary
import com.example.ui.theme.RamsWarmYellow

@Composable
fun HistoryCalibrationScreen(
    calendar28Days: List<DayActivityCell>,
    selectedEpochDay: Long,
    onSelectDay: (Long) -> Unit,
    onClearAllHistory: () -> Unit
) {
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    val selectedCell = calendar28Days.find { it.epochDay == selectedEpochDay }
        ?: calendar28Days.lastOrNull()

    val totalBikeDays = calendar28Days.count { it.hasBikeWorkout }
    val totalCaliReps4Weeks = calendar28Days.sumOf { it.totalCalisthenicsReps }
    val totalBandDays = calendar28Days.count { it.hasBandSet }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RamsBackground)
            .padding(horizontal = 18.dp, vertical = 12.dp)
            .testTag("history_calibration_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. 60-Second RP Calibration Guide (3 clear points)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RamsSurface, RoundedCornerShape(8.dp))
                    .padding(16.dp)
                    .testTag("rp_calibration_card"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "КАЛИБРОВКА «РАБОЧЕЙ ПОЛКИ» (РП) ЗА 60 СЕК",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = RamsAmberAttack
                    )
                    Text(
                        text = "МЕТКА 12:00",
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsSageRecovery
                    )
                }

                CalibrationStepRow(
                    number = "01",
                    title = "Выйди на каденс 85–90 RPM",
                    description = "Сядь в верхний хват и раскрути педали до ровных 85–90 об/мин на лёгком ходу."
                )
                CalibrationStepRow(
                    number = "02",
                    title = "Найди упор под носовое дыхание",
                    description = "Плавно закручивай барашек вправо, пока ноги не почувствуют плотный упор маховика, при котором ты всё ещё можешь спокойно дышать только носом без открывания рта."
                )
                CalibrationStepRow(
                    number = "03",
                    title = "Совмести метку на «12 часов»",
                    description = "Запомни или переклей стикер на рычажке строго вверх на 12:00. Это твоя базовая «РП» — от неё отсчитываются все углы (+45°, +90°, +180°)."
                )
            }
        }

        // 2. 4-Week Activity Calendar Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(RamsSurface, RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "КАЛЕНДАРЬ АКТИВНОСТИ • 4 НЕДЕЛИ",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = RamsSageRecovery
                        )
                        Text(
                            text = "Вело: $totalBikeDays дн.  •  Объём: $totalCaliReps4Weeks повт.  •  Резинка: $totalBandDays сессий",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RamsTextSecondary
                        )
                    }
                }

                // Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDot(color = RamsAmberAttack, text = "Велотренировка")
                    LegendDot(color = RamsSageRecovery, text = "Объём калистеники")
                    LegendDot(color = RamsWarmYellow, text = "Сессия с резинкой")
                }

                // 4 rows x 7 columns grid
                val weeks = calendar28Days.chunked(7)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    weeks.forEach { weekDays ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weekDays.forEach { cell ->
                                CalendarDayGridCell(
                                    cell = cell,
                                    isSelected = cell.epochDay == selectedCell?.epochDay,
                                    onClick = { onSelectDay(cell.epochDay) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Selected Day Detailed Breakdown
        if (selectedCell != null) {
            item {
                SelectedDayDetailCard(cell = selectedCell)
            }
        }

        // 4. Small Clear All History button at the bottom with confirmation
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                OutlinedButton(
                    onClick = { showClearConfirmDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("clear_all_history_button")
                ) {
                    Text(
                        text = "Очистить всю историю",
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsTextSecondary
                    )
                }
            }
        }
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            containerColor = RamsSurface,
            titleContentColor = RamsTextPrimary,
            textContentColor = RamsTextSecondary,
            title = {
                Text("Очистить всю историю?")
            },
            text = {
                Text("Все записи велотренировок, дневного объёма калистеники и подходов с резинкой будут удалены. Пользовательские схемы тренировок останутся.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearConfirmDialog = false
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("ОЧИСТИТЬ", color = RamsAmberAttack)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("ОТМЕНА", color = RamsTextSecondary)
                }
            }
        )
    }
}

@Composable
private fun CalibrationStepRow(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = number,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold
            ),
            color = RamsSageRecovery,
            modifier = Modifier.width(32.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = RamsTextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = RamsTextSecondary
            )
        }
    }
}

@Composable
private fun LegendDot(
    color: androidx.compose.ui.graphics.Color,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = RamsTextSecondary
        )
    }
}

@Composable
private fun CalendarDayGridCell(
    cell: DayActivityCell,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = when {
        isSelected -> RamsSurfaceElevated
        cell.hasBikeWorkout -> RamsAmberContainer.copy(alpha = 0.65f)
        cell.totalCalisthenicsReps > 0 -> RamsSageContainer.copy(alpha = 0.55f)
        else -> RamsBackground
    }

    Column(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag("calendar_cell_${cell.epochDay}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${cell.dayOfMonth} ${cell.dayOfWeekShort}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 10.sp,
                fontWeight = if (cell.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (cell.isToday) RamsAmberAttack else RamsTextSecondary,
            maxLines = 1
        )

        Text(
            text = if (cell.totalCalisthenicsReps > 0) "${cell.totalCalisthenicsReps}" else "—",
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum"
            ),
            color = if (cell.totalCalisthenicsReps > 0) RamsTextPrimary else RamsTextMuted,
            textAlign = TextAlign.Center
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cell.hasBikeWorkout) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(RamsAmberAttack)
                )
            }
            if (cell.totalCalisthenicsReps > 0) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(RamsSageRecovery)
                )
            }
            if (cell.hasBandSet) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(RamsWarmYellow)
                )
            }
            if (!cell.hasBikeWorkout && cell.totalCalisthenicsReps == 0 && !cell.hasBandSet) {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun SelectedDayDetailCard(cell: DayActivityCell) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsSurface, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "СВОДКА ДНЯ: ${cell.dayOfMonth} ${cell.monthShort.uppercase()} (${cell.dayOfWeekShort})",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (cell.isToday) RamsAmberAttack else RamsTextPrimary
            )
            if (cell.isToday) {
                Text(
                    text = "СЕГОДНЯ",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsSageRecovery
                )
            }
        }

        if (cell.bikeSessions.isEmpty() && cell.totalCalisthenicsReps == 0 && cell.bandSets.isEmpty()) {
            Text(
                text = "День отдыха — записей велотренировок и калистеники нет.",
                style = MaterialTheme.typography.bodyMedium,
                color = RamsTextSecondary
            )
        } else {
            cell.bikeSessions.forEach { bike ->
                val min = (bike.durationSec + 30) / 60
                val rpShiftText = if (bike.rpShiftDegrees > 0) "+${bike.rpShiftDegrees}°" else "0° (База)"
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Вело: ${bike.workoutTitle}",
                            style = MaterialTheme.typography.titleMedium,
                            color = RamsAmberAttack
                        )
                        Text(
                            text = "$min мин",
                            style = MaterialTheme.typography.labelLarge,
                            color = RamsTextPrimary
                        )
                    }
                    Text(
                        text = "Ноги: ${bike.legFeeling}  •  Сдвиг полки РП: $rpShiftText",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RamsTextSecondary
                    )
                }
            }

            if (cell.totalCalisthenicsReps > 0) {
                val breakdown = cell.calisthenicsByExercise.entries.joinToString(" • ") { (ex, reps) ->
                    "${ex.title}: $reps"
                }
                Text(
                    text = "Фоновый объём: ${cell.totalCalisthenicsReps} повт. ($breakdown)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RamsSageRecovery
                )
            }

            cell.bandSets.forEach { band ->
                val exTitle = runCatching {
                    BandExerciseType.valueOf(band.exerciseType).title
                }.getOrDefault(band.exerciseType)
                Text(
                    text = "Резинка: $exTitle — ${band.reps} повт. (${band.bandDescription})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RamsWarmYellow
                )
            }
        }
    }
}
