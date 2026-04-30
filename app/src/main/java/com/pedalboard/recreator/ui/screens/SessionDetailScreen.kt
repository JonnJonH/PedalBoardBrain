package com.pedalboard.recreator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedalboard.recreator.data.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    sessionId: String,
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCamera: (String) -> Unit,
    onNavigateToPedalDetail: (String) -> Unit,
    onNavigateToRecreation: () -> Unit
) {
    val sessions by viewModel.sessions.collectAsStateWithLifecycle(initialValue = emptyList())
    val session = sessions.find { it.id == sessionId }
    val pedals by viewModel.currentPedals.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessionId) {
        viewModel.loadSessionData(sessionId)
    }

    // Confirm chain deletion dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Signal Chain?", fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete all pedals and connections in this chain. You can then run the wizard again to build a new one.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteChain(sessionId)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(session?.songTitle ?: "Session Detail", fontWeight = FontWeight.Bold)
                        Text(session?.part ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                // If a chain exists, show Delete in the top bar instead of a FAB
                actions = {
                    if (pedals.isNotEmpty()) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Chain", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        // Only show the Recreation FAB when a chain exists — no Add button
        floatingActionButton = {
            if (pedals.isNotEmpty()) {
                FloatingActionButton(
                    onClick = onNavigateToRecreation,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Enter Recreation Mode")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (pedals.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No signal chain yet.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { onNavigateToCamera("wizard") }) {
                            Text("Start Setup Wizard")
                        }
                    }
                }
            } else {
                SignalChainDiagram(
                    pedals = pedals,
                    onPedalClick = onNavigateToPedalDetail,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
