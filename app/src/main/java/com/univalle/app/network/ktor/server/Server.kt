package com.univalle.app.network.ktor.server

import com.univalle.app.data.dao.ExperimentDao
import com.univalle.app.data.dao.MeasurementDao
import com.univalle.app.data.dao.SessionDao
import com.univalle.app.data.database.AppDatabase
import com.univalle.app.network.ktor.routes.experimentApi
import com.univalle.app.network.ktor.routes.measurementApi
import com.univalle.app.network.ktor.routes.sessionApi
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.defaultResource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.routing.routing

fun startServer(appDatabase: AppDatabase) {
    embeddedServer(CIO, port = 8080, module = { module(appDatabase) }).start(wait = false)
}

fun Application.module(appDatabase: AppDatabase) {
    val experimentDao: ExperimentDao = appDatabase.experimentDao()
    val sessionDao: SessionDao = appDatabase.sessionDao()
    val measurementDao: MeasurementDao = appDatabase.measurementDao()

    routing {
        static("/") {
            resources("web")  // Servir archivos desde la carpeta resources/web
            defaultResource("index.html", "web")
        }
        experimentApi(experimentDao)
        sessionApi(sessionDao)
        measurementApi(measurementDao)
    }
}
