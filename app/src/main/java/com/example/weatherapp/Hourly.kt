package com.example.weatherapp

data class Hourly(
    val rain: List<Double>,
    val relative_humidity_2m: List<Int>,
    val temperature_2m: List<Double>,
    val time: List<String>
)