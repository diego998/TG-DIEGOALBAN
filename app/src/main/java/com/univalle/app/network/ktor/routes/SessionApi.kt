package com.univalle.app.network.ktor.routes

import com.univalle.app.data.dao.SessionDao

import io.ktor.http.HttpStatusCode

import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.sessionApi(sessionDao: SessionDao) {
    route("/sessions") {
        // Obtener todas las sesiones
        get {
            val sessions = sessionDao.getAllSessions()
            call.respond(sessions)
        }

        // Obtener una sesión específica por ID
        get("/{id}") {
            val id = call.parameters["id"]?.toLongOrNull()
            val session = id?.let { sessionDao.getSessionById(it) }
            if (session != null) {
                call.respond(session)
            } else {
                call.respondText("Session not found", status = HttpStatusCode.NotFound)
            }
        }
    }
}
