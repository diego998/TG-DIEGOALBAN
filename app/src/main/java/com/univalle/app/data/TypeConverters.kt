package com.univalle.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataConverters {

    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        val listType = object : TypeToken<List<Long>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromFloatList(value: List<Float>): String {
        val gson = Gson()
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFloatList(value: String): List<Float> {
        val listType = object : TypeToken<List<Float>>() {}.type
        return Gson().fromJson(value, listType)
    }
}
