import java.time.LocalDate

data class WeatherForecast(
    val date: LocalDate,       // Using LocalDate for easier date manipulation
    val temp: Double,          // Temperature
    val description: String?,  // Nullable description
    val icon: String?          // Nullable icon
)


/*Implement Later
{
    fun getIconResource(): Int {
        return when (icon) {
            "01d" -> R.drawable.icon_sunny
            "02d" -> R.drawable.icon_partly_cloudy
            "03d" -> R.drawable.icon_cloudy
            "04d" -> R.drawable.icon_very_cloudy
            "09d" -> R.drawable.icon_rainy
            "10d" -> R.drawable.icon_rain_showers
            "11d" -> R.drawable.icon_stormy
            "13d" -> R.drawable.icon_snowy
            "50d" -> R.drawable.icon_foggy
            else -> R.drawable.icon_default // Fallback icon
        }
    }
}
*/
