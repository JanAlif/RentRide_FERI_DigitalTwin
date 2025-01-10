package com.example.poraproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.poraproject.databinding.LandingActivityBinding

class LandingActivity: AppCompatActivity() {

    private lateinit var binding: LandingActivityBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LandingActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapButton.setOnClickListener{
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }

        binding.accidentReport.setOnClickListener{
            val intent = Intent(this, CrashActivity::class.java)
            startActivity(intent)
        }


    }

}