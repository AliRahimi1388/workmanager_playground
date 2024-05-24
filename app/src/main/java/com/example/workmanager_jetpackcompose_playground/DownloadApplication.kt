package com.example.workmanager_jetpackcompose_playground

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class DownloadApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            "download_channel",
            "File download",
            NotificationManager.IMPORTANCE_HIGH
        )

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

    }
}