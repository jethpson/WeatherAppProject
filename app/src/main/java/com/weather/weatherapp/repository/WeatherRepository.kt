package com.weather.weatherapp.repository

import android.util.Log
import com.weather.weatherapp.BuildConfig
import com.weather.weatherapp.weatherapp.api.RetrofitClient
import com.weather.weatherapp.weatherapp.api.WeatherResponse as ApiWeatherResponse
import com.weather.weatherapp.Model.WeatherResponse
import com.weather.weatherapp.Model.ForecastDay
import com.weather.weatherapp.Model.Location
import com.weather.weatherapp.Model.Forecast
import com.weather.weatherapp.Model.Day
import com.weather.weatherapp.Model.Condition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherRepository {
    private val api = RetrofitClient.weatherApi
    private val apiKey = BuildConfig.WEATHERAPI_KEY

    suspend fun getWeatherByCity(cityName: String): WeatherResponse? {
        return try {
            Log.d("WeatherRepository", "Fetching weather for city: $cityName")
            val response = api.getWeatherByCity(apiKey, cityName)
            Log.d("WeatherRepository", "Received response: $response")
            mapApiToModel(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather for city: $cityName", e)
            null
        }
    }

    suspend fun getWeatherByLocation(latitude: Double, longitude: Double): WeatherResponse? {
        return try {
            Log.d("WeatherRepository", "Fetching weather for location: $latitude,$longitude")
            val response = api.getWeatherByLocation(apiKey, "$latitude,$longitude")
            Log.d("WeatherRepository", "Received response: $response")
            mapApiToModel(response)
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching weather for location", e)
            null
        }
    }

    suspend fun get7DayForecast(location: String): List<ForecastDay> {
        return try {
            Log.d("WeatherRepository", "Fetching 7-day forecast for: $location")
            val response = api.get7DayForecast(apiKey, location, 7)
            response.forecast?.forecastday?.map { mapApiDailyForecastToModel(it) } ?: emptyList()
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error fetching 7-day forecast", e)
            emptyList()
        }
    }

    private fun mapApiToModel(apiResponse: ApiWeatherResponse): WeatherResponse {
        return try {
            WeatherResponse(
                location = mapApiLocationToModel(apiResponse.location),
                current = mapApiCurrentToModel(apiResponse.current),
                forecast = mapApiForecastToModel(apiResponse.forecast)
            )
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error mapping API response to model", e)
            throw e
        }
    }

    private fun mapApiLocationToModel(apiLocation: com.weather.weatherapp.weatherapp.api.Location?): Location {
        return Location(
            name = apiLocation?.name ?: "Unknown",
            region = apiLocation?.region ?: "",
            country = apiLocation?.country ?: "",
            lat = apiLocation?.lat ?: 0.0,
            lon = apiLocation?.lon ?: 0.0,
            tz_id = apiLocation?.tz_id ?: "",
            localtime = apiLocation?.localtime ?: ""
        )
    }

    private fun mapApiCurrentToModel(apiCurrent: com.weather.weatherapp.weatherapp.api.Current?): com.weather.weatherapp.Model.Current {
        return com.weather.weatherapp.Model.Current(
            temp_c = apiCurrent?.temp_c ?: 0.0,
            temp_f = apiCurrent?.temp_f ?: 0.0,
            condition = mapApiConditionToModel(apiCurrent?.condition),
            wind_kph = apiCurrent?.wind_kph ?: 0.0,
            wind_dir = apiCurrent?.wind_dir ?: "",
            humidity = apiCurrent?.humidity ?: 0,
            feelslike_c = apiCurrent?.feelslike_c ?: 0.0,
            feelslike_f = apiCurrent?.feelslike_f ?: 0.0
        )
    }

    private fun mapApiConditionToModel(apiCondition: com.weather.weatherapp.weatherapp.api.Condition?): com.weather.weatherapp.Model.Condition {
        return com.weather.weatherapp.Model.Condition(
            text = apiCondition?.text ?: "",
            icon = apiCondition?.icon ?: "",
            code = apiCondition?.code ?: 0
        )
    }

    private fun mapApiForecastToModel(apiForecast: com.weather.weatherapp.weatherapp.api.Forecast?): Forecast {
        return try {
            Log.d("WeatherRepository", "Mapping forecast: $apiForecast")
            Forecast(
                forecastday = apiForecast?.forecastday?.map { mapApiDailyForecastToModel(it) } ?: emptyList()
            )
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error mapping forecast", e)
            Forecast(forecastday = emptyList())
        }
    }

    private fun mapApiDailyForecastToModel(dailyForecast: com.weather.weatherapp.weatherapp.api.ForecastDay?): ForecastDay {
        return try {
            val formattedDate = if (dailyForecast?.date != null) {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(dailyForecast.date))
                } catch (e: Exception) {
                    Log.e("WeatherRepository", "Error formatting date", e)
                    ""
                }
            } else ""

            ForecastDay(
                date = formattedDate,
                day = mapApiDayToModel(dailyForecast?.day)
            )
        } catch (e: Exception) {
            Log.e("WeatherRepository", "Error mapping daily forecast", e)
            ForecastDay(date = "", day = createDefaultDay())
        }
    }

    private fun mapApiDayToModel(apiDay: com.weather.weatherapp.weatherapp.api.Day?): Day {
        return Day(
            maxtemp_c = apiDay?.maxtemp_c ?: 0.0,
            mintemp_c = apiDay?.mintemp_c ?: 0.0,
            avgtemp_c = apiDay?.avgtemp_c ?: 0.0,
            condition = mapApiConditionToModel(apiDay?.condition)
        )
    }

    private fun createDefaultDay(): Day {
        return Day(
            maxtemp_c = 0.0,
            mintemp_c = 0.0,
            avgtemp_c = 0.0,
            condition = Condition(
                text = "No data",
                icon = "",
                code = 0
            )
        )
    }
}