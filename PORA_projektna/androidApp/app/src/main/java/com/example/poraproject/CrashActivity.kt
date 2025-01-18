package com.example.poraproject

import CrashReport2
import android.content.Context
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
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject

class CrashActivity: AppCompatActivity(), OnMapReadyCallback {


    private lateinit var binding: CrashActivityBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var isMapReady = false
    private var selectedLocation: LatLng? = null
    private var crashReport: CrashReport2? = null

    private var selectedDate: String = SimulatedCrashActivity().getCurrentDate()
    private var selectedTime: String = SimulatedCrashActivity().getCurrentTime()


    private val MQTT_BROKER_URL = "tcp://10.0.2.2:1883" // Replace with your broker's IP
    private val MQTT_TOPIC = "crash/put"
    private val MQTT_CLIENT_ID = "CrashActivityClient"
    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CrashActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        var username = sharedPreferences.getString("username", null)

        try {
            mqttClient = MqttClient(MQTT_BROKER_URL, MQTT_CLIENT_ID, MemoryPersistence())
            mqttClient.connect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }

        // Initialize CrashReport data
        crashReport = CrashReport2(
            title = intent.getStringExtra("title") ?: "",
            description = intent.getStringExtra("description") ?: "",
            latitude = intent.getDoubleExtra("latitude", 0.0),
            longitude = intent.getDoubleExtra("longitude", 0.0),
            resolvedAddress = intent.getStringExtra("resolvedAddress") ?: "",
            user = username ?: "",
            timeOfReport = intent.getStringExtra("timeOfReport") ?: "$selectedDate $selectedTime",
            force = intent.getDoubleExtra("force", 0.0)
        )

        binding.crashTitle.setText(crashReport?.title ?: "")
        binding.crashDescription.setText(crashReport?.description ?: "")

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)


        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.submitButton.setOnClickListener {
            val updatedTitle = binding.crashTitle.text.toString()
            val updatedDescription = binding.crashDescription.text.toString()
            val location = selectedLocation ?: LatLng(crashReport?.latitude ?: 0.0, crashReport?.longitude ?: 0.0)

            resolveAddressUsingGoogleMaps(location.latitude, location.longitude) { resolvedAddress ->
                val updatedCrashReport = CrashReport2(
                    title = updatedTitle,
                    description = updatedDescription,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    resolvedAddress = resolvedAddress,
                    user = username ?: "",
                    timeOfReport = "$selectedDate $selectedTime"
                )

                val payload = JSONObject().apply {
                    put("title", updatedCrashReport.title)
                    put("description", updatedCrashReport.description)
                    put("latitude", updatedCrashReport.latitude)
                    put("longitude", updatedCrashReport.longitude)
                    put("user", updatedCrashReport.user)
                    put("timeOfReport", updatedCrashReport.timeOfReport)
                }.toString()

                try {
                    val mqttMessage = MqttMessage(payload.toByteArray())
                    mqttMessage.qos = 1
                    mqttClient.publish(MQTT_TOPIC, mqttMessage)
                    Toast.makeText(this, "Crash report sent to MQTT broker.", Toast.LENGTH_LONG).show()
                    finish()
                } catch (e: MqttException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to send data to MQTT broker.", Toast.LENGTH_LONG).show()
                }
            }
        }



    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        isMapReady = true

        val latitude = crashReport?.latitude ?: 0.0
        val longitude = crashReport?.longitude ?: 0.0

        // Enable zoom controls
        // Disable all gestures...
        googleMap.uiSettings.setAllGesturesEnabled(false)
        // ... then enable only zoom gestures and controls.
        googleMap.uiSettings.isZoomGesturesEnabled = true
        googleMap.uiSettings.isZoomControlsEnabled = true

        if (latitude != 0.0 && longitude != 0.0) {
            val location = LatLng(latitude, longitude)
            googleMap.addMarker(
                MarkerOptions().position(location).title("Reported Location")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f))
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
