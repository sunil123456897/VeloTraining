package com.example.ui

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioHapticEngine
import com.example.data.local.BandMaxSetEntity
import com.example.data.local.BikeSessionLogEntity
import com.example.data.local.CalisthenicsLogEntity
import com.example.data.local.CockpitDatabase
import com.example.data.local.CockpitRepository
import com.example.data.model.BandExerciseType
import com.example.data.model.CalisthenicsExercise
import com.example.data.model.FlattenedWorkoutStep
import com.example.data.model.PresetWorkouts
import com.example.data.model.WorkoutBlock
import com.example.data.model.WorkoutProgram
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone
import java.util.UUID

enum class TicketSheetMode {
    HIDDEN,
    PRE_WORKOUT,
    POST_WORKOUT
}

data class ActiveWorkoutHudState(
    val isActive: Boolean = false,
    val isPaused: Boolean = false,
    val program: WorkoutProgram = PresetWorkouts.allPresets.first(),
    val flattenedSteps: List<FlattenedWorkoutStep> = PresetWorkouts.allPresets.first().flattenSteps(),
    val currentStepIndex: Int = 0,
    val stepRemainingSec: Int = 240,
    val stepDurationSec: Int = 240,
    val totalElapsedSec: Int = 0,
    val totalWorkoutSec: Int = PresetWorkouts.allPresets.first().totalDurationSec,
    val soundCuesEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val metronomeClickEnabled: Boolean = false,
    val isRightFootBeat: Boolean = false,
    val showDeltaHighlightBanner: Boolean = true
) {
    val currentStep: FlattenedWorkoutStep
        get() = flattenedSteps.getOrElse(currentStepIndex) { flattenedSteps.first() }

    val nextStep: FlattenedWorkoutStep?
        get() = flattenedSteps.getOrNull(currentStepIndex + 1)

    val isLastFiveSeconds: Boolean
        get() = stepRemainingSec in 1..5

    val isLastThreeSeconds: Boolean
        get() = stepRemainingSec in 1..3

    val overallProgressFraction: Float
        get() {
            if (totalWorkoutSec <= 0) return 0f
            return (totalElapsedSec.toFloat() / totalWorkoutSec.toFloat()).coerceIn(0f, 1f)
        }

    val currentStepProgressFraction: Float
        get() {
            if (stepDurationSec <= 0) return 0f
            val done = (stepDurationSec - stepRemainingSec).coerceAtLeast(0)
            return (done.toFloat() / stepDurationSec.toFloat()).coerceIn(0f, 1f)
        }
}

data class ExerciseVolumeSummary(
    val exercise: CalisthenicsExercise,
    val todayReps: Int,
    val weekReps: Int,
    val customStepA: Int,
    val customStepB: Int,
    val customStepC: Int
)

data class BandExerciseSummary(
    val exerciseType: BandExerciseType,
    val latestSet: BandMaxSetEntity?,
    val bestRecordSet: BandMaxSetEntity?,
    val previousBestBeforeLatest: Int?,
    val daysSinceLastSession: Int?,
    val history: List<BandMaxSetEntity>
)

data class DayActivityCell(
    val epochDay: Long,
    val dayOfMonth: Int,
    val monthShort: String,
    val dayOfWeekShort: String,
    val isToday: Boolean,
    val bikeSessions: List<BikeSessionLogEntity>,
    val totalCalisthenicsReps: Int,
    val calisthenicsByExercise: Map<CalisthenicsExercise, Int>,
    val bandSets: List<BandMaxSetEntity>
) {
    val hasBikeWorkout: Boolean get() = bikeSessions.isNotEmpty()
    val hasBandSet: Boolean get() = bandSets.isNotEmpty()
}

data class TicketSessionTally(
    val mode: TicketSheetMode = TicketSheetMode.HIDDEN,
    val addedByExercise: Map<CalisthenicsExercise, Int> = emptyMap()
) {
    val totalAddedReps: Int get() = addedByExercise.values.sum()
}

