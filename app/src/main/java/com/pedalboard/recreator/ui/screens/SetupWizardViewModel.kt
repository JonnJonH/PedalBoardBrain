package com.pedalboard.recreator.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pedalboard.recreator.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class WizardStep {
    object AmpInput : WizardStep()
    // Pre-amp: ask first, then capture, then continue
    object PreAmpQuestion : WizardStep()
    object PreAmpCapture : WizardStep()
    object PreAmpContinue : WizardStep()
    // FX Loop
    object FxLoopEntry : WizardStep()
    object FxLoopMonoChoice : WizardStep()
    object FxLoopStereo : WizardStep()
    object FxLoopSplitLeft : WizardStep()
    object FxLoopSplitLeftChoice : WizardStep()
    object FxLoopSplitRight : WizardStep()
    object FxLoopSplitRightChoice : WizardStep()
    object FxLoopPostSplitChoice : WizardStep()
    object FxLoopMerge : WizardStep()
    object Finished : WizardStep()
}

class SetupWizardViewModel(application: Application, private val sessionId: String) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dao()

    private val _step = MutableStateFlow<WizardStep>(WizardStep.AmpInput)
    val step: StateFlow<WizardStep> = _step.asStateFlow()

    private val _signalState = MutableStateFlow(SignalState.MONO)
    val signalState: StateFlow<SignalState> = _signalState.asStateFlow()

    val pedals = dao.getPedalsForSession(sessionId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val connections = dao.getConnectionsForSession(sessionId).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var lastMonoId: String? = null
    private var lastLeftId: String? = null
    private var lastRightId: String? = null
    private var splitSourceId: String? = null
    private var leftPosition: Int = 0
    private var rightPosition: Int = 0
    private var monoPosition: Int = 0
    private var preAmpCount: Int = 0

    // -- Amp ------------------------------------------------------------------
    fun onAmpCaptured(imagePath: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Guitar Output", imagePath, imagePath, ChainStage.AMP, 0, ChannelType.MONO))
            lastMonoId = id
            monoPosition = 0
            _step.value = WizardStep.PreAmpQuestion
        }
    }

    // -- Pre-amp ---------------------------------------------------------------
    fun answerHasPreAmp(hasPreAmp: Boolean) {
        _step.value = if (hasPreAmp) WizardStep.PreAmpCapture else WizardStep.FxLoopEntry
    }

    fun onPreAmpPedalCaptured(imagePath: String) {
        viewModelScope.launch {
            preAmpCount++
            monoPosition++
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Pre-Amp Pedal $preAmpCount", imagePath, imagePath, ChainStage.PRE_AMP, monoPosition, ChannelType.MONO))
            lastMonoId?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.MONO)) }
            lastMonoId = id
            _step.value = WizardStep.PreAmpContinue
        }
    }

    fun addAnotherPreAmp() { _step.value = WizardStep.PreAmpCapture }
    fun finishPreAmp()     { _step.value = WizardStep.FxLoopEntry }

    // -- FX Loop entry (first pedal - must be mono) ----------------------------
    fun onFxLoopEntryCaptured(imagePath: String) {
        viewModelScope.launch {
            monoPosition++
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "FX Loop Pedal 1", imagePath, imagePath, ChainStage.FX_LOOP, monoPosition, ChannelType.MONO))
            lastMonoId?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.MONO)) }
            lastMonoId = id
            _step.value = WizardStep.FxLoopMonoChoice
        }
    }

    // -- FX mono continuation --------------------------------------------------
    fun onFxMonoPedalCaptured(imagePath: String) {
        viewModelScope.launch {
            monoPosition++
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "FX Pedal", imagePath, imagePath, ChainStage.FX_LOOP, monoPosition, ChannelType.MONO))
            lastMonoId?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.MONO)) }
            lastMonoId = id
            _step.value = WizardStep.FxLoopMonoChoice
        }
    }

    // -- FX stereo continuation ------------------------------------------------
    fun onFxStereoPedalCaptured(imagePath: String) {
        viewModelScope.launch {
            monoPosition++
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Stereo Pedal", imagePath, imagePath, ChainStage.FX_LOOP, monoPosition, ChannelType.STEREO))
            lastMonoId?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.STEREO)) }
            lastMonoId = id
            _step.value = WizardStep.FxLoopStereo
        }
    }

    fun convertToStereo() {
        _signalState.value = SignalState.STEREO
        _step.value = WizardStep.FxLoopStereo
    }

    // -- Split -----------------------------------------------------------------
    fun chooseSplit() {
        splitSourceId = lastMonoId
        val basePos = monoPosition + 1
        leftPosition = basePos
        rightPosition = basePos
        _signalState.value = SignalState.SPLIT
        lastLeftId = null
        lastRightId = null
        _step.value = WizardStep.FxLoopSplitLeft
    }

    fun onFxSplitLeftCaptured(imagePath: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Left Pedal", imagePath, imagePath, ChainStage.FX_LOOP, leftPosition, ChannelType.LEFT))
            (lastLeftId ?: splitSourceId)?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.LEFT)) }
            lastLeftId = id
            _step.value = WizardStep.FxLoopSplitLeftChoice
        }
    }

    fun incrementLeft() { leftPosition++ }  // just increment, camera launched directly from screen
    fun doneWithLeft() { _step.value = WizardStep.FxLoopSplitRight }

    fun onFxSplitRightCaptured(imagePath: String) {
        viewModelScope.launch {
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Right Pedal", imagePath, imagePath, ChainStage.FX_LOOP, rightPosition, ChannelType.RIGHT))
            (lastRightId ?: splitSourceId)?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.RIGHT)) }
            lastRightId = id
            _step.value = WizardStep.FxLoopSplitRightChoice
        }
    }

    fun incrementRight() { rightPosition++ }  // just increment, camera launched directly from screen
    fun doneWithRight() { _step.value = WizardStep.FxLoopPostSplitChoice }

    // -- Merge -----------------------------------------------------------------
    fun chooseMerge() { _step.value = WizardStep.FxLoopMerge }

    fun onMergePedalCaptured(imagePath: String) {
        viewModelScope.launch {
            monoPosition = maxOf(leftPosition, rightPosition) + 1
            val id = UUID.randomUUID().toString()
            dao.insertPedal(PedalEntity(id, sessionId, "Merge Pedal", imagePath, imagePath, ChainStage.FX_LOOP, monoPosition, ChannelType.STEREO))
            lastLeftId?.let  { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.LEFT)) }
            lastRightId?.let { dao.insertConnection(ConnectionEntity(UUID.randomUUID().toString(), sessionId, it, id, ChannelType.RIGHT)) }
            lastMonoId = id
            lastLeftId = null
            lastRightId = null
            splitSourceId = null
            _signalState.value = SignalState.STEREO
            _step.value = WizardStep.FxLoopStereo
        }
    }

    fun finishWizard() { _step.value = WizardStep.Finished }
}



