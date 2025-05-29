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
import androidx.compose.ui.input.key.type
import androidx.preference.PreferenceManager
import java.util.prefs.Preferences


class FlipToShhhService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var audioManager: AudioManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var vibrator: Vibrator
    private val deBounceTime = 1000 // Minimum time between flips to register as a new event
    private var originalRingerMode: Int? = null // Stores the ringer mode before it was changed
    private var faceDown = false // Tracks if the phone is currently face down
    private var lastFlipTime: Long = 0 // Timestamp of the last detected flip


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // This method is called when the service is started.
    // It initializes system services, creates a notification channel,
    // starts the service in the foreground, and registers the accelerometer sensor listener.
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

        // TODO: Add a check for Do Not Disturb access in main activity instead
        // Checks if the app has Do Not Disturb access and prompts the user if not.
//        if (!notificationManager.isNotificationPolicyAccessGranted) {
//            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
//            startActivity(intent)
//        }

        // Creates notification channel for Android Oreo and above.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel("flip_to_shhh", "Flip to Shhh", NotificationManager.IMPORTANCE_LOW)
            channel.setShowBadge(false) // Hides the notification badge on the app icon
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        // Builds the notification for running the service in the foreground.
        // This is required for services that need to run for an extended period.
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = android.app.Notification.Builder(this, "flip_to_shhh")
            .setContentTitle("Flip to Shhh")
            .setContentText("App is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setNumber(0) // Hides the notification count
            .build()
        startForeground(1, notification) // Starts the service in the foreground with the notification


        // Sets the sensor manager attributes to listen for accelerometer events.
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL // Specifies the rate at which sensor events are delivered
            )
        }

        return super.onStartCommand(intent, flags, startId)
    }

    // This method is called when sensor values change.
    // It detects if the phone is flipped face down or face up and changes the ringer mode accordingly.
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Determines if the phone is face down based on accelerometer readings.
            val isFaceDown = x < 2 && x > -2 && y < 2 && y > -2 && z < -9
            val currentTime = System.currentTimeMillis() // Gets current time for debounce logic

            // Check if Do Not Disturb (DND) mode is currently active.
            val isDndActive = notificationManager.currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

            // Get user preferences for ringer mode and vibration.
            val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val sHHHMode = prefs.getString("sHHHMode", "0") // "0" for silent, "1" for vibrate
            val vibrationEnabled = prefs.getBoolean("vibrationSetting", true)

            // Logic for when the phone is flipped face down.
            if (isFaceDown && !faceDown && currentTime - lastFlipTime > deBounceTime) {
                faceDown = true
                lastFlipTime = currentTime

                // Change ringer mode only if DND is not active and phone is not already silent or vibrate.
                if (!isDndActive && audioManager.ringerMode != AudioManager.RINGER_MODE_SILENT &&
                    audioManager.ringerMode != AudioManager.RINGER_MODE_VIBRATE) {
                    originalRingerMode = audioManager.ringerMode // Store the current ringer mode

                    // Sets ringer mode based on user preference.
                    if (sHHHMode == "0") {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT)
                        Log.i("FlipToShhhService", "Phone flipped face down. Ringer mode set to SILENT.")
                    } else {
                        audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE)
                        Log.i("FlipToShhhService", "Phone flipped face down. Ringer mode set to VIBRATE.")
                    }

                    // Vibrate if the setting is enabled.
                    if (vibrationEnabled) {
                        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                // Logic for when the phone is flipped face up.
            } else if (!isFaceDown && faceDown && currentTime - lastFlipTime > deBounceTime) {
                faceDown = false
                lastFlipTime = currentTime

                // Restore original ringer mode if it was changed and DND is not active.
                if (originalRingerMode != null && (audioManager.ringerMode == AudioManager.RINGER_MODE_SILENT || audioManager.ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
                    if (!isDndActive) { // Only restore if DND is not active
                        audioManager.setRingerMode(originalRingerMode!!)

                        // Vibrate if the setting is enabled.
                        if (vibrationEnabled) {
                            vibrator.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
                        }

                        Log.i("FlipToShhhService", "Phone flipped upright. Ringer mode restored to original state: $originalRingerMode")
                        originalRingerMode = null // Reset stored ringer mode after restoring
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed here for this application
    }

    // This method is called when the service is being destroyed.
    // It unregisters the sensor listener and stops the foreground service.
    override fun onDestroy() {
        Log.d("FlipToShhhService", "Service stopped")

        // Stops listening to sensor events to save battery.
        sensorManager.unregisterListener(this)
        // Removes the foreground notification when the service is destroyed.
        // For Android 13 (API 33) and above, use stopForeground(STOP_FOREGROUND_REMOVE).
        // For older versions, stopForeground(true) is used.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) { // `STOP_FOREGROUND_REMOVE` is available from API 24
            stopForeground(Service.STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION") // Suppress deprecation for older Android versions
            stopForeground(true)
        }
        super.onDestroy()
    }

}