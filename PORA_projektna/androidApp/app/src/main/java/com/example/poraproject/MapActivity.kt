/*
package com.example.poraproject

import CrashReport
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poraproject.databinding.MapActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.w3c.dom.Document


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val REQUEST_CODE_CRASH_ACTIVITY = 2001
    }

    private lateinit var binding: MapActivityBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var isReturningFromCrashActivity = false // Flag to track if returning from CrashActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_REQUEST_CODE)
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        val maribor = LatLng(46.5547, 15.6459)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maribor, 10f))
        googleMap.uiSettings.isZoomControlsEnabled = true

        googleMap.setOnMapClickListener { latLng ->
            // Add a marker at the clicked location
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))

            // Open the new activity with the clicked location's details
            val intent = Intent(this, CrashActivity::class.java)
            intent.putExtra("latitude", latLng.latitude)
            intent.putExtra("longitude", latLng.longitude)
            startActivityForResult(intent, REQUEST_CODE_CRASH_ACTIVITY)
        }

        val crashReports = fetchCrashReports()
        crashReports.forEach { crashReport ->
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(crashReport.latitude, crashReport.longitude))
                    .title(crashReport.title)
                    .snippet("${crashReport.description} - Force: ${crashReport.force}")
            )
        }

        val initialLocation = LatLng(46.5547, 15.6459) // Example location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
        googleMap.uiSettings.isZoomControlsEnabled = true



    }

    // Override onActivityResult to detect if the activity is canceled
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CRASH_ACTIVITY) {
            if (resultCode == RESULT_CANCELED) {
                // Set flag to clear markers on resume
                isReturningFromCrashActivity = true
            }
        }
    }

    // Clear markers when returning from CrashActivity
    override fun onResume() {
        super.onResume()
        mapView.onResume()

        if (isReturningFromCrashActivity) {
            googleMap.clear() // Clear all markers
            isReturningFromCrashActivity = false // Reset flag
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    suspend fun fetchCrashReports(): List<CrashReport> {
        val collection = DBManagement.getCollection("your_collection_name") // Replace with your collection name

        // Fetch all documents as a list and map them to CrashReport
        val documents = collection.find().toList() // Ensure you convert to a list
        return documents.map { document ->
            val title = document.getString("title") ?: ""
            val description = document.getString("description") ?: ""

            // Extract latitude and longitude from the coordinates field
            val coordinatesArray = document.get("coordinates", Document::class.java)
                ?.getList("coordinates", Double::class.java)
            val latitude = coordinatesArray?.getOrNull(1) ?: 0.0
            val longitude = coordinatesArray?.getOrNull(0) ?: 0.0

            // Extract resolved address (assumed as optional in your document structure)
            val resolvedAddress = document.getString("user") ?: "Anonymous" // Replace with appropriate field if needed

            // Extract time and format it
            val timeOfReport = document.get("time", Document::class.java)
                ?.getString("\$date") ?: ""

            // Extract force
            val force = document.getDouble("force") ?: 0.0

            CrashReport(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                resolvedAddress = resolvedAddress,
                timeOfReport = timeOfReport,
                force = force
            )
        }
    }






}
*/
package com.example.yourapp

import CrashReport
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.poraproject.CrashActivity
import com.example.poraproject.DBManagement
import com.example.poraproject.databinding.MapActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val REQUEST_CODE_CRASH_ACTIVITY = 2001
    }

    private lateinit var binding: MapActivityBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var isReturningFromCrashActivity = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MapActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Request notification permission if targeting Tiramisu or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Initial location
        val maribor = LatLng(46.5547, 15.6459)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maribor, 10f))
        googleMap.uiSettings.isZoomControlsEnabled = true

        // On map click -> open CrashActivity
        googleMap.setOnMapClickListener { latLng ->
            googleMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
            val intent = Intent(this, CrashActivity::class.java).apply {
                putExtra("latitude", latLng.latitude)
                putExtra("longitude", latLng.longitude)
            }
            startActivityForResult(intent, REQUEST_CODE_CRASH_ACTIVITY)
        }

    }

    // Detect if the CrashActivity was cancelled
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_CRASH_ACTIVITY && resultCode == RESULT_CANCELED) {
            isReturningFromCrashActivity = true
        }
    }

    // Clear markers when returning from CrashActivity
    override fun onResume() {
        super.onResume()
        mapView.onResume()
        if (isReturningFromCrashActivity) {
            googleMap.clear()
            isReturningFromCrashActivity = false
        }
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}
