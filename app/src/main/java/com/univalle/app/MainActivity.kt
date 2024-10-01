package com.univalle.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

    private val experimentViewModel by viewModels<ExperimentViewModel> {
        ExperimentViewModelFactory(DatabaseProvider.getDatabase(applicationContext).experimentDao())
    }

    private val sessionViewModel by viewModels<SessionViewModel> {
        SessionViewModelFactory(DatabaseProvider.getDatabase(applicationContext).sessionDao())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val navController = rememberNavController()
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    startServer(appDatabase = DatabaseProvider.getDatabase(applicationContext))
                    NavHost(navController = navController, startDestination = Routes.PantallaExperimentos.route) {
                        // Pantalla de experimentos
                        composable(Routes.PantallaExperimentos.route) {
                            ExperimentScreen(
                                viewModel = experimentViewModel,
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
}
