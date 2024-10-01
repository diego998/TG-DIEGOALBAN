package com.univalle.app.session.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.univalle.app.data.models.SessionEntity
import com.univalle.app.session.ui.SessionViewModel

@Composable
fun SessionScreen(viewModel: SessionViewModel, experimentId: Long?) {
    val sessions by viewModel.sessions.collectAsState()

    // Cargar sesiones para el experimento específico o todas las sesiones
    LaunchedEffect(Unit) {
        if (experimentId != null) {
            viewModel.loadSessionsForExperiment(experimentId)
        } else {
            viewModel.loadAllSessions()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Sesiones", style = MaterialTheme.typography.titleLarge)

        if (sessions.isNotEmpty()) {
            sessions.forEach { session ->
                SessionItem(session = session)
            }
        } else {
            Text(text = "No hay sesiones disponibles.")
        }
    }
}

@Composable
fun SessionItem(session: SessionEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Sesión ID: ${session.id}", style = MaterialTheme.typography.bodyLarge)
            Text(text = "Fecha de inicio: ${session.startTime}", style = MaterialTheme.typography.bodyMedium)
            session.endTime?.let {
                Text(text = "Fecha de finalización: $it", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
