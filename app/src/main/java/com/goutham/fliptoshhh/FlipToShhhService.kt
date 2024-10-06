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
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.annotation.RequiresApi


class FlipToShhhService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var vibrator: Vibrator
    private lateinit var audioManager: AudioManager
    private var faceDown = false
    private var ringerStatus = 4
    private val deBounceTime = 2000
    private var lastFlipTime: Long = 0

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("FlipToShhhService", "Service started")

        // Gets system audio service
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Gets the system sensor service
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Gets the system vibrator service
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator

        // Gets the ringerStatus before faceDown
        ringerStatus = audioManager.getRingerMode()

        Log.i("Ringer", "Ringer Mode is ${audioManager.getRingerMode()}")

        // Creates notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("flip_to_shhh", "Flip to Shhh", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Builds the notification for running in the background
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = android.app.Notification.Builder(this, "flip_to_shhh")
            .setContentTitle("Flip to Shhh")
            .setContentText("App is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(1, notification)


        // Sets the sensor manager attributes
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_UI)
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // Function to check the sensor orientation and changes the faceDown variable
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Checks the face down status
            val newFaceDownState = x < 2 && x > -2 && y < 2 && y > -2 && z < -9


            val currentTime = System.currentTimeMillis()
            if (newFaceDownState != faceDown && currentTime - lastFlipTime > deBounceTime) {
                faceDown = newFaceDownState
                lastFlipTime = currentTime

                if (faceDown) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                    }, 1000)
                } else {
                    Handler(Looper.getMainLooper()).postDelayed({
                        audioManager.setRingerMode(ringerStatus)
                        Log.i("Ringer", "Ringer changed to ${audioManager.getRingerMode()}")
                    }, 1000)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    // If the service is fully dead it executes these
    override fun onDestroy() {
        Log.d("FlipToShhhService", "Service stopped")

        // Stops the sensor service
        sensorManager.unregisterListener(this)
        stopForeground(true)
        super.onDestroy()
    }

}
