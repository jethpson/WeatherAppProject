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

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData  // Changed this line to accept nullable type

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _sevenDayForecast = MutableLiveData<List<ForecastDay>>()
    val sevenDayForecast: LiveData<List<ForecastDay>> = _sevenDayForecast

    // Fetch weather data based on city name
    fun fetchWeatherForCity(cityName: String, apiKey: String = "") {
        viewModelScope.launch {
            try {
                Log.d("WeatherViewModel", "Starting fetch for city: $cityName")
                val response = repository.getWeatherByCity(cityName)
                Log.d("WeatherViewModel", "Received response for $cityName: $response")

                if (response == null) {
                    Log.e("WeatherViewModel", "Null response received for $cityName")
                    _error.value = "Weather data not available"
                    _weatherData.value = null
                    return@launch
                }

                _weatherData.value = response
                Log.d("WeatherViewModel", "Successfully set weather data: ${response.current.temp_f}°F")

                response.forecast?.let { forecast ->
                    Log.d("WeatherViewModel", "Processing forecast with ${forecast.forecastday.size} days")
                    _sevenDayForecast.value = forecast.forecastday
                } ?: run {
                    Log.e("WeatherViewModel", "Forecast data is null")
                    _sevenDayForecast.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather for $cityName", e)
                _error.value = "Error fetching weather: ${e.message}"
                _weatherData.value = null
                _sevenDayForecast.value = emptyList()
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

                // Add null safety check
                response?.forecast?.let { forecast ->
                    _sevenDayForecast.value = forecast.forecastday
                } ?: run {
                    _sevenDayForecast.value = emptyList()
                    Log.e("WeatherViewModel", "Forecast data is null")
                }
            } catch (e: Exception) {
                _error.value = "Error fetching weather: ${e.message}"
                _sevenDayForecast.value = emptyList()
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
                _sevenDayForecast.value = emptyList()
            }
        }
    }

    fun mapApiDayToModel(apiTemp: com.weather.weatherapp.weatherapp.api.Temp, apiWeather: List<com.weather.weatherapp.weatherapp.api.WeatherCondition>): com.weather.weatherapp.Model.Day {
        return com.weather.weatherapp.Model.Day(
            maxtemp_f = apiTemp.day,
            mintemp_f = apiTemp.night,
            avgtemp_f = (apiTemp.day + apiTemp.night) / 2,
            condition = mapApiConditionToModel(apiWeather.first())
        )
    }

    fun mapApiConditionToModel(apiCondition: com.weather.weatherapp.weatherapp.api.WeatherCondition): com.weather.weatherapp.Model.Condition {
        return com.weather.weatherapp.Model.Condition(
            text = apiCondition.description,
            icon = "",
            code = 0
        )
    }
}