package com.univalle.app.sensors

// MeasurementType Enum para definir si la medición es definida o indefinida
enum class MeasurementType {
    DEFINED,    // Medición con un tiempo de duración específico
    UNDEFINED   // Medición que se detiene manualmente
}

// Clase MeasurementConfig para almacenar las configuraciones de la medición
data class MeasurementConfig(
    var offset: Float = 0f,           // Offset para ajustar el valor medido
    var measurementType: MeasurementType = MeasurementType.UNDEFINED,  // Tipo de medición
    var duration: Long? = null        // Duración de la medición en milisegundos, si es definida
)
