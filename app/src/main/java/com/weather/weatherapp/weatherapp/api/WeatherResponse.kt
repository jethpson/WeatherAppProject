package com.weather.weatherapp.weatherapp.api

data class WeatherResponse(
    val location: Location,
    val current: Current
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val localtime: String
)

data class Current(
    val temp_c: Double,
    val temp_f: Double,
    val condition: Condition,
    val wind_kph: Double,
    val wind_dir: String,
    val humidity: Int,
    val feelslike_c: Double,
    val feelslike_f: Double
)

data class Condition(
    val text: String,
    val icon: String,
    val code: Int
)