package com.weather.weatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import java.util.TimeZone
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var locationTextView: TextView
    private lateinit var locationEditText: EditText
    private var globalLocation: String = "Location not available"

    private val handler = Handler(Looper.getMainLooper())
    private val delay = 3 * 1000L // 30 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
    }

    private fun startRepeatingTask(timeTextView: TextView) {
        handler.postDelayed(object : Runnable {
            override fun run() {
                // Call the updateTime method to update the time every 30 seconds
                updateTime(timeTextView)

                // Repeat the task every 30 seconds
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    // Function to toggle the navigation drawer
    private fun toggleDrawer() {
        if (drawerLayout.isDrawerOpen(findViewById(R.id.navigationDrawer))) {
            drawerLayout.closeDrawer(findViewById(R.id.navigationDrawer))
        } else {
            drawerLayout.openDrawer(findViewById(R.id.navigationDrawer))
        }
    }

    @SuppressLint("MissingPermission")
    private fun fetchLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Use Geocoder to get city name
                val geocoder = Geocoder(this, Locale.getDefault())
                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                if (addresses != null && addresses.isNotEmpty()) {
                    val cityName = addresses[0].locality
                    Log.d("LocationFetch", "Found city: $cityName") // Log city name
                    locationTextView.text = cityName
                    globalLocation = cityName ?: "City not found"
                } else {
                    Log.d("LocationFetch", "City not found") // Log if city not found
                    locationTextView.text = "City not found"
                    globalLocation = "Location not available"
                }
            } else {
                Log.d("LocationFetch", "Location not available") // Log if location is null
                locationTextView.text = "Location not available"
                globalLocation = "Failed to get location"
            }
        }.addOnFailureListener {
            Log.e("LocationFetch", "Failed to fetch location", it) // Log any failure
            locationTextView.text = "Failed to get location"
        }
    }

    private fun showEditTextForLocation() {
        locationTextView.visibility = View.GONE
        locationEditText.visibility = View.VISIBLE
        locationEditText.setText(locationTextView.text) // Populate the EditText with current location
        locationEditText.requestFocus()  // Make sure the EditText has focus
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
        Log.d("LocationSearch", "Searching for: $query") // Log search query
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            // Use Geocoder to search for locations matching the query
            val addresses = geocoder.getFromLocationName(query, 1) // Search for the location name

            if (addresses != null && addresses.isNotEmpty()) {
                // Choose the closest match (first result)
                val closestLocation = addresses[0].locality ?: addresses[0].featureName
                Log.d("LocationSearch", "Found location: $closestLocation") // Log the found location
                updateLocation(closestLocation)
            } else {
                // No matching location found
                Log.d("LocationSearch", "No matching location found") // Log if no location found
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("LocationSearch", "Error while searching location", e) // Log errors
        }
    }

    private fun updateLocation(newLocation: String) {
        Log.d("LocationUpdate", "Updating location to: $newLocation") // Log location update
        globalLocation = newLocation
        locationTextView.text = newLocation  // This updates the TextView with the new location
        locationTextView.visibility = View.VISIBLE
        locationEditText.visibility = View.GONE
        hideKeyboard(locationEditText)  // Hide the keyboard after update
    }

    private val cityTimeZones = mapOf(
        "New York" to "America/New_York", // GMT-5
        "Los Angeles" to "America/Los_Angeles", // GMT-8
        "London" to "Europe/London", // GMT+0
        "Paris" to "Europe/Paris", // GMT+1
        "Berlin" to "Europe/Berlin", // GMT+1
        "Tokyo" to "Asia/Tokyo", // GMT+9
        "Sydney" to "Australia/Sydney", // GMT+10
        "Mumbai" to "Asia/Kolkata", // GMT+5:30
        "Cape Town" to "Africa/Johannesburg", // GMT+2
        "Rio de Janeiro" to "America/Sao_Paulo", // GMT-3
        "Mexico City" to "America/Mexico_City", // GMT-6
        "Beijing" to "Asia/Shanghai", // GMT+8
        "Dubai" to "Asia/Dubai", // GMT+4
        "Moscow" to "Europe/Moscow", // GMT+3
        "Toronto" to "America/Toronto", // GMT-5
        "Buenos Aires" to "America/Argentina/Buenos_Aires", // GMT-3
        "Cairo" to "Africa/Cairo", // GMT+2
        "Jakarta" to "Asia/Jakarta", // GMT+7
        "Seoul" to "Asia/Seoul", // GMT+9
        "Santiago" to "America/Santiago", // GMT-4
        "Istanbul" to "Europe/Istanbul" // GMT+3
    )

    fun updateTime(timeTextView: TextView) {
        if (globalLocation != "Location not available" && globalLocation != "Failed to get location") {
            // Get the current time and format it based on the timezone of the location
            val currentTime = Date()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Example: 10:30 AM

            try {
                // Get the timezone of the phone (device's current time zone)
                val phoneTimeZone = TimeZone.getDefault()

                // Fetch the time zone for the global location (from the cityTimeZones map)
                val locationTimeZoneId = cityTimeZones[globalLocation]

                if (locationTimeZoneId != null) {
                    val locationTimeZone = TimeZone.getTimeZone(locationTimeZoneId)

                    // Log for debugging
                    Log.d("TimeUpdate", "Phone TimeZone: ${phoneTimeZone.id}, Location TimeZone: ${locationTimeZone.id}")

                    // Get the calendar for the location time zone
                    val calendar = Calendar.getInstance(locationTimeZone)
                    calendar.time = currentTime

                    // Convert to phone's time zone by adjusting with offsets
                    val phoneOffset = phoneTimeZone.rawOffset
                    val locationOffset = locationTimeZone.rawOffset

                    val adjustedTime = Date(currentTime.time + (locationOffset - phoneOffset).toLong())

                    // Format the adjusted time for display
                    val formattedTime = timeFormat.format(adjustedTime)

                    // Set the formatted time to the TextView
                    timeTextView.text = formattedTime
                } else {
                    Log.e("TimeUpdate", "Location TimeZone not found for $globalLocation")
                }
            } catch (e: Exception) {
                // Handle any error with fetching the timezone or formatting
                Log.e("TimeUpdate", "Error updating time: ", e)
            }
        } else {
            // Fallback to showing current local time if location is not available
            val currentTime = Date()
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()) // Example: 10:30 AM
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