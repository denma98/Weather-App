package com.example.weatherapp

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_data")
data class WeatherData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0, // Remove the nullable type and default value
    val cityName: String,
    val maxTemp: Double,
    val minTemp: Double,
    val date: String
)


