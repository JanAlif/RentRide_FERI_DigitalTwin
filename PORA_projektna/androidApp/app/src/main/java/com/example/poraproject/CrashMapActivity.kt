package com.example.poraproject

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poraproject.databinding.ActivityCrashMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONArray
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CrashMapActivity : AppCompatActivity(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1002
        const val REQUEST_CODE_CRASH_ACTIVITY = 2001
        const val COLLECTION_NAME = "trafficaccidents"
    }

    private lateinit var binding: ActivityCrashMapBinding
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private var isReturningFromCrashActivity = false
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val MQTT_BROKER_URL = "tcp://10.0.2.2:1883" // Replace with your broker's IP
    private val MQTT_TOPIC = "crash/get"
    private val MQTT_CLIENT_ID = "CrashActivityClient"
    private lateinit var mqttClient: MqttClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrashMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mqttClient = MqttClient(MQTT_BROKER_URL, MQTT_CLIENT_ID, MemoryPersistence())

        mapView = binding.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.addAccidentReport.setOnClickListener{
            finish()
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

        setupMqttClient()
        // Initial location
        val maribor = LatLng(46.5547, 15.6459)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(maribor, 10f))
        googleMap.uiSettings.isZoomControlsEnabled = true


    }

    // MQTT setup: connect, set callbacks, subscribe, and request crash data.
    private fun setupMqttClient() {

        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
        }

        mqttClient.setCallback(object : MqttCallbackExtended {
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {

                try {
                    mqttClient.subscribe("crash/response", 1)
                } catch (e: MqttException) {
                    e.printStackTrace()
                }
                // Also send a request for crash data.
                publishCrashRequest()
            }

            override fun connectionLost(cause: Throwable?) {
                Toast.makeText(this@CrashMapActivity, "MQTT Connection Lost", Toast.LENGTH_SHORT).show()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                // Received a message from the broker.
                val payload = message?.toString() ?: ""
                if (topic == "crash/response") {
                    runOnUiThread {
                        updateMapWithCrashData(payload)
                    }
                }
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                // Called when message delivery is complete (for published messages).
            }
        })
        try {
            mqttClient.connect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    // Publish a message to request crash data.
    private fun publishCrashRequest() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // Parse the date string into a Date object
        val date = Date()

        // Use Calendar to subtract one day
        val dateBefore = Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_MONTH, -1) // subtract 1 day
        }

        val payload = dateFormat.format(dateBefore.time)
        Log.d("MQTT", "Publishing: $payload")
        try {
            val mqttMessage = MqttMessage(payload.toByteArray())
            mqttMessage.qos = 1
            mqttClient.publish(MQTT_TOPIC, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    private fun updateMapWithCrashData(data: String) {
        // Clear existing markers (if you want to refresh the map)
        googleMap.clear()

        Log.d("MQTT", "Received: $data")
        try {
            // Parse the received JSON array string
            val jsonArray = JSONArray(data)

            // Iterate over each accident in the array
            for (i in 0 until jsonArray.length()) {
                val accident = jsonArray.getJSONObject(i)
                Log.d("MQTT", "Received: $accident")

                // Extract the nested coordinates array:
                val coordinatesObject = accident.getJSONObject("coordinates")
                val coordsArray = coordinatesObject.getJSONArray("coordinates")
                val longitude = coordsArray.getDouble(0) // first element
                val latitude = coordsArray.getDouble(1)  // second element

                // Get the other accident details
                val title = accident.getString("title")
                val createdAt = accident.getString("createdAt")
                val user = accident.getString("user")

                // Create a LatLng position from the coordinates
                val position = LatLng(latitude, longitude)

                val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val outputDateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getDefault()
                }


                val formattedTime = try {
                    val parsedDate = inputDateFormat.parse(createdAt)
                    if (parsedDate != null) outputDateFormat.format(parsedDate) else createdAt
                } catch (e: Exception) {
                    e.printStackTrace()
                    createdAt // fallback to original if parsing fails
                }

                // Configure a MarkerOptions with a title and snippet
                // The snippet contains the time and user info
                val markerOptions = MarkerOptions()
                    .position(position)
                    .title(title)
                    .snippet("Time: $formattedTime\nUser: $user")

                // Add the marker to the map
                googleMap.addMarker(markerOptions)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            Toast.makeText(this, "Error parsing crash data", Toast.LENGTH_SHORT).show()
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
