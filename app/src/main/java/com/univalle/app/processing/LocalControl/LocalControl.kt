package com.univalle.app.processing.LocalControl

class LocalControl(
    private val Kp: Float = 0f,  // Constante Proporcional
    private val Ki: Float = 0f,  // Constante Integral
    private val Kd: Float = 0f   // Constante Derivativa
) {
    private var setPoint: Float = 0f  // El valor objetivo
    private var previousError: Float = 0f  // El error en el instante anterior
    private var integral: Float = 0f  // La suma acumulada del error para el término integral
    private var previousTime: Long = System.currentTimeMillis()  // Tiempo anterior para calcular el deltaTime

    // Parámetro para habilitar o deshabilitar términos
    private var useP: Boolean = true
    private var useI: Boolean = true
    private var useD: Boolean = true

    /**
     * Establecer el SetPoint (punto de referencia o valor objetivo)
     */
    fun setSetPoint(newSetPoint: Float) {
        setPoint = newSetPoint
    }

    /**
     * Habilitar o deshabilitar los términos de control P, I, D
     */
    fun configureTerms(p: Boolean, i: Boolean, d: Boolean) {
        useP = p
        useI = i
        useD = d
    }

    /**
     * Método para calcular la salida de control basándose en el valor medido actual
     * @param measuredValue El valor medido en tiempo real
     * @return La señal de control resultante, junto con el error y el tiempo
     */
    fun calculateControlSignal(measuredValue: Float?): Triple<Float, Float, Long> {
        val currentTime = System.currentTimeMillis()
        val deltaTime = (currentTime - previousTime) / 1000f  // Convertir a segundos


        // Si no existe measuredValue, retorna el setPoint como la señal de control
        if (measuredValue == null) {
            // Senal de control setpoint
            return Triple(setPoint, 0f, deltaTime.toLong())
        }

        // Calcular el error actual (SetPoint - valor medido)
        val error = setPoint - measuredValue

        // Si P, I y D están deshabilitados, la señal de control es el error
        if (!useP && !useI && !useD) {
            // Senal de control error
            return Triple(error, error, deltaTime.toLong())
        }

        // Parte proporcional
        val proportional = if (useP) Kp * error else 0f

        // Parte integral (acumulamos el error en el tiempo)
        if (useI) integral += error * deltaTime
        val integralTerm = if (useI) Ki * integral else 0f

        // Parte derivativa (cambio del error)
        val derivative = if (deltaTime > 0) (error - previousError) / deltaTime else 0f
        val derivativeTerm = if (useD) Kd * derivative else 0f

        // Actualizar el error y el tiempo previos para el próximo cálculo
        previousError = error
        previousTime = currentTime

        // Suma de las tres componentes para obtener la señal de control
        val controlSignal = proportional + integralTerm + derivativeTerm

        return Triple(controlSignal, error, deltaTime.toLong())
    }


    /**
     * Método para restablecer el controlador (reiniciar la integral y el error anterior)
     */
    fun reset() {
        integral = 0f
        previousError = 0f
        previousTime = System.currentTimeMillis()
    }
}
