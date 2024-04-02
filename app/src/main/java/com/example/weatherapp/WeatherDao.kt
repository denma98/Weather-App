package com.example.weatherapp

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert
    fun upsert(weatherData: WeatherData)
    @Query("SELECT * FROM weather_data")
    fun getWeatherData(): Flow<List<WeatherData>>
}