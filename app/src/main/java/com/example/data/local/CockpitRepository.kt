package com.example.data.local

import com.example.data.model.BandExerciseType
import com.example.data.model.CalisthenicsExercise
import com.example.data.model.PresetWorkouts
import com.example.data.model.WorkoutProgram
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CockpitRepository(private val dao: CockpitDao) {

    val allProgramsFlow: Flow<List<WorkoutProgram>> = dao.observeCustomWorkouts().map { customEntities ->
        val customPrograms = customEntities.mapNotNull { entity ->
            if (entity.blocks.isEmpty()) null else WorkoutProgram(
                id = entity.id,
                title = entity.title,
                subtitle = entity.subtitle,
                tag = entity.tag,
                isCustom = !entity.isPreset,
                blocks = entity.blocks
            )
        }
        PresetWorkouts.allPresets + customPrograms
    }

    val bikeSessionsFlow: Flow<List<BikeSessionLogEntity>> = dao.observeBikeSessions()
    val calisthenicsLogsFlow: Flow<List<CalisthenicsLogEntity>> = dao.observeCalisthenicsLogs()
    val bandMaxSetsFlow: Flow<List<BandMaxSetEntity>> = dao.observeBandMaxSets()

    suspend fun saveCustomWorkout(program: WorkoutProgram) {
        val entity = CustomWorkoutEntity(
            id = program.id,
            title = program.title,
            subtitle = program.subtitle,
            tag = program.tag,
            isPreset = false,
            blocks = program.blocks,
            updatedAt = System.currentTimeMillis()
        )
        dao.saveWorkoutWithSeriesBlocks(entity)
    }

    suspend fun deleteCustomWorkout(id: String) {
        dao.deleteCustomWorkout(id)
    }

    suspend fun logBikeSession(workoutId: String, workoutTitle: String, durationSec: Int, epochDay: Long) {
        dao.insertBikeSession(
            BikeSessionLogEntity(
                workoutId = workoutId,
                workoutTitle = workoutTitle,
                durationSec = durationSec,
                epochDay = epochDay
            )
        )
    }

    suspend fun logCalisthenics(
        exercise: CalisthenicsExercise,
        reps: Int,
        sourceTag: String,
        epochDay: Long
    ) {
        dao.insertCalisthenicsLog(
            CalisthenicsLogEntity(
                exerciseName = exercise.name,
                reps = reps,
                sourceTag = sourceTag,
                epochDay = epochDay
            )
        )
    }

    suspend fun deleteCalisthenicsLog(id: Long) {
        dao.deleteCalisthenicsLog(id)
    }

    suspend fun logBandMaxSet(
        exerciseType: BandExerciseType,
        bandDescription: String,
        reps: Int,
        epochDay: Long
    ) {
        dao.insertBandMaxSet(
            BandMaxSetEntity(
                exerciseType = exerciseType.name,
                bandDescription = bandDescription.trim().ifEmpty { exerciseType.defaultBandHint },
                reps = reps,
                epochDay = epochDay
            )
        )
    }

    suspend fun deleteBandMaxSet(id: Long) {
        dao.deleteBandMaxSet(id)
    }

    suspend fun seedInitialHistoryIfEmpty(todayEpochDay: Long) {
        if (dao.countBikeSessions() == 0 && dao.countCalisthenicsLogs() == 0) {
            val dayMs = 86_400_000L
            val now = System.currentTimeMillis()

            val sampleBikeDays = listOf(
                Triple(todayEpochDay - 11, "preset_machinegun_20_40", "Пулемёт 20 / 40"),
                Triple(todayEpochDay - 9, "preset_capillary_flush", "Капиллярная промывка после ОФП"),
                Triple(todayEpochDay - 6, "preset_reactor_30_30", "Реактор 30 / 30 на одном винте"),
                Triple(todayEpochDay - 4, "preset_sub_pano_4x6", "Суб-ПАНО Крейсер 4 × 6"),
                Triple(todayEpochDay - 2, "preset_ladder_60_40_20", "Рваная лесенка 60 – 40 – 20")
            )
            sampleBikeDays.forEach { (day, id, title) ->
                val diffDays = (todayEpochDay - day).coerceAtLeast(1)
                dao.insertBikeSession(
                    BikeSessionLogEntity(
                        workoutId = id,
                        workoutTitle = title,
                        durationSec = 26 * 60,
                        epochDay = day,
                        completedAt = now - diffDays * dayMs
                    )
                )
            }

            val calisthenicsSeed = listOf(
                Triple(todayEpochDay - 6, CalisthenicsExercise.PULL_UPS, 25),
                Triple(todayEpochDay - 6, CalisthenicsExercise.PUSH_UPS, 50),
                Triple(todayEpochDay - 5, CalisthenicsExercise.SQUATS, 60),
                Triple(todayEpochDay - 5, CalisthenicsExercise.CALF_RAISES, 80),
                Triple(todayEpochDay - 4, CalisthenicsExercise.PULL_UPS, 30),
                Triple(todayEpochDay - 4, CalisthenicsExercise.PUSH_UPS, 60),
                Triple(todayEpochDay - 3, CalisthenicsExercise.SQUATS, 75),
                Triple(todayEpochDay - 2, CalisthenicsExercise.PULL_UPS, 35),
                Triple(todayEpochDay - 2, CalisthenicsExercise.CALF_RAISES, 90),
                Triple(todayEpochDay - 1, CalisthenicsExercise.PUSH_UPS, 45),
                Triple(todayEpochDay - 1, CalisthenicsExercise.SQUATS, 50),
                Triple(todayEpochDay, CalisthenicsExercise.PULL_UPS, 10),
                Triple(todayEpochDay, CalisthenicsExercise.PUSH_UPS, 20)
            )
            calisthenicsSeed.forEachIndexed { idx, (day, ex, reps) ->
                val diffDays = (todayEpochDay - day).coerceAtLeast(0)
                dao.insertCalisthenicsLog(
                    CalisthenicsLogEntity(
                        exerciseName = ex.name,
                        reps = reps,
                        sourceTag = if (idx % 2 == 0) "PRE_TICKET" else "QUICK_ADD",
                        epochDay = day,
                        createdAt = now - diffDays * dayMs + idx * 1000L
                    )
                )
            }

            dao.insertBandMaxSet(
                BandMaxSetEntity(
                    exerciseType = BandExerciseType.BAND_PULL_UPS.name,
                    bandDescription = "Фиолетовая 15–30 кг",
                    reps = 22,
                    epochDay = todayEpochDay - 9,
                    createdAt = now - 9 * dayMs
                )
            )
            dao.insertBandMaxSet(
                BandMaxSetEntity(
                    exerciseType = BandExerciseType.BAND_PULL_UPS.name,
                    bandDescription = "Фиолетовая 15–30 кг",
                    reps = 26,
                    epochDay = todayEpochDay - 4,
                    createdAt = now - 4 * dayMs
                )
            )
            dao.insertBandMaxSet(
                BandMaxSetEntity(
                    exerciseType = BandExerciseType.BAND_PUSH_UPS.name,
                    bandDescription = "Красная 10–20 кг",
                    reps = 34,
                    epochDay = todayEpochDay - 7,
                    createdAt = now - 7 * dayMs
                )
            )
            dao.insertBandMaxSet(
                BandMaxSetEntity(
                    exerciseType = BandExerciseType.BAND_PUSH_UPS.name,
                    bandDescription = "Красная 10–20 кг",
                    reps = 38,
                    epochDay = todayEpochDay - 3,
                    createdAt = now - 3 * dayMs
                )
            )
        }
    }
}
