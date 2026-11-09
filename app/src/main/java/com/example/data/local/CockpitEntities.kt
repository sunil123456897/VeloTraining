package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import androidx.room.TypeConverter
import com.example.data.model.BandExerciseType
import com.example.data.model.CalisthenicsExercise
import com.example.data.model.DialPosition
import com.example.data.model.HandPosition
import com.example.data.model.PhaseType
import com.example.data.model.PresetWorkouts
import com.example.data.model.WorkoutBlock

/**
 * Room TypeConverters for persisting structured repeatable series blocks (e.g. 8x 20/40s)
 * and strongly-typed enums in SQLite.
 */
class CockpitTypeConverters {

    @TypeConverter
    fun fromWorkoutBlocksList(blocks: List<WorkoutBlock>): String {
        return PresetWorkouts.blocksToJson(blocks)
    }

    @TypeConverter
    fun toWorkoutBlocksList(json: String): List<WorkoutBlock> {
        return PresetWorkouts.jsonToBlocks(json)
    }

    @TypeConverter
    fun fromDialPosition(position: DialPosition): String = position.name

    @TypeConverter
    fun toDialPosition(value: String): DialPosition =
        runCatching { DialPosition.valueOf(value) }.getOrDefault(DialPosition.RP_BASE)

    @TypeConverter
    fun fromHandPosition(position: HandPosition?): String? = position?.name

    @TypeConverter
    fun toHandPosition(value: String?): HandPosition? =
        value?.takeIf { it.isNotBlank() }?.let { runCatching { HandPosition.valueOf(it) }.getOrNull() }

    @TypeConverter
    fun fromPhaseType(phaseType: PhaseType): String = phaseType.name

    @TypeConverter
    fun toPhaseType(value: String): PhaseType =
        runCatching { PhaseType.valueOf(value) }.getOrDefault(PhaseType.ATTACK)

    @TypeConverter
    fun fromCalisthenicsExercise(exercise: CalisthenicsExercise): String = exercise.name

    @TypeConverter
    fun toCalisthenicsExercise(value: String): CalisthenicsExercise =
        runCatching { CalisthenicsExercise.valueOf(value) }.getOrDefault(CalisthenicsExercise.PULL_UPS)

    @TypeConverter
    fun fromBandExerciseType(type: BandExerciseType): String = type.name

    @TypeConverter
    fun toBandExerciseType(value: String): BandExerciseType =
        runCatching { BandExerciseType.valueOf(value) }.getOrDefault(BandExerciseType.BAND_PULL_UPS)
}

/**
 * Main interval workout program entity.
 * Stores both metadata and the typed list of repeatable series blocks (`blocks`),
 * e.g. Warmup (1x 240s) -> Series 1 (8x [20s Attack + 40s Recovery]) -> Cooldown (1x 300s).
 */
@Entity(tableName = "custom_workouts")
data class CustomWorkoutEntity(
    @PrimaryKey val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val isPreset: Boolean = false,
    val blocks: List<WorkoutBlock>,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Relational entity representing a repeatable series block inside a workout
 * (for example: "Серия 1", repeatCount = 8 for an 8x 20/40 block).
 */
@Entity(
    tableName = "workout_series_blocks",
    foreignKeys = [
        ForeignKey(
            entity = CustomWorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["workoutId", "orderIndex"])]
)
data class WorkoutSeriesBlockEntity(
    @PrimaryKey(autoGenerate = true) val blockId: Long = 0,
    val workoutId: String,
    val orderIndex: Int,
    val blockName: String,
    val repeatCount: Int // e.g. 8 for 8x (20s / 40s)
)

/**
 * Relational entity representing an individual phase/step inside a repeatable series block
 * (for example: 20s Attack at RP + 180°, 90-95 RPM, Standing Bullhorn on odd rounds / Sitting Aero on even rounds).
 */
@Entity(
    tableName = "workout_interval_steps",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSeriesBlockEntity::class,
            parentColumns = ["blockId"],
            childColumns = ["seriesBlockId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesBlockId", "stepOrderIndex"])]
)
data class WorkoutIntervalStepEntity(
    @PrimaryKey(autoGenerate = true) val stepId: Long = 0,
    val seriesBlockId: Long,
    val stepOrderIndex: Int,
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

/**
 * Nested relation POJO combining a repeatable series block with its ordered steps.
 */
data class SeriesBlockWithSteps(
    @Embedded val block: WorkoutSeriesBlockEntity,
    @Relation(
        parentColumn = "blockId",
        entityColumn = "seriesBlockId"
    )
    val steps: List<WorkoutIntervalStepEntity>
)

/**
 * Full relational POJO combining a workout program with all of its repeatable series blocks and steps.
 */
data class WorkoutWithSeriesBlocks(
    @Embedded val workout: CustomWorkoutEntity,
    @Relation(
        entity = WorkoutSeriesBlockEntity::class,
        parentColumn = "id",
        entityColumn = "workoutId"
    )
    val seriesBlocks: List<SeriesBlockWithSteps>
)

/**
 * Completed spin-bike workout session log for the 4-week activity calendar.
 */
@Entity(
    tableName = "bike_session_logs",
    indices = [Index(value = ["epochDay"])]
)
data class BikeSessionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: String,
    val workoutTitle: String,
    val durationSec: Int,
    val epochDay: Long,
    val completedAt: Long = System.currentTimeMillis()
)

/**
 * Background non-acidifying calisthenics log entry (Pull-ups, Push-ups, Squats, Calf Raises).
 * Tracks whether the set was logged via the Pre-Workout Ticket ("PRE_TICKET"),
 * Post-Workout Ticket ("POST_TICKET"), or 1-tap Daily Volume tab ("QUICK_ADD").
 */
@Entity(
    tableName = "calisthenics_logs",
    indices = [Index(value = ["epochDay", "exerciseName"])]
)
data class CalisthenicsLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseName: String,
    val reps: Int,
    val sourceTag: String, // "PRE_TICKET", "POST_TICKET", "QUICK_ADD"
    val epochDay: Long,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Long resistance band max set entry (1-2 times per week) for Pull-ups and Push-ups with a band.
 */
@Entity(
    tableName = "band_max_sets",
    indices = [Index(value = ["exerciseType", "epochDay"])]
)
data class BandMaxSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseType: String, // BAND_PULL_UPS or BAND_PUSH_UPS
    val bandDescription: String,
    val reps: Int,
    val epochDay: Long,
    val createdAt: Long = System.currentTimeMillis()
)
