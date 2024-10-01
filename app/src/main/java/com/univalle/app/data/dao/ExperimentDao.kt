package com.univalle.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.univalle.app.data.models.ExperimentEntity

@Dao
interface ExperimentDao {
    @Insert
    suspend fun insertExperiment(experiment: ExperimentEntity): Long

    @Query("SELECT * FROM experiments")
    suspend fun getAllExperiments(): List<ExperimentEntity>

    @Query("SELECT * FROM experiments WHERE id = :experimentId")
    suspend fun getExperimentById(experimentId: Long): ExperimentEntity?

    @Delete
    suspend fun deleteExperiment(experiment: ExperimentEntity)  // Asegúrate de tener esta función
}