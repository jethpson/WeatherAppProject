package com.weather.weatherapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.weatherapp.weatherapp.api.WeatherResponse
import com.weather.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch

class WeatherViewModel : ViewModel() {
    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchWeatherForCity(cityName: String) {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByCity(cityName)
                _weatherData.value = response
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
            }
        }
    }

    fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByLocation(latitude, longitude)
                _weatherData.value = response
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
            }
        }
    }
}