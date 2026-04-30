package com.pedalboard.recreator.ui.screens

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.pedalboard.recreator.data.AppViewModel
import androidx.compose.ui.platform.LocalContext
import android.app.Application

@Composable
fun AppNavGraph(navController: NavHostController, viewModel: AppViewModel) {
    NavHost(navController = navController, startDestination = "session_list") {
        composable("session_list") {
            SessionListScreen(
                viewModel = viewModel,
                onNavigateToDetail = { id -> navController.navigate("session_detail/$id") }
            )
        }
        composable("session_detail/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionDetailScreen(
                sessionId = sessionId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCamera = { mode -> 
                    if (mode == "wizard") {
                        navController.navigate("setup_wizard/$sessionId")
                    } else {
                        navController.navigate("camera/$sessionId/$mode")
                    }
                },
                onNavigateToPedalDetail = { pedalId -> navController.navigate("pedal_detail/$pedalId") },
                onNavigateToRecreation = { navController.navigate("recreation/$sessionId") }
            )
        }
        composable("setup_wizard/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val context = LocalContext.current
            val wizardViewModel: SetupWizardViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        return SetupWizardViewModel((context.applicationContext as Application), sessionId) as T
                    }
                }
            )
            SetupWizardScreen(
                sessionId = sessionId,
                viewModel = wizardViewModel,
                onComplete = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("camera/{sessionId}/{mode}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "board"
            CameraScreen(
                sessionId = sessionId,
                mode = mode,
                viewModel = viewModel,
                onPhotoCaptured = { pedalId ->
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }
        composable("pedal_detail/{pedalId}") { backStackEntry ->
            val pedalId = backStackEntry.arguments?.getString("pedalId") ?: ""
            PedalDetailScreen(
                pedalId = pedalId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("recreation/{sessionId}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            RecreationScreen(
                sessionId = sessionId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

