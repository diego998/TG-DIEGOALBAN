package com.univalle.app.processing

//import com.univalle.app.communication.UsbConnectionManager
import com.univalle.app.data.models.SensorData
import com.univalle.app.sensors.MeasurementConfig
import com.univalle.app.sensors.MeasurementController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ProcessingController(
    private val measurementController: MeasurementController,
    //private val usbConnectionManager: UsbConnectionManager
) {
    private var processingJob: Job? = null

    /**
     * Inicia los procesos simultáneos de medición y envío de datos.
     * @param experimentId ID del experimento.
     * @param config Configuración de la medición.
     */
    fun startProcessing(experimentId: Long, config: MeasurementConfig) {
        // Asegurarse de que cualquier proceso anterior esté detenido
        stopProcessing()

        // Crear una corrutina para ejecutar los procesos simultáneamente
        processingJob = CoroutineScope(Dispatchers.IO).launch {
            // Iniciar la medición
            launch {
                startMeasurementProcess(experimentId, config)
            }

            // Iniciar el proceso de envío de datos
            launch {
                startSendingDataProcess()
            }
        }
    }

    /**
     * Detiene el procesamiento y cierra las conexiones.
     */
    fun stopProcessing() {
        processingJob?.cancel()  // Cancelar todos los procesos simultáneos
        measurementController.stopMeasurement()  // Detener la medición
    }

    /**
     * Inicia el proceso de medición y guarda los datos capturados.
     */
    private suspend fun startMeasurementProcess(experimentId: Long, config: MeasurementConfig) {
        // Iniciar el proceso de medición
        measurementController.startMeasurement(config, experimentId)

        // Recibir los datos de medición en tiempo real
        measurementController.capturedDataFlow.collect { data ->
            // Aquí puedes realizar operaciones con los datos capturados en tiempo real
            processData(data)
        }

        // Guardar los datos capturados en la base de datos cuando se detenga el proceso
        measurementController.saveCapturedData()
    }

    /**
     * Procesa los datos capturados para calcular el valor que se enviará por USB.
     */
    private fun processData(data: List<SensorData>) {
        // Realiza alguna operación en los datos capturados, por ejemplo:
        data.forEach { sensorData ->
            val angle = sensorData.angle
            // Procesar el ángulo para generar un valor de salida que se enviará por USB
            val valueToSend = calculateValueBasedOnAngle(angle)
            sendDataOverUsb(valueToSend)
        }
    }

    /**
     * Inicia el proceso de envío de datos de manera continua (simultánea al proceso de medición).
     */
    private suspend fun startSendingDataProcess() {
        // Simular un flujo continuo de envío de datos cada cierto tiempo
        while (isActive) {
            val dummyDataToSend = ByteArray(4) { 1 }  // Aquí iría el valor que se necesita enviar por USB
            //usbConnectionManager.sendData(dummyDataToSend)
            delay(500)  // Envía datos cada 500 ms (esto es solo un ejemplo)
        }
    }

    /**
     * Calcula el valor que se debe enviar por USB basado en el ángulo medido.
     */
    private fun calculateValueBasedOnAngle(angle: Float): ByteArray {
        // Realiza las operaciones basadas en el ángulo
        val value = angle * 2  // Ejemplo: multiplicar el ángulo por 2
        return value.toString().toByteArray()
    }

    /**
     * Envía datos al dispositivo USB.
     * @param data Valor a enviar por USB.
     */
    private fun sendDataOverUsb(data: ByteArray) {
        //usbConnectionManager.sendData(data)
    }
}
