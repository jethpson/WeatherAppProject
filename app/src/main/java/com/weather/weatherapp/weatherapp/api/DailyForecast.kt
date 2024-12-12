package com.weather.weatherapp.weatherapp.api

// Data class to represent each day's weather forecast
data class DailyForecast(
    val dt: String,          // The date of the forecast (in timestamp format)
    val temp: Temp,        // Temperature data (day/night)
    val weather: List<WeatherCondition> // Weather condition details (e.g., clear, rainy)
)

// Data class to represent temperature data for each day
data class Temp(
    val day: Double,  // Daytime temperature (in Celsius or Fahrenheit)
    val night: Double // Nighttime temperature (in Celsius or Fahrenheit)
)

// Data class to represent weather conditions (e.g., description: "clear sky")
data class WeatherCondition(
    val description: String // Weather condition description (e.g., "clear sky")
)



