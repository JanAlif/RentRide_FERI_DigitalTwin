package com.example.poraproject

import android.Manifest
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

        binding.addAccidentReport.setOnClickListener{
            val intent = Intent(this, SimulatedCrashActivity::class.java)
            startActivity(intent)
        }


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
            val intent = Intent(this, SimulatedCrashActivity::class.java).apply {
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
