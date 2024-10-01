package com.univalle.app.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.app.data.dao.SessionDao
import com.univalle.app.data.models.SessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SessionViewModel(private val sessionDao: SessionDao) : ViewModel() {

    private val _sessions = MutableStateFlow<List<SessionEntity>>(emptyList())
    val sessions: StateFlow<List<SessionEntity>> get() = _sessions

    fun loadSessionsForExperiment(experimentId: Long) {
        viewModelScope.launch {
            _sessions.value = sessionDao.getSessionsForExperiment(experimentId)
        }
    }

    fun loadAllSessions() {
        viewModelScope.launch {
            _sessions.value = sessionDao.getAllSessions()
        }
    }
}
