package com.goutham.fliptoshhh

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi


class FlipToShhhService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private val deBounceTime = 1000
    private var originalRingerMode: Int? = null
    private var faceDown = false
    private var lastFlipTime: Long = 0


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FlipToShhhService", "Service started")

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("flip_to_shhh", "Flip to Shhh", NotificationManager.IMPORTANCE_LOW).apply {
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = android.app.Notification.Builder(this, "flip_to_shhh")
            .setContentTitle("Flip to Shhh")
            .setContentText("App is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { accelerometer ->
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val (x, y, z) = event.values
            val isFaceDown = x in -2.0..2.0 && y in -2.0..2.0 && z < -9
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastFlipTime > deBounceTime) {
                lastFlipTime = currentTime
                handleFlipStateChange(isFaceDown)
            }
        }
    }

    private fun handleFlipStateChange(isFaceDown: Boolean) {
        val isDndActive = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

        if (isFaceDown && !faceDown) {
            faceDown = true
            if (!isDndActive && audioManager.ringerMode !in listOf(AudioManager.RINGER_MODE_SILENT, AudioManager.RINGER_MODE_VIBRATE)) {
                originalRingerMode = audioManager.ringerMode
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                Log.i("FlipToShhhService", "Phone flipped face down. Ringer mode set to SILENT.")
            }
        } else if (!isFaceDown && faceDown) {
            faceDown = false
            if (originalRingerMode != null && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT && !isDndActive) {
                audioManager.ringerMode = originalRingerMode!!
                Log.i("FlipToShhhService", "Phone flipped upright. Ringer mode restored to original state: $originalRingerMode")
                originalRingerMode = null
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onDestroy() {
        Log.d("FlipToShhhService", "Service stopped")
        sensorManager.unregisterListener(this)
        stopForeground(true)
        super.onDestroy()
    }

}
