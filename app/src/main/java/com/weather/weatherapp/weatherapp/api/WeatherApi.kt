package com.weather.weatherapp.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/current.json")
    suspend fun getWeatherByCity(
        @Query("key") apiKey: String,
        @Query("q") cityName: String,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse

    @GET("v1/current.json")
    suspend fun getWeatherByLocation(
        @Query("key") apiKey: String,
        @Query("q") coordinates: String,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse
}