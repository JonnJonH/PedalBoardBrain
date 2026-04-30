package com.pedalboard.recreator.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SetupWizardScreen(
    sessionId: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    viewModel: SetupWizardViewModel
) {
    val step by viewModel.step.collectAsStateWithLifecycle()

    var showCamera by remember { mutableStateOf(false) }
    var currentPhotoCallback by remember { mutableStateOf<(String) -> Unit>({}) }

    if (showCamera) {
        SimpleCameraScreen(
            onPhotoCaptured = { showCamera = false; currentPhotoCallback(it) },
            onCancel = { showCamera = false }
        )
        return
    }

    if (step == WizardStep.Finished) {
        LaunchedEffect(Unit) { onComplete() }
        return
    }

    fun launchCamera(callback: (String) -> Unit) {
        currentPhotoCallback = callback
        showCamera = true
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Crossfade(targetState = step, label = "wizard_step") { currentStep ->
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (currentStep) {

                        // -- Amp ----------------------------------------------
                        is WizardStep.AmpInput -> {
                            WizardPrompt("Start Session", "Take a photo of your amp or amp settings.")
                            WizardButton("Take Photo") { launchCamera { viewModel.onAmpCaptured(it) } }
                        }

                        // -- Pre-amp -------------------------------------------
                        is WizardStep.PreAmpQuestion -> {
                            WizardPrompt("Pre-Amp Pedals", "Do you have any pedals before the amp input?")
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Yes", Modifier.weight(1f)) { viewModel.answerHasPreAmp(true) }
                                WizardButton("No", Modifier.weight(1f), secondary = true) { viewModel.answerHasPreAmp(false) }
                            }
                        }
                        is WizardStep.PreAmpCapture -> {
                            WizardPrompt("Pre-Amp Pedal", "Take a photo of the next pedal in the pre-amp chain.")
                            WizardButton("Take Photo") { launchCamera { viewModel.onPreAmpPedalCaptured(it) } }
                        }
                        is WizardStep.PreAmpContinue -> {
                            WizardPrompt("Pre-Amp Pedal", "Pedal captured. Any more pre-amp pedals?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Add Another Pre-Amp Pedal") { viewModel.addAnotherPreAmp() }
                                WizardButton("Done — Go to FX Loop", secondary = true) { viewModel.finishPreAmp() }
                            }
                        }

                        // -- FX Loop entry -------------------------------------
                        is WizardStep.FxLoopEntry -> {
                            WizardPrompt("FX Loop — First Pedal", "Take a photo of the FIRST pedal in the FX loop.\n\nThis must be a Mono input.")
                            WizardButton("Take Photo") { launchCamera { viewModel.onFxLoopEntryCaptured(it) } }
                        }
                        is WizardStep.FxLoopMonoChoice -> {
                            WizardPrompt("Signal Flow", "How does the chain continue?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Add Mono Pedal") { launchCamera { viewModel.onFxMonoPedalCaptured(it) } }
                                WizardButton("Convert to Stereo") { launchCamera { viewModel.onFxStereoPedalCaptured(it) } }
                                WizardButton("Split into L/R") { viewModel.chooseSplit() }
                                WizardButton("Finish Session", secondary = true) { viewModel.finishWizard() }
                            }
                        }

                        // -- FX Stereo -----------------------------------------
                        is WizardStep.FxLoopStereo -> {
                            WizardPrompt("Stereo Path", "Single stereo path. What's next?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Continue in Stereo") { launchCamera { viewModel.onFxStereoPedalCaptured(it) } }
                                WizardButton("Split into L/R") { viewModel.chooseSplit() }
                                WizardButton("Finish Session", secondary = true) { viewModel.finishWizard() }
                            }
                        }

                        // -- Split ---------------------------------------------
                        is WizardStep.FxLoopSplitLeft -> {
                            WizardPrompt("LEFT Branch", "Take a photo of the next pedal on the LEFT branch.")
                            WizardButton("Take Photo (Left)") { launchCamera { viewModel.onFxSplitLeftCaptured(it) } }
                        }
                        is WizardStep.FxLoopSplitLeftChoice -> {
                            WizardPrompt("LEFT Branch", "Left pedal captured. Any more on the LEFT before the merge?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Add Another Left Pedal") { viewModel.addMoreLeft() }
                                WizardButton("No More Left Pedals Before Merge", secondary = true) { viewModel.doneWithLeft() }
                            }
                        }
                        is WizardStep.FxLoopSplitRight -> {
                            WizardPrompt("RIGHT Branch", "Take a photo of the next pedal on the RIGHT branch.")
                            WizardButton("Take Photo (Right)") { launchCamera { viewModel.onFxSplitRightCaptured(it) } }
                        }
                        is WizardStep.FxLoopSplitRightChoice -> {
                            WizardPrompt("RIGHT Branch", "Right pedal captured. Any more on the RIGHT before the merge?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Add Another Right Pedal") { viewModel.addMoreRight() }
                                WizardButton("No More Right Pedals Before Merge", secondary = true) { viewModel.doneWithRight() }
                            }
                        }

                        // -- Post-split / Merge --------------------------------
                        is WizardStep.FxLoopPostSplitChoice -> {
                            WizardPrompt("Both Branches Complete", "What happens next?")
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                WizardButton("Merge into Stereo Pedal") { viewModel.chooseMerge() }
                                WizardButton("End Here (To Amp Return)", secondary = true) { viewModel.finishWizard() }
                            }
                        }
                        is WizardStep.FxLoopMerge -> {
                            WizardPrompt("Merge Point", "Take a photo of the pedal where both signals merge back together.")
                            WizardButton("Take Photo (Merge Pedal)") { launchCamera { viewModel.onMergePedalCaptured(it) } }
                        }

                        is WizardStep.Finished -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun WizardPrompt(title: String, subtitle: String) {
    Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
    Spacer(modifier = Modifier.height(8.dp))
    Text(subtitle, style = MaterialTheme.typography.bodyLarge, color = Color.Gray, textAlign = TextAlign.Center)
    Spacer(modifier = Modifier.height(48.dp))
}

@Composable
fun WizardButton(text: String, modifier: Modifier = Modifier, secondary: Boolean = false, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(64.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (secondary) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
            contentColor = if (secondary) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

