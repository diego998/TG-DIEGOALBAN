package com.univalle.app.experiment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.univalle.app.data.dao.ExperimentDao

class ExperimentViewModelFactory(private val experimentDao: ExperimentDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExperimentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExperimentViewModel(experimentDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