class CockpitViewModel(
    application: Application,
    private val repository: CockpitRepository,
    private val audioHapticEngine: AudioHapticEngine
) : AndroidViewModel(application) {

    private val _selectedProgramId = MutableStateFlow(PresetWorkouts.allPresets.first().id)
    val selectedProgramId: StateFlow<String> = _selectedProgramId.asStateFlow()

    private val _hudState = MutableStateFlow(ActiveWorkoutHudState())
    val hudState: StateFlow<ActiveWorkoutHudState> = _hudState.asStateFlow()

    private val _ticketState = MutableStateFlow(TicketSessionTally())
    val ticketState: StateFlow<TicketSessionTally> = _ticketState.asStateFlow()

    private val _editingProgram = MutableStateFlow<WorkoutProgram?>(null)
    val editingProgram: StateFlow<WorkoutProgram?> = _editingProgram.asStateFlow()

    private val _selectedCalendarEpochDay = MutableStateFlow(currentEpochDay())
    val selectedCalendarEpochDay: StateFlow<Long> = _selectedCalendarEpochDay.asStateFlow()

    // Custom quick step increments per exercise (defaults to +5 / +10 / +15 as requested)
    private val _customStepsMap = MutableStateFlow<Map<CalisthenicsExercise, Triple<Int, Int, Int>>>(
        CalisthenicsExercise.entries.associateWith { Triple(5, 10, 15) }
    )
    val customStepsMap: StateFlow<Map<CalisthenicsExercise, Triple<Int, Int, Int>>> = _customStepsMap.asStateFlow()

    val programsFlow: StateFlow<List<WorkoutProgram>> = repository.allProgramsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PresetWorkouts.allPresets
    )

    val selectedProgramFlow: StateFlow<WorkoutProgram> = combine(
        programsFlow,
        _selectedProgramId
    ) { programs, selectedId ->
        programs.find { it.id == selectedId } ?: programs.first()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PresetWorkouts.allPresets.first()
    )

    val calisthenicsSummaryFlow: StateFlow<List<ExerciseVolumeSummary>> = combine(
        repository.calisthenicsLogsFlow,
        _customStepsMap
    ) { logs, stepsMap ->
        val today = currentEpochDay()
        val weekStart = today - 6
        CalisthenicsExercise.entries.map { exercise ->
            val exLogs = logs.filter { it.exerciseName == exercise.name }
            val todaySum = exLogs.filter { it.epochDay == today }.sumOf { it.reps }
            val weekSum = exLogs.filter { it.epochDay in weekStart..today }.sumOf { it.reps }
            val steps = stepsMap[exercise] ?: Triple(5, 10, 15)
            ExerciseVolumeSummary(
                exercise = exercise,
                todayReps = todaySum,
                weekReps = weekSum,
                customStepA = steps.first,
                customStepB = steps.second,
                customStepC = steps.third
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalisthenicsExercise.entries.map {
            ExerciseVolumeSummary(it, 0, 0, 5, 10, 15)
        }
    )

    val todayCalisthenicsLogsFlow: StateFlow<List<CalisthenicsLogEntity>> = repository.calisthenicsLogsFlow
        .combine(_selectedCalendarEpochDay) { logs, _ ->
            val today = currentEpochDay()
            logs.filter { it.epochDay == today }.take(12)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val bandSummariesFlow: StateFlow<List<BandExerciseSummary>> = repository.bandMaxSetsFlow
        .combine(_selectedCalendarEpochDay) { allSets, _ ->
            val today = currentEpochDay()
            BandExerciseType.entries.map { type ->
                val list = allSets.filter { it.exerciseType == type.name }
                    .sortedByDescending { it.createdAt }
                val latest = list.firstOrNull()
                val best = list.maxByOrNull { it.reps }
                val previousBest = if (list.size >= 2) {
                    list.drop(1).maxOfOrNull { it.reps }
                } else null
                val daysSince = latest?.let { (today - it.epochDay).toInt().coerceAtLeast(0) }
                BandExerciseSummary(
                    exerciseType = type,
                    latestSet = latest,
                    bestRecordSet = best,
                    previousBestBeforeLatest = previousBest,
                    daysSinceLastSession = daysSince,
                    history = list
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BandExerciseType.entries.map {
                BandExerciseSummary(it, null, null, null, null, emptyList())
            }
        )

    val daysSinceAnyBandSessionFlow: StateFlow<Int?> = repository.bandMaxSetsFlow
        .combine(_selectedCalendarEpochDay) { allSets, _ ->
            val today = currentEpochDay()
            val latestDay = allSets.maxOfOrNull { it.epochDay }
            latestDay?.let { (today - it).toInt().coerceAtLeast(0) }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val calendar28DaysFlow: StateFlow<List<DayActivityCell>> = combine(
        repository.bikeSessionsFlow,
        repository.calisthenicsLogsFlow,
        repository.bandMaxSetsFlow
    ) { bikeSessions, caliLogs, bandSets ->
        val today = currentEpochDay()
        val startDay = today - 27
        (startDay..today).map { day ->
            val (dayNum, monthShort, dowShort) = formatEpochDayParts(day)
            val dayBikes = bikeSessions.filter { it.epochDay == day }
            val dayCali = caliLogs.filter { it.epochDay == day }
            val byEx = CalisthenicsExercise.entries.associateWith { ex ->
                dayCali.filter { it.exerciseName == ex.name }.sumOf { it.reps }
            }.filterValues { it > 0 }
            val dayBands = bandSets.filter { it.epochDay == day }

            DayActivityCell(
                epochDay = day,
                dayOfMonth = dayNum,
                monthShort = monthShort,
                dayOfWeekShort = dowShort,
                isToday = (day == today),
                bikeSessions = dayBikes,
                totalCalisthenicsReps = dayCali.sumOf { it.reps },
                calisthenicsByExercise = byEx,
                bandSets = dayBands
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Monotonic timer state variables
    private var timerJob: Job? = null
    private var metronomeJob: Job? = null
    private var stepTargetElapsedRealtimeMs: Long = 0L
    private var pausedRemainingMs: Long = 0L
    private var lastBeepedSecond: Int = -1
    private var completedStepsDurationSec: Int = 0

    init {
        viewModelScope.launch {
            repository.seedInitialHistoryIfEmpty(currentEpochDay())
        }
    }

    fun selectProgram(programId: String) {
        _selectedProgramId.value = programId
        val prog = programsFlow.value.find { it.id == programId } ?: return
        if (!_hudState.value.isActive) {
            val steps = prog.flattenSteps()
            val firstStep = steps.firstOrNull()
            _hudState.update {
                it.copy(
                    program = prog,
                    flattenedSteps = steps,
                    currentStepIndex = 0,
                    stepRemainingSec = firstStep?.durationSec ?: 60,
                    stepDurationSec = firstStep?.durationSec ?: 60,
                    totalElapsedSec = 0,
                    totalWorkoutSec = prog.totalDurationSec
                )
            }
        }
    }

    /**
     * Triggered when user taps "Начать велотренировку".
     * Opens the Pre-Workout Calisthenics Ticket sheet ("Входной билет").
     */
    fun requestStartWorkoutWithTicket() {
        _ticketState.value = TicketSessionTally(
            mode = TicketSheetMode.PRE_WORKOUT,
            addedByExercise = emptyMap()
        )
    }

    fun dismissTicketAndProceed() {
        val currentMode = _ticketState.value.mode
        _ticketState.value = TicketSessionTally(mode = TicketSheetMode.HIDDEN)
        if (currentMode == TicketSheetMode.PRE_WORKOUT) {
            startActiveWorkoutNow()
        }
    }

    fun addTicketCalisthenics(exercise: CalisthenicsExercise, reps: Int) {
        val mode = _ticketState.value.mode
        val source = if (mode == TicketSheetMode.PRE_WORKOUT) "PRE_TICKET" else "POST_TICKET"
        viewModelScope.launch {
            repository.logCalisthenics(
                exercise = exercise,
                reps = reps,
                sourceTag = source,
                epochDay = currentEpochDay()
            )
        }
        _ticketState.update { state ->
            val currentCount = state.addedByExercise[exercise] ?: 0
            state.copy(
                addedByExercise = state.addedByExercise + (exercise to (currentCount + reps))
            )
        }
    }

    fun startActiveWorkoutNow() {
        val prog = selectedProgramFlow.value
        val steps = prog.flattenSteps()
        if (steps.isEmpty()) return
        val firstStep = steps.first()

        completedStepsDurationSec = 0
        lastBeepedSecond = -1
        val nowMs = SystemClock.elapsedRealtime()
        stepTargetElapsedRealtimeMs = nowMs + firstStep.durationSec * 1000L
        pausedRemainingMs = firstStep.durationSec * 1000L

        _hudState.update {
            it.copy(
                isActive = true,
                isPaused = false,
                program = prog,
                flattenedSteps = steps,
                currentStepIndex = 0,
                stepRemainingSec = firstStep.durationSec,
                stepDurationSec = firstStep.durationSec,
                totalElapsedSec = 0,
                totalWorkoutSec = prog.totalDurationSec,
                showDeltaHighlightBanner = true
            )
        }

        audioHapticEngine.triggerPhaseTransition(
            isAttackPhase = firstStep.phaseType.isHighIntensity,
            soundEnabled = _hudState.value.soundCuesEnabled,
            vibrationEnabled = _hudState.value.vibrationEnabled
        )

        startMonotonicLoop()
        startMetronomeLoop()
    }

    fun togglePauseResume() {
        val state = _hudState.value
        if (!state.isActive) return
        val nowMs = SystemClock.elapsedRealtime()
        if (state.isPaused) {
            // Resume
            stepTargetElapsedRealtimeMs = nowMs + pausedRemainingMs
            _hudState.update { it.copy(isPaused = false) }
            startMonotonicLoop()
            startMetronomeLoop()
        } else {
            // Pause
            pausedRemainingMs = (stepTargetElapsedRealtimeMs - nowMs).coerceAtLeast(0L)
            timerJob?.cancel()
            metronomeJob?.cancel()
            _hudState.update { it.copy(isPaused = true) }
        }
    }

    fun adjustCurrentStepTime(deltaSeconds: Int) {
        val state = _hudState.value
        if (!state.isActive) return
        val deltaMs = deltaSeconds * 1000L
        val nowMs = SystemClock.elapsedRealtime()

        if (state.isPaused) {
            pausedRemainingMs = (pausedRemainingMs + deltaMs).coerceAtLeast(1000L)
            val newSec = ((pausedRemainingMs + 999L) / 1000L).toInt()
            val newStepDur = state.stepDurationSec.coerceAtLeast(newSec)
            _hudState.update {
                it.copy(
                    stepRemainingSec = newSec,
                    stepDurationSec = newStepDur
                )
            }
        } else {
            stepTargetElapsedRealtimeMs = (stepTargetElapsedRealtimeMs + deltaMs).coerceAtLeast(nowMs + 1000L)
            val remMs = (stepTargetElapsedRealtimeMs - nowMs).coerceAtLeast(0L)
            val newSec = ((remMs + 999L) / 1000L).toInt()
            val newStepDur = state.stepDurationSec.coerceAtLeast(newSec)
            lastBeepedSecond = -1
            _hudState.update {
                it.copy(
                    stepRemainingSec = newSec,
                    stepDurationSec = newStepDur
                )
            }
        }
    }

    fun skipToNextStep() {
        val state = _hudState.value
        if (!state.isActive) return
        advanceToStep(state.currentStepIndex + 1)
    }

    fun skipToPreviousStep() {
        val state = _hudState.value
        if (!state.isActive || state.currentStepIndex <= 0) return
        val prevIdx = state.currentStepIndex - 1
        val prevStep = state.flattenedSteps[prevIdx]
        completedStepsDurationSec = (completedStepsDurationSec - prevStep.durationSec).coerceAtLeast(0)
        advanceToStep(prevIdx, countCurrentCompleted = false)
    }

    fun finishWorkoutEarly(saveToLog: Boolean = true) {
        val state = _hudState.value
        timerJob?.cancel()
        metronomeJob?.cancel()

        val actualElapsed = state.totalElapsedSec.coerceAtLeast(60)
        if (saveToLog && state.isActive) {
            viewModelScope.launch {
                repository.logBikeSession(
                    workoutId = state.program.id,
                    workoutTitle = state.program.title,
                    durationSec = actualElapsed,
                    epochDay = currentEpochDay()
                )
            }
        }

        _hudState.update { it.copy(isActive = false, isPaused = false) }
        // Show "Выходной билет" (Post-Workout Calisthenics Ticket)
        _ticketState.value = TicketSessionTally(
            mode = TicketSheetMode.POST_WORKOUT,
            addedByExercise = emptyMap()
        )
    }

    fun toggleSoundCues() {
        _hudState.update { it.copy(soundCuesEnabled = !it.soundCuesEnabled) }
    }

    fun toggleVibration() {
        _hudState.update { it.copy(vibrationEnabled = !it.vibrationEnabled) }
    }

    fun toggleMetronomeClick() {
        _hudState.update { it.copy(metronomeClickEnabled = !it.metronomeClickEnabled) }
    }

    private fun startMonotonicLoop() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_hudState.value.isActive && !_hudState.value.isPaused) {
                val nowMs = SystemClock.elapsedRealtime()
                val remainingMs = stepTargetElapsedRealtimeMs - nowMs

                if (remainingMs <= 0L) {
                    advanceToStep(_hudState.value.currentStepIndex + 1)
                } else {
                    val remainingSec = ((remainingMs + 999L) / 1000L).toInt()
                    val currentState = _hudState.value
                    val stepElapsed = (currentState.stepDurationSec - remainingSec).coerceAtLeast(0)
                    val totalElapsed = completedStepsDurationSec + stepElapsed

                    if (remainingSec in 1..3 && remainingSec != lastBeepedSecond) {
                        lastBeepedSecond = remainingSec
                        audioHapticEngine.triggerCountdownBeep(
                            secondsLeft = remainingSec,
                            soundEnabled = currentState.soundCuesEnabled,
                            vibrationEnabled = currentState.vibrationEnabled
                        )
                    }

                    // Show prominent delta banner during first 7 seconds of each phase
                    val showDelta = stepElapsed <= 7

                    _hudState.update {
                        it.copy(
                            stepRemainingSec = remainingSec,
                            totalElapsedSec = totalElapsed,
                            showDeltaHighlightBanner = showDelta
                        )
                    }
                }
                delay(100L)
            }
        }
    }

    private fun startMetronomeLoop() {
        metronomeJob?.cancel()
        metronomeJob = viewModelScope.launch {
            while (_hudState.value.isActive && !_hudState.value.isPaused) {
                val state = _hudState.value
                val rpm = state.currentStep.targetRpmMid.coerceIn(50, 140)
                // RPM is full pedal revolutions per minute -> 2 foot strokes per revolution (Left + Right)
                val strokesPerMinute = rpm * 2
                val intervalMs = (60_000L / strokesPerMinute).coerceAtLeast(200L)

                val nextFootIsRight = !state.isRightFootBeat
                _hudState.update { it.copy(isRightFootBeat = nextFootIsRight) }

                if (state.metronomeClickEnabled) {
                    audioHapticEngine.triggerMetronomeTick(isRightFoot = nextFootIsRight)
                }
                delay(intervalMs)
            }
        }
    }

    private fun advanceToStep(nextIndex: Int, countCurrentCompleted: Boolean = true) {
        val state = _hudState.value
        if (countCurrentCompleted) {
            completedStepsDurationSec += state.currentStep.durationSec
        }

        if (nextIndex >= state.flattenedSteps.size) {
            // Workout complete!
            timerJob?.cancel()
            metronomeJob?.cancel()
            audioHapticEngine.triggerPhaseTransition(
                isAttackPhase = false,
                soundEnabled = state.soundCuesEnabled,
                vibrationEnabled = state.vibrationEnabled
            )
            viewModelScope.launch {
                repository.logBikeSession(
                    workoutId = state.program.id,
                    workoutTitle = state.program.title,
                    durationSec = state.totalWorkoutSec,
                    epochDay = currentEpochDay()
                )
            }
            _hudState.update { it.copy(isActive = false, isPaused = false) }
            _ticketState.value = TicketSessionTally(
                mode = TicketSheetMode.POST_WORKOUT,
                addedByExercise = emptyMap()
            )
            return
        }

        val nextStep = state.flattenedSteps[nextIndex]
        lastBeepedSecond = -1
        val nowMs = SystemClock.elapsedRealtime()
        stepTargetElapsedRealtimeMs = nowMs + nextStep.durationSec * 1000L
        pausedRemainingMs = nextStep.durationSec * 1000L

        _hudState.update {
            it.copy(
                currentStepIndex = nextIndex,
                stepRemainingSec = nextStep.durationSec,
                stepDurationSec = nextStep.durationSec,
                totalElapsedSec = completedStepsDurationSec,
                showDeltaHighlightBanner = true
            )
        }

        audioHapticEngine.triggerPhaseTransition(
            isAttackPhase = nextStep.phaseType.isHighIntensity,
            soundEnabled = state.soundCuesEnabled,
            vibrationEnabled = state.vibrationEnabled
        )
    }

    // Calisthenics Quick Logging
    fun logQuickCalisthenics(exercise: CalisthenicsExercise, reps: Int) {
        viewModelScope.launch {
            repository.logCalisthenics(
                exercise = exercise,
                reps = reps,
                sourceTag = "QUICK_ADD",
                epochDay = currentEpochDay()
            )
        }
    }

    fun deleteCalisthenicsEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteCalisthenicsLog(id)
        }
    }

    fun updateCustomStepsForExercise(exercise: CalisthenicsExercise, stepA: Int, stepB: Int, stepC: Int) {
        _customStepsMap.update { current ->
            current + (exercise to Triple(
                stepA.coerceIn(1, 200),
                stepB.coerceIn(1, 200),
                stepC.coerceIn(1, 200)
            ))
        }
    }

    // Resistance Band Max Sets
    fun logBandMaxSet(exerciseType: BandExerciseType, bandDescription: String, reps: Int) {
        if (reps <= 0) return
        viewModelScope.launch {
            repository.logBandMaxSet(
                exerciseType = exerciseType,
                bandDescription = bandDescription,
                reps = reps,
                epochDay = currentEpochDay()
            )
        }
    }

    fun deleteBandMaxSet(id: Long) {
        viewModelScope.launch {
            repository.deleteBandMaxSet(id)
        }
    }

    // Custom Workout Builder (Block-based with series repeat counts)
    fun openNewCustomWorkoutBuilder() {
        val template = WorkoutProgram(
            id = "custom_${UUID.randomUUID().toString().take(8)}",
            title = "Моя интервальная схема",
            subtitle = "Пользовательская программа без закисления",
            tag = "СВОЯ СХЕМА",
            isCustom = true,
            blocks = PresetWorkouts.allPresets.first().blocks
        )
        _editingProgram.value = template
    }

    fun openDuplicateOrEditProgram(program: WorkoutProgram) {
        val editable = if (program.isCustom) {
            program
        } else {
            program.copy(
                id = "custom_${UUID.randomUUID().toString().take(8)}",
                title = "${program.title} (Копия)",
                tag = "СВОЯ СХЕМА",
                isCustom = true
            )
        }
        _editingProgram.value = editable
    }

    fun closeWorkoutBuilder() {
        _editingProgram.value = null
    }

    fun saveCustomWorkout(program: WorkoutProgram) {
        viewModelScope.launch {
            val totalMin = program.totalMinutesRounded
            val updated = program.copy(
                tag = "${totalMin} МИН • СВОЯ",
                isCustom = true
            )
            repository.saveCustomWorkout(updated)
            _selectedProgramId.value = updated.id
            _editingProgram.value = null
        }
    }

    fun deleteCustomWorkout(programId: String) {
        viewModelScope.launch {
            repository.deleteCustomWorkout(programId)
            if (_selectedProgramId.value == programId) {
                _selectedProgramId.value = PresetWorkouts.allPresets.first().id
            }
            _editingProgram.value = null
        }
    }

    fun selectCalendarDay(epochDay: Long) {
        _selectedCalendarEpochDay.value = epochDay
    }

    companion object {
        fun currentEpochDay(): Long {
            val cal = Calendar.getInstance()
            val offsetMs = cal.timeZone.getOffset(cal.timeInMillis)
            return (cal.timeInMillis + offsetMs) / 86_400_000L
        }

        fun formatEpochDayParts(epochDay: Long): Triple<Int, String, String> {
            val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utcCal.timeInMillis = epochDay * 86_400_000L + 43_200_000L
            val dayOfMonth = utcCal.get(Calendar.DAY_OF_MONTH)
            val monthNames = arrayOf(
                "янв", "фев", "мар", "апр", "май", "июн",
                "июл", "авг", "сен", "окт", "ноя", "дек"
            )
            val dowNames = arrayOf("ВС", "ПН", "ВТ", "СР", "ЧТ", "ПТ", "СБ")
            val month = monthNames[utcCal.get(Calendar.MONTH)]
            val dow = dowNames[utcCal.get(Calendar.DAY_OF_WEEK) - 1]
            return Triple(dayOfMonth, month, dow)
        }

        fun provideFactory(application: Application): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = CockpitDatabase.getInstance(application)
                    val repo = CockpitRepository(db.cockpitDao())
                    val audio = AudioHapticEngine(application)
                    return CockpitViewModel(application, repo, audio) as T
                }
            }
        }
    }
}
