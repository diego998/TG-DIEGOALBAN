package com.univalle.app.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.univalle.app.data.DataConverters


@Entity(
    tableName = "measurements",
    foreignKeys = [ForeignKey(
        entity = SessionEntity::class,
        parentColumns = ["id"],
        childColumns = ["sessionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["sessionId"])]
)
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,              // Referencia a la sesión a la que pertenece la medición
    @TypeConverters(DataConverters::class) val discreteTimes: List<Long>,
    @TypeConverters(DataConverters::class) val angles: List<Float>,
    @TypeConverters(DataConverters::class) val samplingTimes: List<Long>,
    @TypeConverters(DataConverters::class) val timestamps: List<Long>
)


data class SensorData(
    val discreteTime: Long,  // Tiempo discreto n
    val angle: Float,        // Ángulo medido
    val samplingTime: Long,  // Tiempo de muestreo en milisegundos
    val timestamp: Long      // Timestamp de la medición en milisegundos
)