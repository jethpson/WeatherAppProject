package com.weather.weatherapp.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // Main weather endpoints that include forecast
    @GET("v1/forecast.json")
    suspend fun getWeatherByCity(
        @Query("key") apiKey: String,
        @Query("q") cityName: String,
        @Query("days") days: Int = 7,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse

    @GET("v1/forecast.json")
    suspend fun getWeatherByLocation(
        @Query("key") apiKey: String,
        @Query("q") coordinates: String,
        @Query("days") days: Int = 7,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse

    // Keep this method but ensure it uses forecast.json
    @GET("v1/forecast.json")
    suspend fun get7DayForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7
    ): WeatherResponse

    // Alternative forecast method
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7
    ): WeatherResponse
}