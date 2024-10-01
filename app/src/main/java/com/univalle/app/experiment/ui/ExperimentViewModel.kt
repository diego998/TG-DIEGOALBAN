package com.univalle.app.experiment.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.app.data.dao.ExperimentDao
import com.univalle.app.data.models.ExperimentEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ExperimentViewModel(private val experimentDao: ExperimentDao) : ViewModel() {

    private val _experiments = MutableStateFlow<List<ExperimentEntity>>(emptyList())
    val experiments: StateFlow<List<ExperimentEntity>> get() = _experiments

    // Cargar todos los experimentos
    fun loadExperiments() {
        viewModelScope.launch {
            try {
                _experiments.value = experimentDao.getAllExperiments()
            } catch (e: Exception) {
                // Manejar errores de carga si es necesario
            }
        }
    }

    // Agregar un nuevo experimento
    fun addExperiment(name: String, description: String) {
        viewModelScope.launch {
            try {
                val newExperiment = ExperimentEntity(name = name, description = description)
                experimentDao.insertExperiment(newExperiment)
                _experiments.value = _experiments.value + newExperiment
            } catch (e: Exception) {
                // Manejar errores de inserción si es necesario
            }
        }
    }

    // Eliminar un experimento existente
    fun deleteExperiment(experiment: ExperimentEntity) {
        viewModelScope.launch {
            try {
                experimentDao.deleteExperiment(experiment)  // Asegúrate de que esta función existe en el DAO
                _experiments.value = _experiments.value.filter { it.id != experiment.id }
            } catch (e: Exception) {
                // Manejar errores de eliminación si es necesario
            }
        }
    }
}
