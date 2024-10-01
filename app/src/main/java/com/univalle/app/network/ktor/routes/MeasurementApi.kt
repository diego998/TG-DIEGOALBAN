package com.univalle.app.network.ktor.routes

import com.univalle.app.data.dao.MeasurementDao
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.measurementApi(measurementDao: MeasurementDao) {
    route("/measurements") {
        // Obtener todas las mediciones
        get {
            val measurements = measurementDao.getAllMeasurements()
            call.respond(measurements)
        }

        // Obtener una medición específica por ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            val measurement = id?.let { measurementDao.getMeasurementById(it) }
            if (measurement != null) {
                call.respond(measurement)
            } else {
                call.respondText("Measurement not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
