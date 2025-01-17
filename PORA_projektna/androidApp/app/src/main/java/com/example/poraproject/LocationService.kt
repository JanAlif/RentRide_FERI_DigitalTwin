package com.example.poraproject

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastSpeed: Float = 0f

    private val MQTT_BROKER_URL = "tcp://10.0.2.2:1883" // Replace with your broker's IP
    private val MQTT_TOPIC = "car/put"
    private val MQTT_CLIENT_ID = "CrashActivityClient"
    private lateinit var mqttClient: MqttClient

    override fun onCreate() {
        super.onCreate()

        // Start the foreground service
        val notification = createNotification("Tracking location...")
        startForeground(1, notification)
        try {
            mqttClient = MqttClient(MQTT_BROKER_URL, MQTT_CLIENT_ID, MemoryPersistence())
            mqttClient.connect()
            Toast.makeText(this, "Connected to MQTT Broker", Toast.LENGTH_SHORT).show()
        } catch (e: MqttException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to connect to MQTT Broker", Toast.LENGTH_LONG).show()
        }
        Notifications.createNotificationChannel(this)
        // Initialize location updates
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val locationRequest = LocationRequest.create().apply {
            interval = 1000 // 1 second
            fastestInterval = 500 // Fastest updates
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        // Handle location updates
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                if (locationResult.locations.isNotEmpty()) {
                    val location = locationResult.lastLocation
                    val speedMetersPerSecond = location?.speed
                    val speedKmph = speedMetersPerSecond?.times(3.6f)
                    val force = 1500*(lastSpeed-speedKmph!!)
                    if (force > 1000){
                        val payload = JSONObject().apply {
                            put("title", "Crash Detected")
                            put("description", "Crash was detected at this location")
                            put("latitude", location.latitude)
                            put("longitude", location.longitude)
                            put("timeOfReport", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()).toString()+" "+SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date()).toString())
                            put("force", force)
                        }.toString()

                        try {
                            val mqttMessage = MqttMessage(payload.toByteArray())
                            mqttMessage.qos = 1
                            mqttClient.publish(MQTT_TOPIC, mqttMessage)
                            Notifications.sendNotification(
                                context = this@LocationService,
                                imageId = android.R.drawable.ic_dialog_info,
                                title = "Crash Detected",
                                content = "A simulated crash condition was detected. Detecte force was $force N",
                                itemId = "simulated_crash"
                            )
                            Log.d("LocationService", "Crash was reported with force: ${force} km/h")
                        } catch (e: MqttException) {
                            e.printStackTrace()
                            Log.d("LocationService", "Crash could not be reported")
                        }
                    }

                    lastSpeed = speedKmph!!;
                    Log.d("LocationService", "Speed: ${"%.2f".format(speedKmph)} km/h  Force: ${force} N")
                }
            }
        }

        // Start location updates
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove location updates when service stops
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(contentText: String): Notification {
        val channelId = "location_service_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_stat_name) // Replace with your app's icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}