package com.example.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class WakeWordService : Service() {

    private var wakeWordManager: PorcupineWakeWordManager? = null

    companion object {
        const val CHANNEL_ID = "nova_wake_word_channel"
        const val NOTIFICATION_ID = 2001
        const val ACTION_START = "ACTION_START_WAKE_WORD"
        const val ACTION_STOP = "ACTION_STOP_WAKE_WORD"
        const val ACTION_WAKE_DETECTED = "com.example.nova.WAKE_WORD_DETECTED"

        fun start(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, WakeWordService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        wakeWordManager = PorcupineWakeWordManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopWakeWordDetection()
                stopForeground(true)
                stopSelf()
            }
            else -> {
                startForegroundNotification()
                startWakeWordDetection()
            }
        }
        return START_STICKY
    }

    private fun startForegroundNotification() {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NOVA AI Assistant")
            .setContentText("Listening for \"Hey NOVA\" in background...")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            } else {
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun startWakeWordDetection() {
        wakeWordManager?.start { keyword ->
            val broadcastIntent = Intent(ACTION_WAKE_DETECTED).apply {
                putExtra("keyword", keyword)
                setPackage(packageName)
            }
            sendBroadcast(broadcastIntent)
        }
    }

    private fun stopWakeWordDetection() {
        wakeWordManager?.stop()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "NOVA Wake Word Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background listening for NOVA wake word"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        stopWakeWordDetection()
        wakeWordManager?.destroy()
        wakeWordManager = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
