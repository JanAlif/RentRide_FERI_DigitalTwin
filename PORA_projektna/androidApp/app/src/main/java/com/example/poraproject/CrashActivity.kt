package com.example.poraproject


import CrashReport2
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.poraproject.databinding.CrashActivityBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CrashActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: CrashActivityBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false
    private var selectedLocation: LatLng? = null
    private var crashReport: CrashReport2? = null
    private var selectedDate: String = getCurrentDate()
    private var selectedTime: String = getCurrentTime()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CrashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        crashReport = CrashReport2(
            title = intent.getStringExtra("title") ?: "",
            description = intent.getStringExtra("description") ?: "",
            latitude = intent.getDoubleExtra("latitude", 0.0),
            longitude = intent.getDoubleExtra("longitude", 0.0),
            resolvedAddress = intent.getStringExtra("resolvedAddress") ?: "",
            timeOfReport = intent.getStringExtra("timeOfReport") ?: "$selectedDate $selectedTime",
            force = intent.getDoubleExtra("force", 0.0)
        )

        binding.crashTitle.setText(crashReport?.title ?: "")
        binding.crashDescription.setText(crashReport?.description ?: "")
        binding.forceText.setText(crashReport?.force?.toString() ?: "")


        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        // Date Picker
        binding.datePicker.setOnClickListener {
            showDatePicker()
        }

        // Time Picker
        binding.timePicker.setOnClickListener {
            showTimePicker()
        }

        // Submit button functionality
        binding.submitButton.setOnClickListener {
            val updatedTitle = binding.crashTitle.text.toString()
            val updatedDescription = binding.crashDescription.text.toString()
            val forceInput = binding.forceText.text.toString().toDoubleOrNull() ?: 0.0
            val location = selectedLocation ?: LatLng(crashReport?.latitude ?: 0.0, crashReport?.longitude ?: 0.0)

            // Get resolved address (use the suspend function to get address asynchronously)
            resolveAddressUsingGoogleMaps(location.latitude, location.longitude) { resolvedAddress ->
                val updatedCrashReport = CrashReport2(
                    title = updatedTitle,
                    description = updatedDescription,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    resolvedAddress = resolvedAddress,
                    timeOfReport = "$selectedDate $selectedTime",
                    force = forceInput
                )

                // Display the updated CrashReport data
                val message = """
            Title: ${updatedCrashReport.title}
            Description: ${updatedCrashReport.description}
            Latitude: ${updatedCrashReport.latitude}
            Longitude: ${updatedCrashReport.longitude}
            Address: ${updatedCrashReport.resolvedAddress}
            Time: ${updatedCrashReport.timeOfReport}
            Force: ${updatedCrashReport.force}
        """.trimIndent()

                println(message)

                Toast.makeText(this, "Crash Report Submitted:\n$message", Toast.LENGTH_LONG).show()
            }
        }


        // Cancel button functionality
        binding.cancelButton.setOnClickListener {
            finish() // Close the activity
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            binding.datePicker.text = selectedDate
        }, year, month, day).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedTime = String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)
            binding.timePicker.text = selectedTime
        }, hour, minute, true).show()
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    private fun getCurrentTime(): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return timeFormat.format(Date())
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true

        val latitude = crashReport?.latitude ?: 0.0
        val longitude = crashReport?.longitude ?: 0.0

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (latitude != 0.0 && longitude != 0.0) {
            val location = LatLng(latitude, longitude)
            googleMap.addMarker(
                MarkerOptions().position(location).title("Reported Location")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
        }

        // Handle map clicks to select a new location
        googleMap.setOnMapClickListener { location ->
            googleMap.clear()
            googleMap.addMarker(
                MarkerOptions().position(location).title("Selected Location")
            )
            selectedLocation = location
            Toast.makeText(this, "Location updated. Tap 'Submit' to save changes.", Toast.LENGTH_SHORT).show()
        }
    }

    private val okHttpClient = OkHttpClient()

    // Suspend function to resolve address and update UI asynchronously
    private fun resolveAddressUsingGoogleMaps(latitude: Double, longitude: Double, onResolved: (String) -> Unit) {
        val apiKey = getApiKey() ?: return onResolved("API key not found")
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$latitude,$longitude&key=$apiKey"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(url).build()
                val response = okHttpClient.newCall(request).execute()

                val resolvedAddress = if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val results = jsonObject.getJSONArray("results")
                    if (results.length() > 0) {
                        results.getJSONObject(0).getString("formatted_address")
                    } else {
                        "Unknown address"
                    }
                } else {
                    "Error retrieving address"
                }

                // Use withContext to switch back to the main thread and pass the resolved address
                withContext(Dispatchers.Main) {
                    onResolved(resolvedAddress)
                }
            } catch (e: Exception) {
                Log.e("CrashActivity", "Error resolving address", e)
                withContext(Dispatchers.Main) {
                    onResolved("Error retrieving address")
                }
            }
        }
    }

    private fun getApiKey(): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            appInfo.metaData?.getString("com.google.android.geo.API_KEY")
        } catch (e: Exception) {
            Log.e("CrashActivity", "Error fetching API key", e)
            null
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
}
