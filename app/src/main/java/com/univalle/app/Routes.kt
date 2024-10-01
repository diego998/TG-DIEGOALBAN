package com.univalle.app

sealed class Routes(val route: String) {
    object PantallaExperimentos : Routes("experiment_screen")
    object PantallaSesiones : Routes("session_screen/{experimentId}") {
        fun createRoute(experimentId: Long) = "session_screen/$experimentId"
    }
}
