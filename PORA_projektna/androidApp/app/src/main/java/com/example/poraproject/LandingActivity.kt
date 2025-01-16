package com.example.poraproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poraproject.databinding.LandingActivityBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: LandingActivityBinding

    // Permissions required for location tracking
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION
    )
    private val permissionRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check for permissions and start the service if granted
        if (hasAllPermissions()) {
            startLocationService()
        } else {
            requestPermissions()
        }

        // Map button click listener
        binding.mapButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        // Accident report button click listener
        binding.accidentReport.setOnClickListener {
            val intent = Intent(this, SimulatedCrashActivity::class.java)
            startActivity(intent)
        }
    }

    // Function to check if all required permissions are granted
    private fun hasAllPermissions(): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Function to request missing permissions
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, requiredPermissions, permissionRequestCode)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startLocationService()
            } else {
                // Handle permission denial (optional)
                println("Permissions denied. Location tracking cannot start.")
            }
        }
    }

    // Function to start LocationService
    private fun startLocationService() {
        val serviceIntent = Intent(this, LocationService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent) // For Android 8.0+ (API 26+)
        } else {
            startService(serviceIntent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Optionally stop the service if it is no longer needed
        val serviceIntent = Intent(this, LocationService::class.java)
        stopService(serviceIntent)
    }
}