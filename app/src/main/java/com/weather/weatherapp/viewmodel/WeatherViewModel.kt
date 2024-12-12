import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.weather.weatherapp.Model.WeatherResponse
import com.weather.weatherapp.Model.ForecastDay
import com.weather.weatherapp.repository.WeatherRepository
import kotlinx.coroutines.launch
import android.util.Log

class WeatherViewModel : ViewModel() {

    private val repository = WeatherRepository()

    private val _weatherData = MutableLiveData<WeatherResponse>()
    val weatherData: LiveData<WeatherResponse> get() = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _sevenDayForecast = MutableLiveData<List<ForecastDay>>()
    val sevenDayForecast: LiveData<List<ForecastDay>> get() = _sevenDayForecast

    // Fetch weather data based on city name
    fun fetchWeatherForCity(apiKey: String, cityName: String) {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByCity(cityName)
                _weatherData.value = response

                // Log the response to inspect the structure
                Log.d("WeatherViewModel", "Weather Response: $response")

                // Ensure forecast is accessed correctly
                _sevenDayForecast.value = response.forecast.forecastday // Access forecast correctly
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
            }
        }
    }

    // Fetch weather data based on location (latitude and longitude)
    fun fetchWeatherForLocation(apiKey: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getWeatherByLocation(latitude, longitude)
                _weatherData.value = response

                // Log the response to inspect the structure
                Log.d("WeatherViewModel", "Weather Response: $response")

                _sevenDayForecast.value = response.forecast.forecastday // Set the 7-day forecast
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
            }
        }
    }

    fun fetch7DayForecast(location: String) {
        viewModelScope.launch {
            try {
                val forecastDays = repository.get7DayForecast(location)
                _sevenDayForecast.value = forecastDays
            } catch (e: Exception) {
                _error.value = "Error fetching forecast: ${e.message}"
            }
        }
    }

    fun mapApiDayToModel(apiTemp: com.weather.weatherapp.weatherapp.api.Temp, apiWeather: List<com.weather.weatherapp.weatherapp.api.WeatherCondition>): com.weather.weatherapp.Model.Day {
        return com.weather.weatherapp.Model.Day(
            maxtemp_c = apiTemp.day,  // Assume we use 'day' for the max temp
            mintemp_c = apiTemp.night, // Assume we use 'night' for the min temp
            avgtemp_c = (apiTemp.day + apiTemp.night) / 2, // Average temperature
            condition = mapApiConditionToModel(apiWeather.first()) // Map to the first weather condition (or modify as needed)
        )
    }

    fun mapApiConditionToModel(apiCondition: com.weather.weatherapp.weatherapp.api.WeatherCondition): com.weather.weatherapp.Model.Condition {
        return com.weather.weatherapp.Model.Condition(
            text = apiCondition.description,
            icon = "", // Add appropriate logic for icon mapping, if needed
            code = 0 // Add appropriate logic for the code, if needed
        )
    }
}

