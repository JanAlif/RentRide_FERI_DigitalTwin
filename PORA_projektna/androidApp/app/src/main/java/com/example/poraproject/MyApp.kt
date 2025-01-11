package com.example.poraproject

import android.app.Application

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel
        Notifications.createNotificationChannel(this)
        println("Calling createNotificationChannel")

        // Start monitoring for crash conditions
        monitorCrashCondition()
    }

    private fun monitorCrashCondition() {
        // Example condition
        val crashCondition = 1 > 0 // Replace this with your actual condition

        if (crashCondition) {
            println("Crash condition met. Sending notification.")

            // Send a notification when the condition is met
            Notifications.sendNotification(
                context = this,
                imageId = android.R.drawable.ic_dialog_info,
                title = "Crash Detected",
                content = "A simulated crash condition was detected.",
                itemId = "simulated_crash"
            )
        } else {
            println("No crash detected.")
        }
    }


}