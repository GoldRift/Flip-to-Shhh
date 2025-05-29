package com.goutham.fliptoshhh

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.preference.PreferenceManager
import java.util.prefs.Preferences


class FlipToShhhService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator
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

        // Gets system audio service
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Gets the system sensor service
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Gets the notification manager service
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Gets the vibrator service
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }

        // Creates notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("flip_to_shhh", "Flip to Shhh", NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false)
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
            .setNumber(0)
            .build()
        startForeground(1, notification)


        // Sets the sensor manager attributes
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL)
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

            val isFaceDown = x < 2 && x > -2 && y < 2 && y > -2 && z < -9 // Checks the face down status
            val currentTime = System.currentTimeMillis() // Gets time for debounce time

            // Check if DND is active
            val isDndActive = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

            // Get ringer mode preference
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val sHHHMode = prefs.getString("sHHHMode", "0")
            val vibrationEnabled = prefs.getBoolean("vibrationSetting", true)

            if (isFaceDown && !faceDown && currentTime - lastFlipTime > deBounceTime) {
                faceDown = true
                lastFlipTime = currentTime

                if (!isDndActive && audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT &&
                    audioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                    originalRingerMode = audioManager.ringerMode // Store the current mode before changing it

                    // Based on user preference changes the ringer mode variable according
                    if (sHHHMode == "0") {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                        Log.i("FlipToShhhService", "Phone flipped face down. Ringer mode set to SILENT.")
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                        Log.i("FlipToShhhService", "Phone flipped face down. Ringer mode set to VIBRATE.")
                    }

                    if (vibrationEnabled) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
            } else if (!isFaceDown && faceDown && currentTime - lastFlipTime > deBounceTime) {
                faceDown = false
                lastFlipTime = currentTime

                if (originalRingerMode != null && audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT || audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE) {
                    if (!isDndActive) { // Only restore if DND is not active
                        audioManager.setRingerMode(originalRingerMode!!)

                        if (vibrationEnabled) {
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                        }

                        Log.i("FlipToShhhService", "Phone flipped upright. Ringer mode restored to original state: $originalRingerMode")
                        originalRingerMode = null // Reset after restoring
                    }
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
        STOP_FOREGROUND_REMOVE
        super.onDestroy()
    }

}
