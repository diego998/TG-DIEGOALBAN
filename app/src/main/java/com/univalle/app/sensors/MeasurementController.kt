package com.univalle.app.sensors

import com.univalle.app.data.dao.MeasurementDao
import com.univalle.app.data.dao.SessionDao
import com.univalle.app.data.models.MeasurementEntity
import com.univalle.app.data.models.SensorData
import com.univalle.app.data.models.SessionEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull

class MeasurementController(
    private val sensorManagerWrapper: SensorManagerWrapper,
    private val sessionDao: SessionDao,
    private val measurementDao: MeasurementDao
) {
    private var measurementJob: Job? = null
    private var measurementConfig: MeasurementConfig? = null
    private var zeroOffset: Float = 0f  // Para almacenar el valor de cero ajustado

    // StateFlow para emitir la lista de datos capturados en tiempo real
    private val _capturedDataFlow = MutableStateFlow<List<SensorData>>(emptyList())
    val capturedDataFlow: StateFlow<List<SensorData>> = _capturedDataFlow

    // Lista para almacenar temporalmente los datos capturados
    private val capturedData = mutableListOf<SensorData>()
    private var sessionId: Long? = null  // ID de la sesión actual

    /**
     * Inicia la medición con la configuración especificada.
     * @param config Configuración de la medición.
     * @param experimentId ID del experimento al que pertenece la sesión.
     */
    suspend fun startMeasurement(config: MeasurementConfig, experimentId: Long) {
        measurementConfig = config

        // Limpiar datos capturados de mediciones anteriores
        capturedData.clear()

        // Crear una nueva sesión en la base de datos
        sessionId = createNewSession(experimentId)

        // Configurar el SensorManagerWrapper para la medición
        sensorManagerWrapper.startMeasurement()

        // Iniciar la captura de datos y ajustar el cero inicial antes de la medición
        measurementJob = CoroutineScope(Dispatchers.IO).launch {
            // Ajuste de cero inicial
            adjustZeroOffset()

            // Si la medición es definida, configurar para que se detenga automáticamente después de la duración especificada
            val duration = config.duration
            if (config.measurementType == MeasurementType.DEFINED && duration != null) {
                delay(duration)
                stopMeasurement()
            }

            // Capturar los datos en tiempo real
            sensorManagerWrapper.realTimeDataFlow.collect { (value, timestamp, samplingTime) ->
                // Aplicar el offset ajustado y personalizado
                val adjustedValue = value - zeroOffset - (config.offset)
                val discreteTime = capturedData.size.toLong()

                // Agregar el dato capturado a la lista
                val data = SensorData(discreteTime, adjustedValue, samplingTime, timestamp)
                capturedData.add(data)

                // Emitir la lista actualizada en el StateFlow para visualización en tiempo real
                _capturedDataFlow.value = capturedData.toList()
            }
        }
    }

    /**
     * Detiene la medición en curso.
     */
    fun stopMeasurement() {
        measurementJob?.cancel()  // Cancelar la medición
        sensorManagerWrapper.stopMeasurement()  // Detener la captura de datos del sensor

        // Actualizar el endTime de la sesión en la base de datos
        sessionId?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val session = sessionDao.getSessionById(it)
                session?.let { s ->
                    val updatedSession = s.copy(endTime = System.currentTimeMillis())
                    sessionDao.updateSession(updatedSession)
                }
            }
        }
    }


    /**
     * Ajusta el valor de cero antes de comenzar la medición.
     */
    private suspend fun adjustZeroOffset() {
        val zeroAdjustmentDuration = 2000L // Ajustar cero por 2 segundos
        var sum = 0f
        var count = 0

        // Capturar valores durante el tiempo de ajuste de cero
        val job = CoroutineScope(Dispatchers.IO).launch {
            sensorManagerWrapper.realTimeDataFlow.collect { (value, _, _) ->
                sum += value
                count++
            }
        }

        // Esperar a que se complete el tiempo de ajuste de cero
        delay(zeroAdjustmentDuration)
        job.cancel()

        // Calcular el promedio y ajustar el cero
        zeroOffset = if (count > 0) sum / count else 0f
    }

    /**
     * Guarda los datos capturados en la base de datos.
     */
    suspend fun saveCapturedData() {
        sessionId?.let { sessionId ->
            if (capturedData.isNotEmpty()) {
                val discreteTimes = capturedData.map { it.discreteTime }
                val angles = capturedData.map { it.angle }
                val samplingTimes = capturedData.map { it.samplingTime }
                val timestamps = capturedData.map { it.timestamp }

                val measurementEntity = MeasurementEntity(
                    sessionId = sessionId,
                    discreteTimes = discreteTimes,
                    angles = angles,
                    samplingTimes = samplingTimes,
                    timestamps = timestamps
                )
                measurementDao.insertMeasurement(measurementEntity)
            }
        }
    }

    /**
     * Crea una nueva sesión en la base de datos.
     * @param experimentId ID del experimento al que pertenece la sesión.
     * @return ID de la nueva sesión creada.
     */
    private suspend fun createNewSession(experimentId: Long): Long {
        val session = SessionEntity(
            experimentId = experimentId,
            startTime = System.currentTimeMillis()
        )
        return sessionDao.insertSession(session)
    }

    /**
     * Devuelve los últimos datos de medición capturados.
     * @return Un objeto Triple que contiene el valor medido, el timestamp y el tiempo de muestreo.
     */
    suspend fun getLatestMeasurement(): Triple<Float, Long, Long>? {
        return sensorManagerWrapper.realTimeDataFlow.firstOrNull()
    }

    /**
     * Devuelve la lista completa de datos capturados en esta sesión.
     * @return Lista de SensorData capturados.
     */
    suspend fun getCapturedData(): List<SensorData> {
        return capturedData.toList()
    }
}
