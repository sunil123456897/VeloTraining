package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.CalisthenicsExercise
import com.example.ui.TicketSessionTally
import com.example.ui.TicketSheetMode
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsOutline
import com.example.ui.theme.RamsSageContainer
import com.example.ui.theme.RamsSageRecovery
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsSurfaceElevated
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary
import androidx.compose.material3.MaterialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketBottomSheet(
    ticketState: TicketSessionTally,
    onAddReps: (CalisthenicsExercise, Int) -> Unit,
    onDismissAndProceed: () -> Unit
) {
    if (ticketState.mode == TicketSheetMode.HIDDEN) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPreWorkout = ticketState.mode == TicketSheetMode.PRE_WORKOUT

    ModalBottomSheet(
        onDismissRequest = onDismissAndProceed,
        sheetState = sheetState,
        containerColor = RamsSurface,
        contentColor = RamsTextPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("ticket_bottom_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isPreWorkout) "ВХОДНОЙ БИЛЕТ ДО СЕДЛА" else "ВЫХОДНОЙ БИЛЕТ ПОСЛЕ СЕДЛА",
                    style = MaterialTheme.typography.labelLarge,
                    color = RamsSageRecovery
                )
                if (ticketState.totalAddedReps > 0) {
                    Text(
                        text = "Записано: +${ticketState.totalAddedReps} повт.",
                        style = MaterialTheme.typography.labelLarge,
                        color = RamsAmberAttack
                    )
                }
            }

            Text(
                text = if (isPreWorkout) {
                    stringResource(R.string.ticket_prompt)
                } else {
                    stringResource(R.string.ticket_post_prompt)
                },
                style = MaterialTheme.typography.titleLarge,
                color = RamsTextPrimary
            )

            // All 4 exercises with +5 / +10 / +15 buttons right in the sheet
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CalisthenicsExercise.entries.forEach { exercise ->
                    val addedNow = ticketState.addedByExercise[exercise] ?: 0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RamsBackground, RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exercise.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = RamsTextPrimary
                            )
                            Text(
                                text = if (addedNow > 0) "Добавлено сейчас: +$addedNow" else "Сухой подход без отказа",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (addedNow > 0) RamsSageRecovery else RamsTextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(5, 10, 15).forEach { step ->
                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(RamsSurfaceElevated)
                                        .clickable { onAddReps(exercise, step) }
                                        .padding(horizontal = 12.dp)
                                        .testTag("ticket_add_${exercise.name}_$step"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+$step",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        ),
                                        color = RamsTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Primary action or 1-tap skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (ticketState.totalAddedReps == 0) {
                    OutlinedButton(
                        onClick = onDismissAndProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("ticket_skip_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = RamsTextPrimary
                        )
                    ) {
                        Text(
                            text = if (isPreWorkout) {
                                "ПРОПУСТИТЬ В 1 КАСАНИЕ • СТАРТ"
                            } else {
                                "ПРОПУСТИТЬ В 1 КАСАНИЕ"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                } else {
                    Button(
                        onClick = onDismissAndProceed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("ticket_confirm_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPreWorkout) RamsAmberAttack else RamsSageRecovery,
                            contentColor = RamsBackground
                        )
                    ) {
                        Text(
                            text = if (isPreWorkout) {
                                "ЗАПУСТИТЬ КОКПИТ (+${ticketState.totalAddedReps} В ОБЪЁМ)"
                            } else {
                                "ГОТОВО (+${ticketState.totalAddedReps} В ДНЕВНОЙ ОБЪЁМ)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
