package com.example.data.model

import kotlin.math.abs

enum class DialPosition(
    val angleDegrees: Float,
    val shortLabel: String,
    val clockLabel: String,
    val description: String
) {
    RP_MINUS_45(
        angleDegrees = -45f,
        shortLabel = "РП - 45°",
        clockLabel = "10:30",
        description = "Совсем легко • разгрузка"
    ),
    RP_BASE(
        angleDegrees = 0f,
        shortLabel = "РП",
        clockLabel = "12:00",
        description = "Базовая полка • дыхание носом"
    ),
    RP_PLUS_45(
        angleDegrees = 45f,
        shortLabel = "РП + 45°",
        clockLabel = "1:30",
        description = "Плотный накат"
    ),
    RP_PLUS_90(
        angleDegrees = 90f,
        shortLabel = "РП + 90°",
        clockLabel = "3:00",
        description = "Четверть оборота • порог"
    ),
    RP_PLUS_135(
        angleDegrees = 135f,
        shortLabel = "РП + 135°",
        clockLabel = "4:30",
        description = "Тяжёлый силовой накат"
    ),
    RP_PLUS_180(
        angleDegrees = 180f,
        shortLabel = "РП + 180°",
        clockLabel = "6:00",
        description = "Пол-оборота • силовой штурм"
    );

    companion object {
        fun computeDeltaInstruction(previous: DialPosition?, current: DialPosition): String {
            if (previous == null) {
                return when (current) {
                    RP_BASE -> "ВЫСТАВИ В БАЗУ РП (12:00)"
                    RP_MINUS_45 -> "УБАВЬ ВЛЕВО ДО РП - 45° (10:30)"
                    else -> "ВЫСТАВИ НА ${current.shortLabel} (${current.clockLabel})"
                }
            }
            val diff = (current.angleDegrees - previous.angleDegrees).toInt()
            if (diff == 0) {
                return "НЕ ТРОГАЙ РЫЧАЖОК (${current.shortLabel})"
            }
            if (current == RP_BASE) {
                return if (diff < 0) {
                    "ВЕРНИ В БАЗУ РП (${diff}°)"
                } else {
                    "ВЕРНИ В БАЗУ РП (+${diff}°)"
                }
            }
            return if (diff > 0) {
                "ПОВЕРНИ ВПРАВО +${diff}° → ${current.shortLabel}"
            } else {
                "УБАВЬ ВЛЕВО ${diff}° → ${current.shortLabel}"
            }
        }
    }
}

enum class HandPosition(
    val label: String,
    val shortHint: String
) {
    SITTING_TOP(
        label = "Сидя • Верхний хват",
        shortHint = "Расслабь плечи, ровное дыхание"
    ),
    SITTING_AERO(
        label = "Сидя • Нижний хват Aero",
        shortHint = "Плотная посадка, локти согнуты"
    ),
    STANDING_BULLHORN(
        label = "Стоя • Рога руля Bullhorn",
        shortHint = "Таз над кареткой, вес в педали"
    ),
    PULL_PEDAL_STRAP(
        label = "Тяни педаль вверх ремешком",
        shortHint = "Верхний хват • круговое педалирование"
    ),
    HEEL_DOWN_STRETCH(
        label = "Опусти пятку вниз в нижней точке",
        shortHint = "Плавная прокачка и растяжка икр"
    )
}

enum class PhaseType(
    val displayTitle: String,
    val isHighIntensity: Boolean
) {
    WARMUP("Разминка", false),
    ATTACK("Атака / Нагрузка", true),
    RECOVERY("Перезарядка / Сброс", false),
    COOLDOWN("Заминка", false)
}

data class WorkoutStepTemplate(
    val title: String,
    val durationSec: Int,
    val phaseType: PhaseType,
    val dialPosition: DialPosition,
    val minRpm: Int,
    val maxRpm: Int,
    val handPosition: HandPosition,
    val evenRoundHandPosition: HandPosition? = null,
    val coachNote: String = ""
)

