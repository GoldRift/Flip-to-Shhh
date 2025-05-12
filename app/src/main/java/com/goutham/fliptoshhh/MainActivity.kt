package com.goutham.fliptoshhh

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
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

        // Retrieve saved toggle state and update UI
        isToggledOn = sharedPreferences.getBoolean("isToggledOn", false)
        updateToggleIndicator(toggleIndicator)

        toggleShhhButton.setOnClickListener {
            isToggledOn = !isToggledOn
            if (isToggledOn) {
                startService(android.content.Intent(this, FlipToShhhService::class.java))
            } else {
                stopService(android.content.Intent(this, FlipToShhhService::class.java))
            }
            updateToggleIndicator(toggleIndicator)
            saveToggleState()
        }

        settingsButton.setOnClickListener {
            Toast.makeText(this, "I ain't that fast", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateToggleIndicator(toggleIndicator: TextView) {
        toggleIndicator.text = if (isToggledOn) "ON" else "OFF"
    }

    private fun saveToggleState() {
        with(sharedPreferences.edit()) {
            putBoolean("isToggledOn", isToggledOn)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}