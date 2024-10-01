package com.univalle.app.network.ktor.routes

import com.univalle.app.data.dao.ExperimentDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.experimentApi(experimentDao: ExperimentDao) {
    route("/experiments") {
        get {
            val experiments = experimentDao.getAllExperiments()
            call.respond(experiments)
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            val experiment = id?.let { experimentDao.getExperimentById(it) }
            if (experiment != null) {
                call.respond(experiment)
            } else {
                call.respondText("Experiment not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
