package com.goutham.fliptoshhh

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private var isToggledOn = false
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val toggleShhhButton = findViewById<ExtendedFloatingActionButton>(R.id.toggleShhh)
        val toggleIndicator = findViewById<TextView>(R.id.toggleIndicator)
        val settingsButton = findViewById<FloatingActionButton>(R.id.settings)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FlipToShhhPrefs", Context.MODE_PRIVATE)

        // Retrieve saved toggle state and set the textview to it
        isToggledOn = sharedPreferences.getBoolean("isToggledOn", false)

        if (isToggledOn) {
            toggleIndicator.setText("ON")
        } else {
            toggleIndicator.setText("OFF")
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (!notificationManager.isNotificationPolicyAccessGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
            startActivity(intent)
        }



        toggleShhhButton.setOnClickListener {
            if (!isToggledOn) {
                // Toggle is on
                startService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText("ON")
                isToggledOn = true
            } else {
                // Toggle is off
                stopService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText("OFF")
                isToggledOn = false
            }

            with(sharedPreferences.edit()) {
                putBoolean("isToggledOn", isToggledOn)
                apply()
            }
        }

        settingsButton.setOnClickListener {
            Toast.makeText(this,"I ain't that fast", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}