data class WorkoutBlock(
    val blockName: String,
    val repeatCount: Int = 1,
    val steps: List<WorkoutStepTemplate>
)

data class WorkoutProgram(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val isCustom: Boolean = false,
    val blocks: List<WorkoutBlock>
) {
    val totalDurationSec: Int
        get() = blocks.sumOf { block ->
            block.repeatCount * block.steps.sumOf { it.durationSec }
        }

    val totalMinutesRounded: Int
        get() = (totalDurationSec + 30) / 60

    fun flattenSteps(): List<FlattenedWorkoutStep> {
        val rawList = mutableListOf<FlattenedWorkoutStep>()
        var previousDial: DialPosition? = null

        blocks.forEach { block ->
            val repeats = block.repeatCount.coerceAtLeast(1)
            for (round in 1..repeats) {
                block.steps.forEach { step ->
                    val effectiveHand = if (round % 2 == 0 && step.evenRoundHandPosition != null) {
                        step.evenRoundHandPosition
                    } else {
                        step.handPosition
                    }
                    val deltaText = DialPosition.computeDeltaInstruction(previousDial, step.dialPosition)
                    rawList.add(
                        FlattenedWorkoutStep(
                            stepIndex = rawList.size,
                            totalSteps = 0, // updated below
                            blockName = block.blockName,
                            roundIndexInBlock = round,
                            totalRoundsInBlock = repeats,
                            title = step.title,
                            durationSec = step.durationSec.coerceAtLeast(5),
                            phaseType = step.phaseType,
                            dialPosition = step.dialPosition,
                            previousDialPosition = previousDial,
                            deltaInstruction = deltaText,
                            minRpm = step.minRpm,
                            maxRpm = step.maxRpm,
                            targetRpmMid = (step.minRpm + step.maxRpm) / 2,
                            handPosition = effectiveHand,
                            coachNote = step.coachNote
                        )
                    )
                    previousDial = step.dialPosition
                }
            }
        }
        val total = rawList.size
        return rawList.map { it.copy(totalSteps = total) }
    }
}

data class FlattenedWorkoutStep(
    val stepIndex: Int,
    val totalSteps: Int,
    val blockName: String,
    val roundIndexInBlock: Int,
    val totalRoundsInBlock: Int,
    val title: String,
    val durationSec: Int,
    val phaseType: PhaseType,
    val dialPosition: DialPosition,
    val previousDialPosition: DialPosition?,
    val deltaInstruction: String,
    val minRpm: Int,
    val maxRpm: Int,
    val targetRpmMid: Int,
    val handPosition: HandPosition,
    val coachNote: String
) {
    val rpmRangeText: String
        get() = if (abs(maxRpm - minRpm) <= 1) "$minRpm RPM" else "$minRpm–$maxRpm RPM"

    val roundBadgeText: String
        get() = if (totalRoundsInBlock > 1) {
            "${blockName} • Раунд $roundIndexInBlock / $totalRoundsInBlock"
        } else {
            blockName
        }
}

enum class CalisthenicsExercise(
    val title: String,
    val muscleHint: String,
    val unitLabel: String = "повт."
) {
    PULL_UPS("Подтягивания", "Сухой подход без отказа • спина и хват"),
    PUSH_UPS("Отжимания", "Чистая техника • грудь и трицепс"),
    SQUATS("Приседания", "Полная амплитуда • ноги без закисления"),
    CALF_RAISES("Подъёмы на икры", "Пауза вверху и растяжка пяткой вниз")
}

enum class BandExerciseType(
    val title: String,
    val defaultBandHint: String
) {
    BAND_PULL_UPS("Подтягивания с резинкой", "Фиолетовая 15–30 кг"),
    BAND_PUSH_UPS("Отжимания с резинкой", "Красная 10–20 кг")
}
