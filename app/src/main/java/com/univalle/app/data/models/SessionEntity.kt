package com.univalle.app.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sessions",
    foreignKeys = [ForeignKey(
        entity = ExperimentEntity::class,
        parentColumns = ["id"],
        childColumns = ["experimentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["experimentId"])]
)
data class SessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val experimentId: Long,            // Referencia al experimento al que pertenece la sesión
    val startTime: Long,               // Hora de inicio de la sesión (timestamp)
    val endTime: Long? = null          // Hora de finalización de la sesión (timestamp, opcional)
)


