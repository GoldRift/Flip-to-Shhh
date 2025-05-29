package com.goutham.fliptoshhh

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    private var isToggledOn = false
    private lateinit var sharedPreferences: SharedPreferences
    public

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val toggleShhhSwitch = findViewById<SwitchCompat>(R.id.MainToggle)
        val toggleIndicator = findViewById<TextView>(R.id.StatusIndicator)
        val settingsButton = findViewById<ImageButton>(R.id.SettingsButton)



        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("FlipToShhhPrefs", Context.MODE_PRIVATE)

        // Retrieve saved toggle state and set the textview to it
        isToggledOn = sharedPreferences.getBoolean("isToggledOn", false)
        toggleShhhSwitch.isChecked = isToggledOn

        if (isToggledOn) {
            toggleIndicator.setText(R.string.status_indicator_on)
        } else {
            toggleIndicator.setText(R.string.status_indicator_off)
        }

        toggleShhhSwitch.setOnCheckedChangeListener { _, isChecked ->
            isToggledOn = isChecked

            if (isChecked) {
                // Toggle is on
                startService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText(R.string.status_indicator_on)
            } else {
                // Toggle is off
                stopService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText(R.string.status_indicator_off)
            }

            with(sharedPreferences.edit()) {
                putBoolean("isToggledOn", isToggledOn)
                apply()
            }
        }

        settingsButton.setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}