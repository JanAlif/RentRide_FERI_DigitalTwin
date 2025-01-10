package com.example.poraproject

import CrashReport2
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.poraproject.DBUtil.testMongoConnection
import com.example.poraproject.databinding.MapActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val REQUEST_CODE_CRASH_ACTIVITY = 2001
        const val COLLECTION_NAME = "trafficaccidents"
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


        testMongoConnection()

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

    private suspend fun fetchLocationsFromDatabase() {
        withContext(Dispatchers.IO) {
            try {
                // Fetch all documents from the collection
                val documents = DBUtil.getCollection("trafficaccidents").find().toList()

                // Process and display the documents (UI updates must happen on the Main thread)
                withContext(Dispatchers.Main) {
                    println("Fetched ${documents.size} documents from trafficaccidents collection")
                    documents.forEach { document ->
                        val title = document.getString("title") ?: "No Title"
                        val description = document.getString("description") ?: "No Description"

                        // Print title and description
                        println("Title: $title")
                        println("Description: $description")
                    }


                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    e.printStackTrace()
                    // Handle errors (e.g., display a Toast)
                    Toast.makeText(this@MapActivity, "Error fetching data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



    /*private fun fetchLocationsFromDatabase() {
        // Fetch all documents from the specified collection
        val documents = DBUtil.fetchAll(COLLECTION_NAME)

        if (documents.isNullOrEmpty()) {
            println("No documents found in the collection")
        }


        // Process the documents on the main thread
        documents.forEach { document ->
            // Extract data from the document
            val title = document.getString("title") ?: "No Title"
            val description = document.getString("description") ?: "No Description"
            val coordinates = document.get("coordinates") as? Map<*, *>
            val latLng = coordinates?.get("coordinates") as? List<*>
            val latitude = latLng?.get(1) as? Double ?: 0.0
            val longitude = latLng?.get(0) as? Double ?: 0.0
            val timeOfReport = document.getString("time") ?: "Unknown Time"
            val force = document.getDouble("force") ?: 0.0

            // Create a CrashReport2 instance
            val crashReport = CrashReport2(
                title = title,
                description = description,
                latitude = latitude,
                longitude = longitude,
                resolvedAddress = "", // Placeholder for resolved address
                timeOfReport = timeOfReport,
                force = force
            )

            println("Title: $title")
            println("Description: $description")
            println("Coordinates: ($latitude, $longitude)")
            println("Force: $force")
            println("Time of Report: $timeOfReport")
            println("------------------------------------------------")
            // Add a marker to the map
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(crashReport.latitude, crashReport.longitude))
                    .title(crashReport.title)
                    .snippet("Description: ${crashReport.description}\nForce: ${crashReport.force}\nTime: ${crashReport.timeOfReport}")
            )
        }


    }*/


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
