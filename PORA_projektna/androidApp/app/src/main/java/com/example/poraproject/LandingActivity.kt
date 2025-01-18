package com.example.poraproject

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var username = sharedPreferences.getString("username", null)
        if (username.isNullOrBlank()) {
            showNameInputDialog()
        }

        // Map button click listener
        binding.addCrashButton.setOnClickListener {
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        binding.viewCrashButton.setOnClickListener {
            val intent = Intent(this, CrashMapActivity::class.java)
            startActivity(intent)
        }

        binding.simulateCrashButton.setOnClickListener {
            val intent = Intent(this, SimulatedCrashActivity::class.java)
            startActivity(intent)
        }

        binding.usernameButton.setOnClickListener {
            username = sharedPreferences.getString("username", null)
            showNameInputDialog(username)
        }

        // Accident report button click listener
        /*binding.accidentReport.setOnClickListener {
            val intent = Intent(this, SimulatedCrashActivity::class.java)
            startActivity(intent)
        }*/
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

    private fun showNameInputDialog(username: String? = "") {
        val dialogView = layoutInflater.inflate(R.layout.nameinput, null)
        val inputField = dialogView.findViewById<EditText>(R.id.name_input)
        val okButton = dialogView.findViewById<Button>(R.id.ok_button)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancel_button)

        inputField.setText(username)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        okButton.setOnClickListener {
            val newUserame = inputField.text.toString().trim()
            if (newUserame.isNotEmpty()) {
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()

                editor.putString("username", newUserame)
                editor.apply()

                dialog.dismiss()
            } else {
                Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}