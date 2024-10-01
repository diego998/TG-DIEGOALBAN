package com.univalle.app.measurement.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.univalle.app.data.dao.MeasurementDao
import com.univalle.app.data.models.MeasurementEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MeasurementViewModel(private val measurementDao: MeasurementDao) : ViewModel() {

    private val _measurements = MutableStateFlow<List<MeasurementEntity>>(emptyList())
    val measurements: StateFlow<List<MeasurementEntity>> get() = _measurements

    // Cargar mediciones para una sesión específica
    fun loadMeasurementsForSession(sessionId: Long) {
        viewModelScope.launch {
            _measurements.value = measurementDao.getMeasurementsForSession(sessionId)
        }
    }
}
