package com.univalle.app.measurement.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univalle.app.data.models.MeasurementEntity
import com.univalle.app.measurement.ui.MeasurementViewModel

@Composable
fun MeasurementScreen(viewModel: MeasurementViewModel, sessionId: Long) {
    val measurements by viewModel.measurements.collectAsState()

    // Cargar mediciones para la sesión específica
    LaunchedEffect(Unit) {
        viewModel.loadMeasurementsForSession(sessionId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Mediciones", style = MaterialTheme.typography.titleLarge)

        if (measurements.isNotEmpty()) {
            measurements.forEach { measurement ->
                MeasurementItem(measurement = measurement)
            }
        } else {
            Text(text = "No hay mediciones disponibles.")
        }
    }
}

@Composable
fun MeasurementItem(measurement: MeasurementEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Tiempo discreto: ${measurement.discreteTimes.joinToString()}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Ángulos: ${measurement.angles.joinToString()}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
