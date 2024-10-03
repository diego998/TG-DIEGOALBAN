package com.univalle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.univalle.app.communication.UsbService
import com.univalle.app.data.database.DatabaseProvider
import com.univalle.app.experiment.ui.ExperimentScreen
import com.univalle.app.experiment.ui.ExperimentViewModel
import com.univalle.app.experiment.ui.ExperimentViewModelFactory
import com.univalle.app.network.ktor.server.startServer
import com.univalle.app.session.ui.SessionScreen
import com.univalle.app.session.ui.SessionViewModel
import com.univalle.app.session.ui.SessionViewModelFactory
import com.univalle.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    // ViewModel para los experimentos
    private val experimentViewModel by viewModels<ExperimentViewModel> {
        ExperimentViewModelFactory(DatabaseProvider.getDatabase(applicationContext).experimentDao())
    }

    // ViewModel para las sesiones
    private val sessionViewModel by viewModels<SessionViewModel> {
        SessionViewModelFactory(DatabaseProvider.getDatabase(applicationContext).sessionDao())
    }

    // Instancia del servicio USB
    private lateinit var usbService: UsbService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar el servicio USB y pasar el método de callback para actualizar el estado de conexión
        usbService = UsbService(this) { isConnected ->
            experimentViewModel.updateUsbConnectionStatus(isConnected)

            if (isConnected) {
                // Enviar un valor de 100 que representa el brillo o la velocidad en tiempo real
                usbService.writeDataInRealTime("100".toByteArray(), 5)  // Envía el valor cada 500 ms

            } else {
                // Detener la comunicación si la Raspberry Pi se desconecta
                usbService.stopRealTimeCommunication()
            }
        }


        setContent {
            AppTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Iniciar el servidor con la base de datos
                    startServer(appDatabase = DatabaseProvider.getDatabase(applicationContext))

                    // Observar el estado de la conexión a la Raspberry Pi Pico
                    val isConnected by experimentViewModel.isConnected.collectAsState()

                    NavHost(navController = navController, startDestination = Routes.PantallaExperimentos.route) {
                        // Pantalla de experimentos
                        composable(Routes.PantallaExperimentos.route) {
                            ExperimentScreen(
                                viewModel = experimentViewModel,
                                isConnected = isConnected,  // Pasar el estado de conexión
                                onListSessions = { experimentId ->
                                    navController.navigate(Routes.PantallaSesiones.createRoute(experimentId))
                                }
                            )
                        }

                        // Pantalla de sesiones para un experimento específico
                        composable(
                            route = Routes.PantallaSesiones.route,
                            arguments = listOf(navArgument("experimentId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val experimentId = backStackEntry.arguments?.getLong("experimentId")
                            SessionScreen(viewModel = sessionViewModel, experimentId = experimentId)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Opcional: verificar si hay dispositivos conectados cuando la actividad se reanuda
        //usbService.listConnectedDevices()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Desregistrar el receptor USB cuando la actividad se destruye
        usbService.unregisterReceiver()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            // Verificar dispositivos cuando la ventana recupera el foco
            //usbService.listConnectedDevices()
        }
    }
}
