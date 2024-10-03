package com.univalle.app.experiment.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univalle.app.data.models.ExperimentEntity


@Composable
fun ExperimentScreen(
    viewModel: ExperimentViewModel,
    onListSessions: (Long) -> Unit,
    isConnected: Boolean
) {
    val experiments by viewModel.experiments.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (experiments.isEmpty()) {
            viewModel.loadExperiments()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Indicador de estado de conexión
        ConnectionStatusIndicator(isConnected = isConnected)

        Spacer(modifier = Modifier.height(32.dp))

        // Sección de creación de nuevo experimento
        Text(
            text = "Crear Nuevo Experimento",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre del experimento") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Descripción (opcional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                if (name.isNotEmpty()) {
                    viewModel.addExperiment(name, description)
                    name = ""
                    description = ""
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Crear Experimento")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Lista de Experimentos",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (experiments.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                experiments.forEach { experiment ->
                    ExperimentItem(
                        experiment = experiment,
                        onCreateSession = { /* Lógica para crear una nueva sesión */ },
                        onListSessions = { onListSessions(experiment.id) },
                        onDeleteExperiment = { viewModel.deleteExperiment(experiment) }
                    )
                }
            }
        } else {
            Text(text = "No hay experimentos", style = MaterialTheme.typography.bodyLarge)
        }
    }
}


@Composable
fun ExperimentItem(
    experiment: ExperimentEntity,
    onCreateSession: () -> Unit,
    onListSessions: () -> Unit,
    onDeleteExperiment: () -> Unit
) {
    // Estado para mostrar el diálogo de confirmación
    var showDialog by remember { mutableStateOf(false) }

    // Si el usuario confirma la eliminación, ejecutamos la acción
    if (showDialog) {
        DeleteConfirmationDialog(
            onConfirm = {
                onDeleteExperiment()
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mostrar el nombre del experimento
            Text(
                text = experiment.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )

            // Mostrar la descripción solo si está disponible
            experiment.description?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fila de botones
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Botón para crear una nueva sesión
                Button(onClick = onCreateSession) {
                    Text("Crear Sesión")
                }

                // Botón para listar sesiones
                Button(onClick = onListSessions) {
                    Text("Listar Sesiones")
                }

                // Botón para eliminar experimento con confirmación
                Button(
                    onClick = { showDialog = true },
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirmar eliminación") },
        text = { Text("¿Estás seguro de que deseas eliminar este experimento? Esta acción no se puede deshacer.") },
        confirmButton = {
            Button(
                onClick = { onConfirm() },
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
            ) {
                Text("Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("Cancelar")
            }
        }
    )
}


@Composable
fun ConnectionStatusIndicator(isConnected: Boolean) {
    val backgroundColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336)
    val connectionText = if (isConnected) "Motor conectado" else "Motor desconectado"
    val textColor = Color.White

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = connectionText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor
                ),
                fontSize = 18.sp
            )
        }
    }
}
