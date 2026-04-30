package com.pedalboard.recreator.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            sessions.collect { all ->
                _currentSession.value = all.find { it.id == sessionId }
            }
        }
        viewModelScope.launch {
            dao.getPedalsForSession(sessionId).collect { pedals ->
                _currentPedals.value = pedals
            }
        }
        viewModelScope.launch {
            dao.getConnectionsForSession(sessionId).collect { connections ->
                _currentConnections.value = connections
            }
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

    fun saveSessionFullBoardImagePath(sessionId: String, imagePath: String) {
        viewModelScope.launch {
            dao.insertSession(_currentSession.value?.copy(fullBoardImagePath = imagePath) ?: return@launch)
        }
    }
}
