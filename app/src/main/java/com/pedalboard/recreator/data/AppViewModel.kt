package com.pedalboard.recreator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).dao()

    val sessions = dao.getAllSessions()

    private val _currentSession = MutableStateFlow<SessionEntity?>(null)
    val currentSession: StateFlow<SessionEntity?> = _currentSession.asStateFlow()

    private val _currentPedals = MutableStateFlow<List<PedalEntity>>(emptyList())
    val currentPedals: StateFlow<List<PedalEntity>> = _currentPedals.asStateFlow()

    private val _currentConnections = MutableStateFlow<List<ConnectionEntity>>(emptyList())
    val currentConnections: StateFlow<List<ConnectionEntity>> = _currentConnections.asStateFlow()

    fun loadSessionData(sessionId: String) {
        viewModelScope.launch {
            sessions.collect { all -> _currentSession.value = all.find { it.id == sessionId } }
        }
        viewModelScope.launch {
            dao.getPedalsForSession(sessionId).collect { _currentPedals.value = it }
        }
        viewModelScope.launch {
            dao.getConnectionsForSession(sessionId).collect { _currentConnections.value = it }
        }
    }

    fun addSession(session: SessionEntity) {
        viewModelScope.launch { dao.insertSession(session) }
    }

    fun addPedal(pedal: PedalEntity) {
        viewModelScope.launch { dao.insertPedal(pedal) }
    }

    fun deleteChain(sessionId: String) {
        viewModelScope.launch {
            dao.deletePedalsForSession(sessionId)
            dao.deleteConnectionsForSession(sessionId)
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            dao.deletePedalsForSession(sessionId)
            dao.deleteConnectionsForSession(sessionId)
            dao.deleteSession(sessionId)
        }
    }

    fun cloneSession(original: SessionEntity) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            dao.insertSession(original.copy(
                id = newId,
                songTitle = original.songTitle + " (Copy)",
                date = System.currentTimeMillis()
            ))
            val idMap = mutableMapOf<String, String>()
            dao.getPedalsForSession(original.id).first().forEach { pedal ->
                val newPedalId = UUID.randomUUID().toString()
                idMap[pedal.id] = newPedalId
                dao.insertPedal(pedal.copy(id = newPedalId, sessionId = newId))
            }
            dao.getConnectionsForSession(original.id).first().forEach { conn ->
                dao.insertConnection(conn.copy(
                    id = UUID.randomUUID().toString(),
                    sessionId = newId,
                    fromPedalId = idMap[conn.fromPedalId] ?: conn.fromPedalId,
                    toPedalId   = idMap[conn.toPedalId]   ?: conn.toPedalId
                ))
            }
        }
    }

    fun saveSessionFullBoardImagePath(sessionId: String, imagePath: String) {
        viewModelScope.launch {
            dao.insertSession(_currentSession.value?.copy(fullBoardImagePath = imagePath) ?: return@launch)
        }
    }
}
