package com.example.data.model

import org.json.JSONArray
import org.json.JSONObject

object PresetWorkouts {

    val allPresets: List<WorkoutProgram> = listOf(
        // 1) «Пулемёт 20 / 40» (28 мин, взрывная мощь без кислоты)
        WorkoutProgram(
            id = "preset_machinegun_20_40",
            title = "Пулемёт 20 / 40",
            subtitle = "Взрывная мощь без закисления • чередование Стоя и Aero",
            tag = "28 МИН • АЛАКТАТ",
            isCustom = false,
            blocks = listOf(
                WorkoutBlock(
                    blockName = "Разминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Плавный разогрев",
                            durationSec = 240,
                            phaseType = PhaseType.WARMUP,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 85,
                            maxRpm = 85,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Дыхание строго носом, найди базовую полку РП"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Серия 1",
                    repeatCount = 8,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Атака 20 сек",
                            durationSec = 20,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_180,
                            minRpm = 90,
                            maxRpm = 95,
                            handPosition = HandPosition.STANDING_BULLHORN,
                            evenRoundHandPosition = HandPosition.SITTING_AERO,
                            coachNote = "Нечётные круги — Стоя Bullhorn, чётные — Сидя Aero"
                        ),
                        WorkoutStepTemplate(
                            title = "Перезарядка 40 сек",
                            durationSec = 40,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 75,
                            maxRpm = 80,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Сбрось напряжение ног, восстанови дыхание"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Передышка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Передышка между сериями",
                            durationSec = 180,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Лёгкое вращение без давления на педаль"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Серия 2",
                    repeatCount = 8,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Атака 20 сек",
                            durationSec = 20,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_180,
                            minRpm = 90,
                            maxRpm = 95,
                            handPosition = HandPosition.STANDING_BULLHORN,
                            evenRoundHandPosition = HandPosition.SITTING_AERO,
                            coachNote = "Короткий мощный импульс без закисления"
                        ),
                        WorkoutStepTemplate(
                            title = "Перезарядка 40 сек",
                            durationSec = 40,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 75,
                            maxRpm = 80,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Верни рычажок точно в РП (12:00)"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Заминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Заминка и охлаждение",
                            durationSec = 300,
                            phaseType = PhaseType.COOLDOWN,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Спокойный выдох, плавное снижение пульса"
                        )
                    )
                )
            )
        ),

        // 2) «Реактор 30 / 30 на одном винте» (26 мин, скоростная выносливость)
        WorkoutProgram(
            id = "preset_reactor_30_30",
            title = "Реактор 30 / 30 на одном винте",
            subtitle = "Скоростная выносливость на РП + 45° без кручения барашка в серии",
            tag = "26 МИН • ОДИН ВИНТ",
            isCustom = false,
            blocks = listOf(
                WorkoutBlock(
                    blockName = "Разминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Выход на рабочую полку +45°",
                            durationSec = 240,
                            phaseType = PhaseType.WARMUP,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 85,
                            maxRpm = 90,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Выстави РП + 45° (1:30) — дальше в серии рычажок не трогаем"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Серия 1",
                    repeatCount = 8,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Ускорение каденса",
                            durationSec = 30,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 100,
                            maxRpm = 105,
                            handPosition = HandPosition.SITTING_AERO,
                            coachNote = "Рычажок на месте! Разгоняй только частоту вращения"
                        ),
                        WorkoutStepTemplate(
                            title = "Сброс темпа",
                            durationSec = 30,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 70,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Рычажок не трогаем, плавно сбавь обороты"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Передышка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Активный отдых",
                            durationSec = 180,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Сбрось винт в РП - 45°, дай ногам продышаться"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Серия 2",
                    repeatCount = 8,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Ускорение каденса",
                            durationSec = 30,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 100,
                            maxRpm = 105,
                            handPosition = HandPosition.SITTING_AERO,
                            coachNote = "Плотная посадка в Aero, таз не прыгает в седле"
                        ),
                        WorkoutStepTemplate(
                            title = "Сброс темпа",
                            durationSec = 30,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 70,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Спокойный вдох через нос, длинный выдох"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Заминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Заминка",
                            durationSec = 180,
                            phaseType = PhaseType.COOLDOWN,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Свободный накат до полного восстановления дыхания"
                        )
                    )
                )
            )
        ),

        // 3) «Рваная лесенка 60 – 40 – 20» (26 мин, драйвовый фартлек)
        WorkoutProgram(
            id = "preset_ladder_60_40_20",
            title = "Рваная лесенка 60 – 40 – 20",
            subtitle = "Драйвовый фартлек: Крейсер → Турбина с подтягиванием педали → Штурм стоя",
            tag = "26 МИН • ФАРТЛЕК",
            isCustom = false,
            blocks = listOf(
                WorkoutBlock(
                    blockName = "Разминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Разминка в базе",
                            durationSec = 240,
                            phaseType = PhaseType.WARMUP,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 85,
                            maxRpm = 85,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Подготовь голеностоп и коленные связки"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Лесенка 60-40-20",
                    repeatCount = 6,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Крейсер 60 сек",
                            durationSec = 60,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 85,
                            maxRpm = 85,
                            handPosition = HandPosition.SITTING_AERO,
                            coachNote = "Плотный ровный накат в нижнем хвате Aero"
                        ),
                        WorkoutStepTemplate(
                            title = "Турбина 40 сек",
                            durationSec = 40,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_45,
                            minRpm = 95,
                            maxRpm = 100,
                            handPosition = HandPosition.PULL_PEDAL_STRAP,
                            coachNote = "Верхний хват + активно тяни педаль вверх ремешком"
                        ),
                        WorkoutStepTemplate(
                            title = "Штурм 20 сек",
                            durationSec = 20,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_135,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.STANDING_BULLHORN,
                            coachNote = "Встань на рога руля Bullhorn, продави весом тела"
                        ),
                        WorkoutStepTemplate(
                            title = "Сброс 60 сек",
                            durationSec = 60,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Сидя спокойно, верни рычажок в РП"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Заминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Заминка",
                            durationSec = 240,
                            phaseType = PhaseType.COOLDOWN,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Мягкое вращение, расслабь кисти и трапеции"
                        )
                    )
                )
            )
        ),

        // 4) «Суб-ПАНО Крейсер 4 × 6» (42 мин, сдвиг порога без жжения)
        WorkoutProgram(
            id = "preset_sub_pano_4x6",
            title = "Суб-ПАНО Крейсер 4 × 6",
            subtitle = "Аэробная мощность и сдвиг порога без накопления жжения",
            tag = "42 МИН • СУБ-ПАНО",
            isCustom = false,
            blocks = listOf(
                WorkoutBlock(
                    blockName = "Разминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Плавный вход в темп",
                            durationSec = 360,
                            phaseType = PhaseType.WARMUP,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 85,
                            maxRpm = 85,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "6 минут прогрева до открытия рабочего потоотделения"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Крейсерские блоки 4×6",
                    repeatCount = 4,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Порог 6 мин",
                            durationSec = 360,
                            phaseType = PhaseType.ATTACK,
                            dialPosition = DialPosition.RP_PLUS_90,
                            minRpm = 85,
                            maxRpm = 88,
                            handPosition = HandPosition.SITTING_AERO,
                            coachNote = "Держи ровное дыхание без нарастания жжения"
                        ),
                        WorkoutStepTemplate(
                            title = "Отдых 2 мин",
                            durationSec = 120,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_BASE,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Верни рычажок в РП, сделай глоток воды"
                        )
                    )
                ),
                WorkoutBlock(
                    blockName = "Заминка",
                    repeatCount = 1,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Заминка",
                            durationSec = 240,
                            phaseType = PhaseType.COOLDOWN,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 75,
                            maxRpm = 75,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Плавное завершение работы"
                        )
                    )
                )
            )
        ),

        // 5) «Капиллярная промывка после ОФП» (25 мин, восстановление ног и икр)
        WorkoutProgram(
            id = "preset_capillary_flush",
            title = "Капиллярная промывка после ОФП",
            subtitle = "Ускорение кровотока, дренаж и мягкая растяжка икр без нагрузки на ЦНС",
            tag = "25 МИН • РЕГЕНЕРАЦИЯ",
            isCustom = false,
            blocks = listOf(
                WorkoutBlock(
                    blockName = "Циклы промывки",
                    repeatCount = 5,
                    steps = listOf(
                        WorkoutStepTemplate(
                            title = "Лёгкий накат",
                            durationSec = 260,
                            phaseType = PhaseType.RECOVERY,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 90,
                            maxRpm = 95,
                            handPosition = HandPosition.SITTING_TOP,
                            coachNote = "Высокая частота без сопротивления для капиллярного насоса"
                        ),
                        WorkoutStepTemplate(
                            title = "Растяжка икр в ходу",
                            durationSec = 40,
                            phaseType = PhaseType.WARMUP,
                            dialPosition = DialPosition.RP_MINUS_45,
                            minRpm = 70,
                            maxRpm = 75,
                            handPosition = HandPosition.HEEL_DOWN_STRETCH,
                            coachNote = "Опусти пятку вниз в нижней точке для растяжки икр"
                        )
                    )
                )
            )
        )
    )

    fun blocksToJson(blocks: List<WorkoutBlock>): String {
        val rootArray = JSONArray()
        blocks.forEach { block ->
            val blockObj = JSONObject()
            blockObj.put("blockName", block.blockName)
            blockObj.put("repeatCount", block.repeatCount)
            val stepsArray = JSONArray()
            block.steps.forEach { step ->
                val stepObj = JSONObject()
                stepObj.put("title", step.title)
                stepObj.put("durationSec", step.durationSec)
                stepObj.put("phaseType", step.phaseType.name)
                stepObj.put("dialPosition", step.dialPosition.name)
                stepObj.put("minRpm", step.minRpm)
                stepObj.put("maxRpm", step.maxRpm)
                stepObj.put("handPosition", step.handPosition.name)
                stepObj.put("evenRoundHandPosition", step.evenRoundHandPosition?.name ?: "")
                stepObj.put("coachNote", step.coachNote)
                stepsArray.put(stepObj)
            }
            blockObj.put("steps", stepsArray)
            rootArray.put(blockObj)
        }
        return rootArray.toString()
    }

    fun jsonToBlocks(json: String): List<WorkoutBlock> {
        return try {
            val rootArray = JSONArray(json)
            val result = mutableListOf<WorkoutBlock>()
            for (i in 0 until rootArray.length()) {
                val blockObj = rootArray.getJSONObject(i)
                val blockName = blockObj.optString("blockName", "Серия ${i + 1}")
                val repeatCount = blockObj.optInt("repeatCount", 1).coerceAtLeast(1)
                val stepsArray = blockObj.getJSONArray("steps")
                val steps = mutableListOf<WorkoutStepTemplate>()
                for (j in 0 until stepsArray.length()) {
                    val s = stepsArray.getJSONObject(j)
                    val evenHandName = s.optString("evenRoundHandPosition", "")
                    steps.add(
                        WorkoutStepTemplate(
                            title = s.optString("title", "Интервал"),
                            durationSec = s.optInt("durationSec", 60).coerceAtLeast(5),
                            phaseType = runCatching { PhaseType.valueOf(s.getString("phaseType")) }
                                .getOrDefault(PhaseType.ATTACK),
                            dialPosition = runCatching { DialPosition.valueOf(s.getString("dialPosition")) }
                                .getOrDefault(DialPosition.RP_BASE),
                            minRpm = s.optInt("minRpm", 85),
                            maxRpm = s.optInt("maxRpm", 90),
                            handPosition = runCatching { HandPosition.valueOf(s.getString("handPosition")) }
                                .getOrDefault(HandPosition.SITTING_TOP),
                            evenRoundHandPosition = if (evenHandName.isNotBlank()) {
                                runCatching { HandPosition.valueOf(evenHandName) }.getOrNull()
                            } else null,
                            coachNote = s.optString("coachNote", "")
                        )
                    )
                }
                if (steps.isNotEmpty()) {
                    result.add(WorkoutBlock(blockName, repeatCount, steps))
                }
            }
            result
        } catch (e: Exception) {
            emptyList()
        }
    }
}
