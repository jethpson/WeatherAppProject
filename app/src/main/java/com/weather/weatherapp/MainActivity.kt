package com.weather.weatherapp

import WeatherViewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.CameraUpdateFactory
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.google.android.gms.maps.model.TileProvider
import com.google.android.gms.maps.model.Tile
import com.google.android.material.navigation.NavigationView
import com.weather.weatherapp.Model.WeatherResponse //Check cuz there are two of them
import java.net.HttpURLConnection

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var locationTextView: TextView
    private lateinit var locationEditText: EditText
    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var displayTempTextView: TextView
    private lateinit var cloudStatusTextView: TextView
    private lateinit var map: GoogleMap
    private var globalLocation: String = "Location not available"
    private val openWeatherMapKey = BuildConfig.OPENWEATHER_API_KEY

    private val handler = Handler(Looper.getMainLooper())
    private val delay = 3 * 1000L // 3 seconds

    //for 7-day-forecast
    private lateinit var navigationView: NavigationView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize weather-related views
        displayTempTextView = findViewById(R.id.DisplayTemp)
        cloudStatusTextView = findViewById(R.id.CloudStatus)

        // Initialize ViewModel
        weatherViewModel = ViewModelProvider(this)[WeatherViewModel::class.java]

        // Observe weather data
        weatherViewModel.weatherData.observe(this) { weatherResponse ->
            weatherResponse?.let { response ->
                updateWeatherUI(response)
            } ?: run {
                // Handle null case
                Toast.makeText(this, "Weather data not available", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe errors
        weatherViewModel.error.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
        }

        // Initialize RecyclerView for the 7-day forecast
        val recyclerView7DayForecast = findViewById<RecyclerView>(R.id.recyclerView7DayForecast)
        recyclerView7DayForecast.layoutManager = LinearLayoutManager(this)

        // Observe the 7-day forecast data
        weatherViewModel.sevenDayForecast.observe(this) { forecastList ->
            val adapter = ForecastAdapter(forecastList)
            recyclerView7DayForecast.adapter = adapter
        }

        // Fetch weather for a city (You can replace this with user input or a default city)
        weatherViewModel.fetchWeatherForCity("your_api_key", "New York")  // Replace with actual city and API key

        // Initialize the DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)

        // Find the ThreeBarImageView and set a click listener
        val threeBarImageView: View = findViewById(R.id.ThreeBarImageView)
        threeBarImageView.setOnClickListener {
            toggleDrawer()
        }

        // Find the Time TextView
        val timeTextView: TextView = findViewById(R.id.Time)

        // Update the Time TextView with the current time
        updateTime(timeTextView)

        startRepeatingTask(timeTextView)

        // Initialize location views
        locationTextView = findViewById(R.id.Location)
        locationEditText = findViewById(R.id.LocationEditText)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize the map
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.weather_map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Check location permission and fetch the location
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fetchLocation()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }

        // Set click listener to switch to EditText for location input
        locationTextView.setOnClickListener {
            showEditTextForLocation()
        }

        // Handle when editing location is done
        locationEditText.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searchAndUpdateLocation(locationEditText.text.toString())
                return@setOnEditorActionListener true
            }
            false
        }

        val navigationDrawer = findViewById<LinearLayout>(R.id.navigationDrawer)
        val weatherMapButton = navigationDrawer.getChildAt(0) as TextView
        weatherMapButton.setOnClickListener {
            startActivity(Intent(this, WeatherMapActivity::class.java))
            drawerLayout.closeDrawers()
        }

        // Find the forecast button
        val forecastButton = findViewById<TextView>(R.id.forecast_button)

        // Set click listener for the forecast button
        forecastButton.setOnClickListener {
            // Toggle visibility of the RecyclerView
            if (recyclerView7DayForecast.visibility == View.VISIBLE) {
                recyclerView7DayForecast.visibility = View.GONE
            } else {
                recyclerView7DayForecast.visibility = View.VISIBLE
                // Fetch the forecast data when showing the RecyclerView
                weatherViewModel.fetchWeatherForCity(openWeatherMapKey, globalLocation)
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.uiSettings.apply {
            isZoomGesturesEnabled = false
            isZoomControlsEnabled = false
            isScrollGesturesEnabled = false
            isRotateGesturesEnabled = false
        }

        val tileProvider = object : TileProvider {
            val tileSize = 256

            override fun getTile(x: Int, y: Int, zoom: Int): Tile? {
                val url = "https://tile.openweathermap.org/map/temp_new/$zoom/$x/$y.png?appid=$openWeatherMapKey"
                try {
                    Log.d("WeatherMap", "Fetching tile: $url")
                    val connection = URL(url).openConnection() as HttpURLConnection
                    connection.doInput = true
                    connection.connect()

                    if (connection.responseCode == 200) {
                        val input = connection.inputStream
                        val byteArray = input.readBytes()

                        // Debug: Print first few bytes to verify we're getting PNG data
                        val hexString = byteArray.take(8).joinToString("") {
                            String.format("%02X", it)
                        }
                        Log.d("WeatherMap", "First 8 bytes: $hexString")

                        // Debug: Check content type
                        val contentType = connection.contentType
                        Log.d("WeatherMap", "Content Type: $contentType")

                        return Tile(tileSize, tileSize, byteArray)
                    } else {
                        Log.e("WeatherMap", "Server returned code: ${connection.responseCode}")
                        // Debug: Print error stream if available
                        val errorStream = connection.errorStream?.bufferedReader()?.readText()
                        Log.e("WeatherMap", "Error response: $errorStream")
                    }
                } catch (e: Exception) {
                    Log.e("WeatherMap", "Error loading tile: $url", e)
                }
                return null
            }
        }

        map.addTileOverlay(
            TileOverlayOptions()
                .tileProvider(tileProvider)
                .visible(true)
                .fadeIn(false)
                .transparency(0.0f)  // Make fully opaque
        )



        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    val userLocation = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 5f))
                    map.setMinZoomPreference(10f)
                    map.setMaxZoomPreference(10f)
                }
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateWeatherUI(weatherResponse: WeatherResponse) {
        displayTempTextView.text = "${weatherResponse.current.temp_f.toInt()}°"
        cloudStatusTextView.text = "☁︎ ${weatherResponse.current.condition.text}"
    }

    private fun startRepeatingTask(timeTextView: TextView) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                updateTime(timeTextView)
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.navigationDrawer))) {
            drawerLayout.closeDrawer(findViewById(R.id.navigationDrawer))
        } else {
            drawerLayout.openDrawer(findViewById(R.id.navigationDrawer))
        }
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    val cityName = addresses[0].locality
                    locationTextView.text = cityName ?: "Unknown Location"
                    globalLocation = cityName ?: "Location not available"
                } else {
                    locationTextView.text = "City not found"
                    globalLocation = "Location not available"
                }
            } else {
                Log.d("LocationFetch", "Location not available")
                locationTextView.text = "Location not available"
                globalLocation = "Failed to get location"
            }
        }.addOnFailureListener {
            Log.e("LocationFetch", "Failed to fetch location", it)
            locationTextView.text = "Failed to get location"
        }
    }

    private fun showEditTextForLocation() {
        locationTextView.visibility = View.GONE
        locationEditText.visibility = View.VISIBLE
        locationEditText.setText(locationTextView.text)
        locationEditText.requestFocus()
        showKeyboard(locationEditText)
    }

    private fun showKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun searchAndUpdateLocation(query: String) {
        Log.d("LocationSearch", "Searching for: $query")
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocationName(query, 1)

            if (!addresses.isNullOrEmpty()) {
                val closestLocation = addresses[0].locality ?: addresses[0].featureName
                Log.d("LocationSearch", "Found location: $closestLocation")
                updateLocation(closestLocation)
            } else {
                Log.d("LocationSearch", "No matching location found")
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("LocationSearch", "Error while searching location", e)
        }
    }

    private fun updateLocation(newLocation: String) {
        Log.d("LocationUpdate", "Updating location to: $newLocation")
        globalLocation = newLocation
        locationTextView.text = newLocation
        locationTextView.visibility = View.VISIBLE
        locationEditText.visibility = View.GONE
        hideKeyboard(locationEditText)

        // Fetch weather for new location
        weatherViewModel.fetchWeatherForCity(newLocation, openWeatherMapKey)
    }

    private val cityTimeZones = mapOf(
        "New York" to "America/New_York",
        "Los Angeles" to "America/Los_Angeles",
        "London" to "Europe/London",
        "Paris" to "Europe/Paris",
        "Berlin" to "Europe/Berlin",
        "Tokyo" to "Asia/Tokyo",
        "Sydney" to "Australia/Sydney",
        "Mumbai" to "Asia/Kolkata",
        "Cape Town" to "Africa/Johannesburg",
        "Rio de Janeiro" to "America/Sao_Paulo",
        "Mexico City" to "America/Mexico_City",
        "Beijing" to "Asia/Shanghai",
        "Dubai" to "Asia/Dubai",
        "Moscow" to "Europe/Moscow",
        "Toronto" to "America/Toronto",
        "Buenos Aires" to "America/Argentina/Buenos_Aires",
        "Cairo" to "Africa/Cairo",
        "Jakarta" to "Asia/Jakarta",
        "Seoul" to "Asia/Seoul",
        "Santiago" to "America/Santiago",
        "Istanbul" to "Europe/Istanbul"
    )

    fun updateTime(timeTextView: TextView) {
        if (globalLocation != "Location not available" && globalLocation != "Failed to get location") {
            val currentTime = Date()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

            try {
                val phoneTimeZone = TimeZone.getDefault()
                val locationTimeZoneId = cityTimeZones[globalLocation]

                if (locationTimeZoneId != null) {
                    val locationTimeZone = TimeZone.getTimeZone(locationTimeZoneId)
                    Log.d("TimeUpdate", "Phone TimeZone: ${phoneTimeZone.id}, Location TimeZone: ${locationTimeZone.id}")

                    val calendar = Calendar.getInstance(locationTimeZone)
                    calendar.time = currentTime

                    val phoneOffset = phoneTimeZone.rawOffset
                    val locationOffset = locationTimeZone.rawOffset

                    val adjustedTime = Date(currentTime.time + (locationOffset - phoneOffset).toLong())
                    val formattedTime = timeFormat.format(adjustedTime)
                    timeTextView.text = formattedTime
                } else {
                    Log.e("TimeUpdate", "Location TimeZone not found for $globalLocation")
                }
            } catch (e: Exception) {
                Log.e("TimeUpdate", "Error updating time: ", e)
            }
        } else {
            val currentTime = Date()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val formattedTime = timeFormat.format(currentTime)
            timeTextView.text = formattedTime
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocation()
        }
    }
}