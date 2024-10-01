package com.univalle.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.univalle.app.data.models.SessionEntity

@Dao
interface SessionDao {

    @Insert
    suspend fun insertSession(session: SessionEntity): Long

    @Query("SELECT * FROM sessions WHERE experimentId = :experimentId")
    suspend fun getSessionsForExperiment(experimentId: Long): List<SessionEntity>

    @Query("SELECT * FROM sessions")
    suspend fun getAllSessions(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Update
    suspend fun updateSession(session: SessionEntity)  // Método para actualizar la sesión
}