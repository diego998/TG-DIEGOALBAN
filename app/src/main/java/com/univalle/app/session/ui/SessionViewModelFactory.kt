package com.univalle.app.session.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.univalle.app.data.dao.SessionDao

class SessionViewModelFactory(private val sessionDao: SessionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SessionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SessionViewModel(sessionDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
