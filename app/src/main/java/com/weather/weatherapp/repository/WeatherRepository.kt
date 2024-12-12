package com.weather.weatherapp.repository

import com.weather.weatherapp.BuildConfig
import com.weather.weatherapp.weatherapp.api.RetrofitClient
import com.weather.weatherapp.weatherapp.api.WeatherResponse as ApiWeatherResponse // Aliasing for clarity
import com.weather.weatherapp.Model.WeatherResponse // App's WeatherResponse
import com.weather.weatherapp.Model.ForecastDay // App's ForecastDay
import com.weather.weatherapp.Model.Location // App's Location
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {
    private val api = RetrofitClient.weatherApi
    private val apiKey = BuildConfig.WEATHERAPI_KEY

    suspend fun getWeatherByCity(cityName: String): WeatherResponse {
        val response = api.getWeatherByCity(apiKey, cityName)
        return mapApiToModel(response)
    }

    suspend fun getWeatherByLocation(latitude: Double, longitude: Double): WeatherResponse {
        val response = api.getWeatherByLocation(apiKey, "$latitude,$longitude")
        return mapApiToModel(response)
    }

    suspend fun get7DayForecast(location: String): List<ForecastDay> {
        val response = api.get7DayForecast(apiKey, location, 7)
        return response.forecast.forecastday.map { mapApiDailyForecastToModel(it) }
    }

    // Mapping functions
    private fun mapApiToModel(apiResponse: ApiWeatherResponse): WeatherResponse {
        return WeatherResponse(
            location = mapApiLocationToModel(apiResponse.location),
            current = mapApiCurrentToModel(apiResponse.current),
            forecast = mapApiForecastToModel(apiResponse.forecast)
        )
    }

    private fun mapApiLocationToModel(apiLocation: com.weather.weatherapp.weatherapp.api.Location): Location {
        return Location(
            name = apiLocation.name,
            region = apiLocation.region,
            country = apiLocation.country,
            lat = apiLocation.lat,
            lon = apiLocation.lon,
            tz_id = apiLocation.tz_id,
            localtime = apiLocation.localtime
        )
    }

    private fun mapApiCurrentToModel(apiCurrent: com.weather.weatherapp.weatherapp.api.Current): com.weather.weatherapp.Model.Current {
        return com.weather.weatherapp.Model.Current(
            temp_c = apiCurrent.temp_c,
            temp_f = apiCurrent.temp_f,
            condition = mapApiConditionToModel(apiCurrent.condition),
            wind_kph = apiCurrent.wind_kph,
            wind_dir = apiCurrent.wind_dir,
            humidity = apiCurrent.humidity,
            feelslike_c = apiCurrent.feelslike_c,
            feelslike_f = apiCurrent.feelslike_f
        )
    }

    private fun mapApiConditionToModel(apiCondition: com.weather.weatherapp.weatherapp.api.Condition): com.weather.weatherapp.Model.Condition {
        return com.weather.weatherapp.Model.Condition(
            text = apiCondition.text,
            icon = apiCondition.icon,
            code = apiCondition.code
        )
    }

    private fun mapApiForecastToModel(apiForecast: com.weather.weatherapp.weatherapp.api.Forecast): com.weather.weatherapp.Model.Forecast {
        return com.weather.weatherapp.Model.Forecast(
            forecastday = apiForecast.forecastday.map { mapApiDailyForecastToModel(it) }
        )
    }

    private fun mapApiDailyForecastToModel(dailyForecast: com.weather.weatherapp.weatherapp.api.ForecastDay): com.weather.weatherapp.Model.ForecastDay {
        // Convert the timestamp (Long) to a human-readable date string
        val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dailyForecast.date))

        return com.weather.weatherapp.Model.ForecastDay(
            date = formattedDate,  // Use the formatted date string
            day = mapApiDayToModel(dailyForecast.day)
        )
    }

    private fun mapApiDayToModel(apiDay: com.weather.weatherapp.weatherapp.api.Day): com.weather.weatherapp.Model.Day {
        return com.weather.weatherapp.Model.Day(
            maxtemp_c = apiDay.maxtemp_c,
            mintemp_c = apiDay.mintemp_c,
            avgtemp_c = apiDay.avgtemp_c,
            condition = mapApiConditionToModel(apiDay.condition)
        )
    }

}



