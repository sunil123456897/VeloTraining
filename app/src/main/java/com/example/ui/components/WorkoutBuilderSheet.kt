package com.example.ui.components

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import com.example.data.model.DialPosition
import com.example.data.model.HandPosition
import com.example.data.model.PhaseType
import com.example.data.model.WorkoutBlock
import com.example.data.model.WorkoutProgram
import com.example.data.model.WorkoutStepTemplate
import com.example.ui.theme.RamsAmberAttack
import com.example.ui.theme.RamsBackground
import com.example.ui.theme.RamsOutline
import com.example.ui.theme.RamsSageRecovery
import com.example.ui.theme.RamsSurface
import com.example.ui.theme.RamsSurfaceElevated
import com.example.ui.theme.RamsTextPrimary
import com.example.ui.theme.RamsTextSecondary

/**
 * Block-based custom workout builder allowing repeatable series (e.g., 8x [20s Attack + 40s Recovery]).
 */
@Composable
fun WorkoutBuilderScreen(
    initialProgram: WorkoutProgram,
    onSave: (WorkoutProgram) -> Unit,
    onDelete: (String) -> Unit,
    onCancel: () -> Unit
) {
    BackHandler { onCancel() }

    var title by remember(initialProgram.id) { mutableStateOf(initialProgram.title) }
    var subtitle by remember(initialProgram.id) { mutableStateOf(initialProgram.subtitle) }
    var blocks by remember(initialProgram.id) { mutableStateOf(initialProgram.blocks) }

    val draftProgram = remember(title, subtitle, blocks) {
        initialProgram.copy(
            title = title.ifBlank { "Моя схема" },
            subtitle = subtitle.ifBlank { "Интервальная тренировка без закисления" },
            blocks = blocks
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("workout_builder_screen"),
        color = RamsBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Закрыть конструктор",
                            tint = RamsTextPrimary
                        )
                    }
                    Column {
                        Text(
                            text = "КОНСТРУКТОР ПРОГРАММЫ",
                            style = MaterialTheme.typography.labelLarge,
                            color = RamsAmberAttack
                        )
                        Text(
                            text = "Итого: ${draftProgram.totalMinutesRounded} мин (${draftProgram.flattenSteps().size} интервалов)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RamsTextSecondary
                        )
                    }
                }

                Button(
                    onClick = { onSave(draftProgram) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RamsSageRecovery,
                        contentColor = RamsBackground
                    ),
                    modifier = Modifier.testTag("save_custom_workout_button")
                ) {
                    Text(
                        text = "СОХРАНИТЬ",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RamsSurface, RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Название велотренировки") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RamsAmberAttack,
                                unfocusedBorderColor = RamsOutline,
                                focusedTextColor = RamsTextPrimary,
                                unfocusedTextColor = RamsTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = subtitle,
                            onValueChange = { subtitle = it },
                            label = { Text("Краткое описание цели") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = RamsSageRecovery,
                                unfocusedBorderColor = RamsOutline,
                                focusedTextColor = RamsTextPrimary,
                                unfocusedTextColor = RamsTextPrimary
                            )
                        )
                    }
                }

                itemsIndexed(blocks) { blockIndex, block ->
                    BlockEditorCard(
                        blockIndex = blockIndex,
                        block = block,
                        canDelete = blocks.size > 1,
                        onUpdateBlock = { updated ->
                            blocks = blocks.toMutableList().also { it[blockIndex] = updated }
                        },
                        onDeleteBlock = {
                            if (blocks.size > 1) {
                                blocks = blocks.toMutableList().also { it.removeAt(blockIndex) }
                            }
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val newBlock = WorkoutBlock(
                                    blockName = "Серия ${blocks.size + 1}",
                                    repeatCount = 6,
                                    steps = listOf(
                                        WorkoutStepTemplate(
                                            title = "Атака",
                                            durationSec = 20,
                                            phaseType = PhaseType.ATTACK,
                                            dialPosition = DialPosition.RP_PLUS_90,
                                            minRpm = 90,
                                            maxRpm = 95,
                                            handPosition = HandPosition.SITTING_AERO
                                        ),
                                        WorkoutStepTemplate(
                                            title = "Перезарядка",
                                            durationSec = 40,
                                            phaseType = PhaseType.RECOVERY,
                                            dialPosition = DialPosition.RP_BASE,
                                            minRpm = 75,
                                            maxRpm = 80,
                                            handPosition = HandPosition.SITTING_TOP
                                        )
                                    )
                                )
                                blocks = blocks + newBlock
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = RamsAmberAttack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ДОБАВИТЬ СЕРИЮ (ЦИКЛ)", color = RamsTextPrimary)
                        }
                    }
                }

                if (initialProgram.isCustom) {
                    item {
                        TextButton(
                            onClick = { onDelete(initialProgram.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = RamsAmberAttack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Удалить эту пользовательскую схему", color = RamsAmberAttack)
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
private fun BlockEditorCard(
    blockIndex: Int,
    block: WorkoutBlock,
    canDelete: Boolean,
    onUpdateBlock: (WorkoutBlock) -> Unit,
    onDeleteBlock: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsSurface, RoundedCornerShape(8.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = block.blockName,
                onValueChange = { onUpdateBlock(block.copy(blockName = it)) },
                label = { Text("Блок #${blockIndex + 1}") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RamsAmberAttack,
                    unfocusedBorderColor = RamsOutline,
                    focusedTextColor = RamsTextPrimary,
                    unfocusedTextColor = RamsTextPrimary
                )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Repeat counter for the series
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RamsSurfaceElevated)
                        .clickable {
                            val next = (block.repeatCount - 1).coerceAtLeast(1)
                            onUpdateBlock(block.copy(repeatCount = next))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("-", style = MaterialTheme.typography.titleLarge, color = RamsTextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${block.repeatCount}×",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = RamsAmberAttack
                    )
                    Text(
                        text = "кругов",
                        style = MaterialTheme.typography.labelMedium,
                        color = RamsTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(RamsSurfaceElevated)
                        .clickable {
                            val next = (block.repeatCount + 1).coerceAtMost(30)
                            onUpdateBlock(block.copy(repeatCount = next))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text("+", style = MaterialTheme.typography.titleLarge, color = RamsTextPrimary)
                }

                if (canDelete) {
                    IconButton(onClick = onDeleteBlock) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Удалить блок",
                            tint = RamsTextSecondary
                        )
                    }
                }
            }
        }

        // Steps inside this block
        block.steps.forEachIndexed { stepIdx, step ->
            StepEditorRow(
                step = step,
                canRemove = block.steps.size > 1,
                onUpdateStep = { updatedStep ->
                    val newSteps = block.steps.toMutableList().also { it[stepIdx] = updatedStep }
                    onUpdateBlock(block.copy(steps = newSteps))
                },
                onRemoveStep = {
                    if (block.steps.size > 1) {
                        val newSteps = block.steps.toMutableList().also { it.removeAt(stepIdx) }
                        onUpdateBlock(block.copy(steps = newSteps))
                    }
                }
            )
        }

        TextButton(
            onClick = {
                val added = WorkoutStepTemplate(
                    title = "Фаза ${block.steps.size + 1}",
                    durationSec = 30,
                    phaseType = PhaseType.RECOVERY,
                    dialPosition = DialPosition.RP_BASE,
                    minRpm = 75,
                    maxRpm = 80,
                    handPosition = HandPosition.SITTING_TOP
                )
                onUpdateBlock(block.copy(steps = block.steps + added))
            }
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = RamsSageRecovery)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Добавить фазу внутрь блока", color = RamsSageRecovery)
        }
    }
}

@Composable
private fun StepEditorRow(
    step: WorkoutStepTemplate,
    canRemove: Boolean,
    onUpdateStep: (WorkoutStepTemplate) -> Unit,
    onRemoveStep: () -> Unit
) {
    val isAttack = step.phaseType.isHighIntensity
    val accent = if (isAttack) RamsAmberAttack else RamsSageRecovery

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(RamsBackground, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Phase Type Toggle (Attack vs Recovery)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RamsSurfaceElevated)
                    .clickable {
                        val nextPhase = when (step.phaseType) {
                            PhaseType.WARMUP -> PhaseType.ATTACK
                            PhaseType.ATTACK -> PhaseType.RECOVERY
                            PhaseType.RECOVERY -> PhaseType.COOLDOWN
                            PhaseType.COOLDOWN -> PhaseType.WARMUP
                        }
                        val nextTitle = when (nextPhase) {
                            PhaseType.WARMUP -> "Разминка"
                            PhaseType.ATTACK -> "Атака"
                            PhaseType.RECOVERY -> "Перезарядка"
                            PhaseType.COOLDOWN -> "Заминка"
                        }
                        onUpdateStep(step.copy(phaseType = nextPhase, title = nextTitle))
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.phaseType.displayTitle.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent
                )
            }

            // Duration controls (-10s / +10s / +60s)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SmallStepButton("-10с") {
                    onUpdateStep(step.copy(durationSec = (step.durationSec - 10).coerceAtLeast(10)))
                }
                Text(
                    text = formatDurationLabel(step.durationSec),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = RamsTextPrimary
                )
                SmallStepButton("+10с") {
                    onUpdateStep(step.copy(durationSec = (step.durationSec + 10).coerceAtMost(1800)))
                }
                SmallStepButton("+1м") {
                    onUpdateStep(step.copy(durationSec = (step.durationSec + 60).coerceAtMost(1800)))
                }
                if (canRemove) {
                    IconButton(onClick = onRemoveStep, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Удалить шаг",
                            tint = RamsTextSecondary
                        )
                    }
                }
            }
        }

        // Dial Position selector strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            DialPosition.entries.forEach { dial ->
                val selected = step.dialPosition == dial
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selected) accent else RamsSurfaceElevated)
                        .clickable { onUpdateStep(step.copy(dialPosition = dial)) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = dial.shortLabel,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (selected) RamsBackground else RamsTextPrimary
                    )
                }
            }
        }

        // RPM & Hand position selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // RPM cycle chip
            val rpmPresets = listOf(75 to 75, 75 to 80, 85 to 88, 90 to 95, 95 to 100, 100 to 105)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RamsSurfaceElevated)
                    .clickable {
                        val idx = rpmPresets.indexOfFirst { it.first == step.minRpm }
                        val next = rpmPresets[(idx + 1) % rpmPresets.size]
                        onUpdateStep(step.copy(minRpm = next.first, maxRpm = next.second))
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Каденс: ${step.minRpm}–${step.maxRpm} RPM",
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsTextPrimary
                )
            }

            // Hand position cycle chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(RamsSurfaceElevated)
                    .clickable {
                        val allHands = HandPosition.entries
                        val nextHand = allHands[(step.handPosition.ordinal + 1) % allHands.size]
                        onUpdateStep(step.copy(handPosition = nextHand))
                    }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = step.handPosition.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = RamsTextSecondary
                )
            }
        }
    }
}

@Composable
private fun SmallStepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(RamsSurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = RamsTextPrimary
        )
    }
}

private fun formatDurationLabel(sec: Int): String {
    val m = sec / 60
    val s = sec % 60
    return if (m > 0 && s > 0) "${m}м ${s}с" else if (m > 0) "${m} мин" else "${s} сек"
}
