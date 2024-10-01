package com.univalle.app.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class SensorManagerWrapper(private val context: Context) : SensorEventListener {

    private val _realTimeDataFlow = MutableStateFlow(Triple(0f, 0L, 0L))
    val realTimeDataFlow: StateFlow<Triple<Float, Long, Long>> get() = _realTimeDataFlow

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var magnetometer: Sensor
    private lateinit var gyroscope: Sensor

    private var gyro = FloatArray(3)
    private var gyroMatrix = FloatArray(9)
    private var gyroOrientation = FloatArray(3)
    private var magnet = FloatArray(3)
    private var accel = FloatArray(3)
    private var accMagOrientation = FloatArray(3)
    private var fusedOrientation = FloatArray(3)
    private var rotationMatrix = FloatArray(9)

    private var timestamp: Long = 0L
    private var previousTimestamp: Long = 0L
    private var samplingTime: Long = 0L
    private var discreteTime: Long = 0L
    private var initState = true
    private val valueDrift = 0.001f

    init {
        initSensors()
    }

    private fun initSensors() {
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("Accelerometer sensor not available")
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            ?: throw IllegalStateException("Magnetometer sensor not available")
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
            ?: throw IllegalStateException("Gyroscope sensor not available")

        gyroMatrix[0] = 1.0f
        gyroMatrix[4] = 1.0f
        gyroMatrix[8] = 1.0f

        initListeners()
    }

    private fun initListeners() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onSensorChanged(event: SensorEvent) {
        timestamp = event.timestamp / 1_000_000 // Convertir nanosegundos a milisegundos
        discreteTime++

        // Calcular tiempo de muestreo (diferencia entre timestamps)
        if (previousTimestamp != 0L) {
            samplingTime = timestamp - previousTimestamp
        }
        previousTimestamp = timestamp

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accel, 0, 3)
                calculateAccMagOrientation()
            }
            Sensor.TYPE_GYROSCOPE -> {
                gyroFunction(event)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnet, 0, 3)
            }
        }

        val pitchValue = calculatePitchValue()

        // Emitir el valor de pitch junto con el timestamp y el tiempo de muestreo
        _realTimeDataFlow.value = Triple(pitchValue, timestamp, samplingTime)
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Manejar cambios en la precisión si es necesario
    }

    private fun calculateAccMagOrientation() {
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation)
        }
    }

    private fun gyroFunction(event: SensorEvent) {
        if (accMagOrientation.isEmpty()) return

        if (initState) {
            gyroMatrix = matrixMultiplication(gyroMatrix, getRotationMatrixFromOrientation(accMagOrientation))
            initState = false
        }

        val deltaVector = FloatArray(4)
        if (previousTimestamp != 0L) {
            val dT = (event.timestamp - previousTimestamp) * NS2S
            System.arraycopy(event.values, 0, gyro, 0, 3)
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f)
        }

        val deltaMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector)

        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix)

        SensorManager.getOrientation(gyroMatrix, gyroOrientation)
    }

    private fun getRotationVectorFromGyro(gyroValues: FloatArray, deltaRotationVector: FloatArray, timeFactor: Float) {
        val normValues = FloatArray(3)
        val omegaMagnitude = kotlin.math.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1] * gyroValues[1] + gyroValues[2] * gyroValues[2])

        if (omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude
            normValues[1] = gyroValues[1] / omegaMagnitude
            normValues[2] = gyroValues[2] / omegaMagnitude
        }

        val thetaOverTwo = omegaMagnitude * timeFactor
        val sinThetaOverTwo = sin(thetaOverTwo)
        val cosThetaOverTwo = cos(thetaOverTwo)
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0]
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1]
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2]
        deltaRotationVector[3] = cosThetaOverTwo
    }

    private fun getRotationMatrixFromOrientation(o: FloatArray): FloatArray {
        val xM = FloatArray(9)
        val yM = FloatArray(9)
        val zM = FloatArray(9)

        val sinX = sin(o[1].toDouble()).toFloat()
        val cosX = cos(o[1].toDouble()).toFloat()
        val sinY = sin(o[2].toDouble()).toFloat()
        val cosY = cos(o[2].toDouble()).toFloat()
        val sinZ = sin(o[0].toDouble()).toFloat()
        val cosZ = cos(o[0].toDouble()).toFloat()

        xM[0] = 1.0f
        xM[4] = cosX
        xM[5] = sinX
        xM[7] = -sinX
        xM[8] = cosX

        yM[0] = cosY
        yM[2] = sinY
        yM[4] = 1.0f
        yM[6] = -sinY
        yM[8] = cosY

        zM[0] = cosZ
        zM[1] = sinZ
        zM[3] = -sinZ
        zM[4] = cosZ
        zM[8] = 1.0f

        // Rotación en el orden y, x, z (roll, pitch, azimuth)
        var resultMatrix = matrixMultiplication(xM, yM)
        resultMatrix = matrixMultiplication(zM, resultMatrix)
        return resultMatrix
    }

    private fun matrixMultiplication(A: FloatArray, B: FloatArray): FloatArray {
        val result = FloatArray(9)

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6]
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7]
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8]

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6]
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7]
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8]

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6]
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7]
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8]

        return result
    }

    private fun calculatePitchValue(): Float {
        var pitch = fusedOrientation[1]

        if (abs(pitch) < valueDrift) {
            pitch = 0f
        }

        return Math.toDegrees(pitch.toDouble()).toFloat() + 90f
    }

    fun startMeasurement() {
        onResume()
    }

    fun stopMeasurement() {
        onPause()
    }

    private fun onResume() {
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this,
            magnetometer,
            SensorManager.SENSOR_DELAY_FASTEST
        )
        sensorManager.registerListener(
            this,
            gyroscope,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    private fun onPause() {
        sensorManager.unregisterListener(this)
    }
    companion object {
        private const val NS2S = 1.0f / 1000000000.0f
        private const val EPSILON = 0.000000001f
    }
}
