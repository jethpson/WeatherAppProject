package com.weather.weatherapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.weather.weatherapp.databinding.ItemForecastBinding
import com.weather.weatherapp.Model.ForecastDay // Import the ForecastDay class

class ForecastAdapter(private val forecastList: List<ForecastDay>) :
    RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val binding = ItemForecastBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ForecastViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val forecast = forecastList[position]
        holder.bind(forecast)
    }

    override fun getItemCount(): Int = forecastList.size

    inner class ForecastViewHolder(private val binding: ItemForecastBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(forecast: ForecastDay) {
            val day = forecast.day

            binding.date.text = forecast.date
            binding.temperature.text = "${day.avgtemp_f}°F"
            binding.description.text = day.condition.text

            // Use Coil to load the weather icon from the condition object
            binding.icon.load("https://openweathermap.org/img/wn/${day.condition.icon}.png") {
                crossfade(true)
                placeholder(R.drawable.placeholder_icon) // Optional placeholder
            }
        }
    }
}
