package com.example.weatherapp

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiInterface {
    @GET("weather")
    fun getWeatherData(
        @Query("q") q: String,
        @Query("appid") appid: String,
        @Query("units") units: String
    ): Call<WeatherApp>
}