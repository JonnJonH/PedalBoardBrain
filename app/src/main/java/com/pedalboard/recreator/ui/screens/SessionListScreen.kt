package com.pedalboard.recreator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedalboard.recreator.data.AppViewModel
import com.pedalboard.recreator.data.SessionEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SessionListScreen(
    viewModel: AppViewModel,
    onNavigateToDetail: (String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var sessionForAction by remember { mutableStateOf<SessionEntity?>(null) }
    val sessions by viewModel.sessions.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Sessions", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Session")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 80.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(sessions, key = { it.id }) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onNavigateToDetail(session.id) },
                            onLongClick = { sessionForAction = session }
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = session.songTitle,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        val subtitle = listOf(session.section, session.part)
                            .filter { it.isNotBlank() }.joinToString(" / ")
                        if (subtitle.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (session.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = session.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }

        // Long-press action sheet
        sessionForAction?.let { session ->
            AlertDialog(
                onDismissRequest = { sessionForAction = null },
                containerColor = MaterialTheme.colorScheme.surface,
                title = { Text(session.songTitle, fontWeight = FontWeight.Bold) },
                text = { Text("What would you like to do with this session?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.cloneSession(session)
                            sessionForAction = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text("Clone") }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { sessionForAction = null }) { Text("Cancel") }
                        TextButton(
                            onClick = {
                                viewModel.deleteSession(session.id)
                                sessionForAction = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }

        if (showCreateDialog) {
            CreateSessionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { songTitle, section, part, notes ->
                    val newSession = SessionEntity(
                        id = UUID.randomUUID().toString(),
                        songTitle = songTitle,
                        section = section,
                        part = part,
                        date = System.currentTimeMillis(),
                        notes = notes,
                        fullBoardImagePath = null
                    )
                    viewModel.addSession(newSession)
                    showCreateDialog = false
                    onNavigateToDetail(newSession.id)
                }
            )
        }
    }
}

@Composable
fun CreateSessionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var songTitle by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var part by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("New Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = songTitle,
                    onValueChange = { songTitle = it },
                    label = { Text("Song Title *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section (e.g. Chorus)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = part,
                    onValueChange = { part = it },
                    label = { Text("Part (e.g. Lead)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (songTitle.isNotBlank()) onCreate(songTitle, section, part, notes) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Create", color = MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurface) }
        }
    )
}
