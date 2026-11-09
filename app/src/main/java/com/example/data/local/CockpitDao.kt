package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.WorkoutBlock
import com.example.data.model.WorkoutStepTemplate
import kotlinx.coroutines.flow.Flow

@Dao
interface CockpitDao {

    // =========================================================================
    // 1. INTERVAL WORKOUTS & REPEATABLE SERIES BLOCKS (e.g. 8x 20/40)
    // =========================================================================

    @Query("SELECT * FROM custom_workouts ORDER BY updatedAt DESC")
    fun observeCustomWorkouts(): Flow<List<CustomWorkoutEntity>>

    @Query("SELECT * FROM custom_workouts WHERE id = :id LIMIT 1")
    suspend fun getCustomWorkoutById(id: String): CustomWorkoutEntity?

    @Transaction
    @Query("SELECT * FROM custom_workouts WHERE id = :id LIMIT 1")
    fun observeWorkoutWithSeriesBlocks(id: String): Flow<WorkoutWithSeriesBlocks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomWorkout(workout: CustomWorkoutEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeriesBlock(block: WorkoutSeriesBlockEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIntervalSteps(steps: List<WorkoutIntervalStepEntity>)

    @Query("DELETE FROM workout_series_blocks WHERE workoutId = :workoutId")
    suspend fun deleteSeriesBlocksForWorkout(workoutId: String)

    /**
     * Atomically saves a workout entity along with its normalized repeatable series blocks
     * (e.g., 8x [20s Attack + 40s Recovery]) and individual interval steps.
     */
    @Transaction
    suspend fun saveWorkoutWithSeriesBlocks(workout: CustomWorkoutEntity) {
        upsertCustomWorkout(workout)
        deleteSeriesBlocksForWorkout(workout.id)
        workout.blocks.forEachIndexed { blockIdx, block: WorkoutBlock ->
            val generatedBlockId = insertSeriesBlock(
                WorkoutSeriesBlockEntity(
                    workoutId = workout.id,
                    orderIndex = blockIdx,
                    blockName = block.blockName,
                    repeatCount = block.repeatCount
                )
            )
            val stepEntities = block.steps.mapIndexed { stepIdx, step: WorkoutStepTemplate ->
                WorkoutIntervalStepEntity(
                    seriesBlockId = generatedBlockId,
                    stepOrderIndex = stepIdx,
                    title = step.title,
                    durationSec = step.durationSec,
                    phaseType = step.phaseType,
                    dialPosition = step.dialPosition,
                    minRpm = step.minRpm,
                    maxRpm = step.maxRpm,
                    handPosition = step.handPosition,
                    evenRoundHandPosition = step.evenRoundHandPosition,
                    coachNote = step.coachNote
                )
            }
            insertIntervalSteps(stepEntities)
        }
    }

    @Query("DELETE FROM custom_workouts WHERE id = :id")
    suspend fun deleteCustomWorkout(id: String)

    // =========================================================================
    // 2. COMPLETED SPIN-BIKE SESSION LOGS
    // =========================================================================

    @Query("SELECT * FROM bike_session_logs ORDER BY completedAt DESC")
    fun observeBikeSessions(): Flow<List<BikeSessionLogEntity>>

    @Query("SELECT * FROM bike_session_logs WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY completedAt DESC")
    fun observeBikeSessionsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<BikeSessionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBikeSession(session: BikeSessionLogEntity): Long

    @Query("SELECT COUNT(*) FROM bike_session_logs")
    suspend fun countBikeSessions(): Int

    // =========================================================================
    // 3. BACKGROUND CALISTHENICS VOLUME LOGS (Pull-ups, Push-ups, Squats, Calves)
    // =========================================================================

    @Query("SELECT * FROM calisthenics_logs ORDER BY createdAt DESC")
    fun observeCalisthenicsLogs(): Flow<List<CalisthenicsLogEntity>>

    @Query("SELECT * FROM calisthenics_logs WHERE epochDay = :epochDay ORDER BY createdAt DESC")
    fun observeCalisthenicsForDay(epochDay: Long): Flow<List<CalisthenicsLogEntity>>

    @Query("SELECT * FROM calisthenics_logs WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay ORDER BY createdAt DESC")
    fun observeCalisthenicsInRange(startEpochDay: Long, endEpochDay: Long): Flow<List<CalisthenicsLogEntity>>

    @Query("SELECT COALESCE(SUM(reps), 0) FROM calisthenics_logs WHERE exerciseName = :exerciseName AND epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeExerciseVolumeSum(exerciseName: String, startEpochDay: Long, endEpochDay: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalisthenicsLog(log: CalisthenicsLogEntity): Long

    @Query("DELETE FROM calisthenics_logs WHERE id = :id")
    suspend fun deleteCalisthenicsLog(id: Long)

    @Query("SELECT COUNT(*) FROM calisthenics_logs")
    suspend fun countCalisthenicsLogs(): Int

    // =========================================================================
    // 4. RESISTANCE BAND LONG MAX SETS (1-2 times per week)
    // =========================================================================

    @Query("SELECT * FROM band_max_sets ORDER BY createdAt DESC")
    fun observeBandMaxSets(): Flow<List<BandMaxSetEntity>>

    @Query("SELECT * FROM band_max_sets WHERE exerciseType = :exerciseType ORDER BY createdAt DESC")
    fun observeBandMaxSetsByExercise(exerciseType: String): Flow<List<BandMaxSetEntity>>

    @Query("SELECT * FROM band_max_sets WHERE exerciseType = :exerciseType ORDER BY reps DESC, createdAt DESC LIMIT 1")
    fun observeBestBandRecord(exerciseType: String): Flow<BandMaxSetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBandMaxSet(entry: BandMaxSetEntity): Long

    @Query("DELETE FROM band_max_sets WHERE id = :id")
    suspend fun deleteBandMaxSet(id: Long)

    // =========================================================================
    // 5. CLEAR ALL HISTORY
    // =========================================================================

    @Query("DELETE FROM bike_session_logs")
    suspend fun deleteAllBikeSessions()

    @Query("DELETE FROM calisthenics_logs")
    suspend fun deleteAllCalisthenicsLogs()

    @Query("DELETE FROM band_max_sets")
    suspend fun deleteAllBandMaxSets()

    @Transaction
    suspend fun clearAllHistory() {
        deleteAllBikeSessions()
        deleteAllCalisthenicsLogs()
        deleteAllBandMaxSets()
    }
}
