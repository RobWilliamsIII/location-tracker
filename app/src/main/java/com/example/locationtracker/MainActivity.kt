package com.example.locationtracker

// Imports
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Main class
class LocationActivity : AppCompatActivity(), OnMapReadyCallback {

    // Variables
    private lateinit var gmap: GoogleMap // Google Map
    private lateinit var userLocationClient: FusedLocationProviderClient // Identifies user location
    private lateinit var locationUpdate: LocationCallback // location updates
    private lateinit var userLocationText: TextView // Displays user's location

    private val LOCATION_PERMISSION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Show location coordinates
        userLocationText = findViewById(R.id.location)

        // Get user location
        userLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Check for location permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Ask for permissions
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
        } else {
            startMap()
        }
    }

    // Start map fragment
    private fun startMap() {

        // Load map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.user_map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // Map ready to use
    override fun onMapReady(googleMap: GoogleMap) {
        gmap = googleMap // Store map instance

        // Check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        // Display user location on map
        gmap.isMyLocationEnabled = true

        // check and update user location
        locationUpdate = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateUserLocation(location)
                }
            }
        }

        // Request location every 5 seconds
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .setWaitForAccurateLocation(false)
            .build()

        // Start requesting location updates
        userLocationClient.requestLocationUpdates(locationRequest, locationUpdate, null)
    }

    // Show user location on map and location text
    private fun updateUserLocation(location: Location) {

        // user location lat and long
        val currentLatLng = LatLng(location.latitude, location.longitude)
        gmap.clear()
        gmap.addMarker(MarkerOptions().position(currentLatLng)) // Mark user current location
        gmap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 13f)) // Zoom in on map

        // Show user current lat and long
        userLocationText.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
    }

    // Handle permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startMap() // Start map
        }
    }
}
