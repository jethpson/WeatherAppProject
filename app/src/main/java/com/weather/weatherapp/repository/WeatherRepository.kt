package com.weather.weatherapp.repository

import com.weather.weatherapp.weatherapp.api.RetrofitClient
import com.weather.weatherapp.weatherapp.api.WeatherResponse

class WeatherRepository {
    private val api = RetrofitClient.weatherApi
    private val apiKey = "1c19095587fb4b0bace233250240412"

    suspend fun getWeatherByCity(cityName: String): WeatherResponse {
        return api.getWeatherByCity(apiKey, cityName)
    }

    suspend fun getWeatherByLocation(latitude: Double, longitude: Double): WeatherResponse {
        val coordinates = "$latitude,$longitude"
        return api.getWeatherByLocation(apiKey, coordinates)
    }
}