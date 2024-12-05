package com.weather.weatherapp.repository

import com.weather.weatherapp.BuildConfig
import com.weather.weatherapp.weatherapp.api.RetrofitClient
import com.weather.weatherapp.weatherapp.api.WeatherResponse

class WeatherRepository {
    private val api = RetrofitClient.weatherApi
    private val apiKey = BuildConfig.WEATHERAPI_KEY

    suspend fun getWeatherByCity(cityName: String): WeatherResponse {
        return api.getWeatherByCity(apiKey, cityName)
    }

    suspend fun getWeatherByLocation(latitude: Double, longitude: Double): WeatherResponse {
        val coordinates = "$latitude,$longitude"
        return api.getWeatherByLocation(apiKey, coordinates)
    }
}