package com.univalle.app.data.database


import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    fun getDatabase(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java, "experiments-database"
        ).build()
    }
}
