package com.pedalboard.recreator.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val existingSongs by viewModel.songTitles.collectAsStateWithLifecycle(initialValue = emptyList())

    val groupedSessions = remember(sessions) {
        sessions.groupBy { it.songTitle }.toSortedMap()
    }

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
            groupedSessions.forEach { (songTitle, songSessions) ->
                // Group Header (Optional but good for clarity)
                item(key = "header_$songTitle") {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = songTitle.uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                        if (songSessions.size > 1) {
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = "${songSessions.size} SESSIONS",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                items(songSessions, key = { it.id }) { session ->
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    val title = if (session.section.isNotBlank()) session.section else "Untitled Section"
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (session.part.isNotBlank()) {
                                        Text(
                                            text = session.part,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                
                                if (songSessions.size > 1) {
                                    Icon(
                                        Icons.Default.Layers,
                                        contentDescription = "Part of a song group",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (session.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = session.notes,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    maxLines = 2
                                )
                            }
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
                existingSongs = existingSongs,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSessionDialog(
    existingSongs: List<String>,
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String) -> Unit
) {
    var songTitle by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var part by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        title = { Text("New Session", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Song Title with Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = songTitle,
                        onValueChange = { songTitle = it },
                        label = { Text("Song Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    
                    if (existingSongs.isNotEmpty()) {
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            existingSongs.forEach { song ->
                                DropdownMenuItem(
                                    text = { Text(song) },
                                    onClick = {
                                        songTitle = song
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = section,
                    onValueChange = { section = it },
                    label = { Text("Section (e.g. Chorus)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = part,
                    onValueChange = { part = it },
                    label = { Text("Part (e.g. Lead)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
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