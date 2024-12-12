package com.weather.weatherapp.weatherapp.api

import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface WeatherApi {

    // Fetch current weather by city
    @GET("v1/current.json")
    suspend fun getWeatherByCity(
        @Query("key") apiKey: String,
        @Query("q") cityName: String,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse

    // Fetch current weather by location (latitude/longitude)
    @GET("v1/current.json")
    suspend fun getWeatherByLocation(
        @Query("key") apiKey: String,
        @Query("q") coordinates: String,
        @Query("aqi") includeAirQuality: String = "no"
    ): WeatherResponse

    // Fetch 7-day forecast (correcting annotation)
    @GET("v1/forecast.json")  // Add the GET annotation
    suspend fun get7DayForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7 // Default to 7 days
    ): WeatherResponse

    // Alternative method to fetch forecast
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 7 // Default to 7 days
    ): WeatherResponse

}
