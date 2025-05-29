package com.goutham.fliptoshhh

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceManager

class MainActivity : AppCompatActivity() {

    private var isToggledOn = false
    private var modeIndicator: TextView? = null
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val toggleShhhSwitch = findViewById<SwitchCompat>(R.id.MainToggle)
        val toggleIndicator = findViewById<TextView>(R.id.StatusIndicator)
        val settingsButton = findViewById<ImageButton>(R.id.SettingsButton)
        modeIndicator = findViewById<TextView>(R.id.ModeIndicator)



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

    override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val sHHHMode = prefs.getString("sHHHMode", "0")

        if (sHHHMode == "0") {
            modeIndicator?.setText(R.string.mode_indicator_silent)
        } else {
            modeIndicator?.setText(R.string.mode_indicator_vibrate)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
    }
}