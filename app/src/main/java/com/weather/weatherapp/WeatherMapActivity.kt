package com.weather.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.UrlTileProvider
import java.net.MalformedURLException
import java.net.URL

class WeatherMapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val openWeatherMapKey = BuildConfig.OPENWEATHER_API_KEY
    private var locationLat: Double = 0.0
    private var locationLon: Double = 0.0
    private var locationName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("WeatherMapActivity", "onCreate started")
        setContentView(R.layout.activity_weather_map)

        // Get location data from intent
        locationLat = intent.getDoubleExtra("LOCATION_LAT", 0.0)
        locationLon = intent.getDoubleExtra("LOCATION_LON", 0.0)
        locationName = intent.getStringExtra("LOCATION_NAME") ?: ""

        Log.d("WeatherMapActivity", "Received location data - Lat: $locationLat, Lon: $locationLon, Location: $locationName")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        Log.d("WeatherMapActivity", "Getting map async")
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        Log.d("WeatherMapActivity", "onMapReady called")
        map = googleMap

        // Add the OpenWeatherMap tile overlay
        val tileProvider = object : UrlTileProvider(256, 256) {
            override fun getTileUrl(x: Int, y: Int, zoom: Int): URL? {
                val url = "https://maps.openweathermap.org/maps/2.0/weather/PA0/$zoom/$x/$y?appid=$openWeatherMapKey"
                Log.d("WeatherMapActivity", "Creating tile URL: $url")
                return try {
                    URL(url)
                } catch (e: MalformedURLException) {
                    Log.e("WeatherMapActivity", "Error creating URL", e)
                    null
                }
            }
        }
        map.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider))

        // Center on the passed location if available
        if (locationLat != 0.0 && locationLon != 0.0) {
            Log.d("WeatherMapActivity", "Moving camera to saved location: $locationLat, $locationLon")
            val location = LatLng(locationLat, locationLon)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
        } else {
            Log.d("WeatherMapActivity", "No saved location, trying to use user location")
            // Center on user location
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        Log.d("WeatherMapActivity", "Using user location: ${it.latitude}, ${it.longitude}")
                        val userLocation = LatLng(it.latitude, it.longitude)
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 10f))
                    }
                }
            } else {
                Log.d("WeatherMapActivity", "No location permission")
            }
        }
    }
}