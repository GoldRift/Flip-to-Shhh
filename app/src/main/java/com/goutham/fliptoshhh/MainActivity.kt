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

    // Called when the activity is first created.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val toggleShhhSwitch = findViewById<SwitchCompat>(R.id.MainToggle)
        val toggleIndicator = findViewById<TextView>(R.id.StatusIndicator)
        val settingsButton = findViewById<ImageButton>(R.id.SettingsButton)
        modeIndicator = findViewById<TextView>(R.id.ModeIndicator)



        // Initialize SharedPreferences for storing app preferences.
        sharedPreferences = getSharedPreferences("FlipToShhhPrefs", Context.MODE_PRIVATE)

        // Retrieve the saved toggle state and update the switch and text view accordingly.
        isToggledOn = sharedPreferences.getBoolean("isToggledOn", false)
        toggleShhhSwitch.isChecked = isToggledOn

        // Update the toggle indicator text based on the current toggle state.
        if (isToggledOn) {
            toggleIndicator.setText(R.string.status_indicator_on)
        } else {
            toggleIndicator.setText(R.string.status_indicator_off)
        }

        // Set a listener for the toggle switch to respond to state changes.
        toggleShhhSwitch.setOnCheckedChangeListener { _, isChecked ->
            isToggledOn = isChecked

            if (isChecked) {
                // Toggle is on: Start the service and update the indicator text.
                startService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText(R.string.status_indicator_on)
            } else {
                // Toggle is off: Stop the service and update the indicator text.
                stopService(android.content.Intent(this, FlipToShhhService::class.java))
                toggleIndicator.setText(R.string.status_indicator_off)
            }

            // Save the new toggle state to SharedPreferences.
            with(sharedPreferences.edit()) {
                putBoolean("isToggledOn", isToggledOn)
                apply() // Apply changes asynchronously.
            }
        }

        // Set a click listener for the settings button to open the SettingsActivity.
        settingsButton.setOnClickListener {
            val intent = android.content.Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    // Called when the activity will start interacting with the user.
    override fun onResume() {
        super.onResume()
        // Get the default SharedPreferences.
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        // Retrieve the "sHHHMode" preference, defaulting to "0" (Silent).
        val sHHHMode = prefs.getString("sHHHMode", "0")

        // Update the mode indicator text based on the retrieved preference.
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