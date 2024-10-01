package com.goutham.fliptoshhh

import android.os.Bundle
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)

        val toggleShhhButton = findViewById<ToggleButton>(R.id.toggleShhh)

        toggleShhhButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Toggle is on
                Toast.makeText(this, "Toggle is on", Toast.LENGTH_SHORT).show()
                startService(android.content.Intent(this, FlipToShhhService::class.java))
            } else {
                // Toggle is off
                Toast.makeText(this, "Toggle is off", Toast.LENGTH_SHORT).show()
                stopService(android.content.Intent(this, FlipToShhhService::class.java))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}