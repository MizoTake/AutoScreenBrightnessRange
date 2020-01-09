package com.mizotake.autoscreenbrightnessrange

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class BackgroundService : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        p0.also {
            var brightness = brightness
            val (start, end) = rangeValue
            brightness = clamp(brightness, start, end)
            setBrightness(brightness)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(it)
                return null
            }
            startService(it)
        }

        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val name = "通知のタイトル的情報を設定"
        val id = application.packageName
        val notifyDescription = "この通知の詳細情報を設定します"

        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.getNotificationChannel(id) == null
            } else {
                TODO("VERSION.SDK_INT < O")
            }) {
            val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH)
            } else {
                TODO("VERSION.SDK_INT < O")
            }
            channel.apply {
                description = notifyDescription
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this,id).apply {
            setContentTitle("AutoScreenBrightnessRange")
            setContentText("設定した範囲で明るさが調整されます")
            setSmallIcon(R.drawable.ic_launcher_background)
        }.build()

        Thread(
            Runnable {
                (0..5).map {
                    Thread.sleep(1000)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_DETACH)
                }
            }).start()

        startForeground(100, notification)

        return START_STICKY
    }
}