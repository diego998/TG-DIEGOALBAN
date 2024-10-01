package com.univalle.app.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "experiments")
data class ExperimentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                 // Nombre del experimento
    val description: String? = null   // Descripción opcional del experimento
)
