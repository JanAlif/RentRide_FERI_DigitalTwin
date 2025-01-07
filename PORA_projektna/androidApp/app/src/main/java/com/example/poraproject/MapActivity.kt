package com.example.poraproject

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poraproject.databinding.MapActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/*

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val REQUEST_CODE_CRASH_ACTIVITY = 2001
    }



    private lateinit var binding: MapActivityBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

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
            startActivity(intent)
        }


    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_CRASH_ACTIVITY) {
            if (resultCode == RESULT_CANCELED) {
                // Clear all markers from the map when the user cancels the CrashActivity
                googleMap.clear()
                Toast.makeText(this, "CrashActivity canceled, markers cleared.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}*/
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
}
