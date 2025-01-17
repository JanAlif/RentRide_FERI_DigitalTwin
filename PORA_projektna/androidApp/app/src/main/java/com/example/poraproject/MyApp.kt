package com.example.poraproject

import android.app.Application

class MyApp: Application() {

    override fun onCreate() {
        super.onCreate()

        // Create the notification channel
        //Notifications.createNotificationChannel(this)
        println("Calling createNotificationChannel")

        // Start monitoring for crash conditions
        //monitorCrashCondition()
    }

    fun monitorCrashCondition(force: Float) {

        // Send a notification when the condition is met
        Notifications.sendNotification(
            context = this,
            imageId = android.R.drawable.ic_dialog_info,
            title = "Crash Detected",
            content = "A simulated crash condition was detected. Detecte force was $force N",
            itemId = "simulated_crash"
        )
    }


}