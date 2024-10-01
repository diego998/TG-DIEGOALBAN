package com.univalle.app.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.univalle.app.data.DataConverters
import com.univalle.app.data.dao.ExperimentDao
import com.univalle.app.data.dao.MeasurementDao
import com.univalle.app.data.dao.SessionDao
import com.univalle.app.data.models.ExperimentEntity
import com.univalle.app.data.models.MeasurementEntity
import com.univalle.app.data.models.SessionEntity

@Database(
    entities = [ExperimentEntity::class, SessionEntity::class, MeasurementEntity::class],
    version = 1
)
@TypeConverters(DataConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun experimentDao(): ExperimentDao
    abstract fun sessionDao(): SessionDao
    abstract fun measurementDao(): MeasurementDao
}