package com.univalle.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.univalle.app.data.models.MeasurementEntity

@Dao
interface MeasurementDao {

    @Insert
    suspend fun insertMeasurement(measurement: MeasurementEntity)

    @Insert
    suspend fun insertMeasurements(measurements: List<MeasurementEntity>)

    @Query("SELECT * FROM measurements WHERE sessionId = :sessionId")
    suspend fun getMeasurementsForSession(sessionId: Long): List<MeasurementEntity>

    @Query("SELECT * FROM measurements")
    suspend fun getAllMeasurements(): List<MeasurementEntity>

    @Query("SELECT * FROM measurements WHERE id = :id")
    suspend fun getMeasurementById(id: Long): MeasurementEntity?

    @Query("DELETE FROM measurements WHERE sessionId = :sessionId")
    suspend fun deleteMeasurementsForSession(sessionId: Long)
}
