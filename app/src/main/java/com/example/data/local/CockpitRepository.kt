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

    suspend fun logBikeSession(
        workoutId: String,
        workoutTitle: String,
        durationSec: Int,
        legFeeling: String,
        rpShiftDegrees: Int,
        epochDay: Long
    ) {
        dao.insertBikeSession(
            BikeSessionLogEntity(
                workoutId = workoutId,
                workoutTitle = workoutTitle,
                durationSec = durationSec,
                legFeeling = legFeeling,
                rpShiftDegrees = rpShiftDegrees,
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

    suspend fun clearAllHistory() {
        dao.clearAllHistory()
    }

    /**
     * Kept as a no-op so statistics start from a clean zero on first launch.
     */
    @Suppress("UNUSED_PARAMETER")
    suspend fun seedInitialHistoryIfEmpty(todayEpochDay: Long) {
        // Intentionally empty: user statistics start from clean zero
    }
}
