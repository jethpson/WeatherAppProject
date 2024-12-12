package com.weather.weatherapp.Model

data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)

data class Location(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val tz_id: String,  // Ensure tz_id is included here
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

data class Forecast(
    val forecastday: List<ForecastDay>
)

data class ForecastDay(
    val date: String,
    val day: Day
)

data class Day(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val avgtemp_c: Double,
    val condition: Condition
)

