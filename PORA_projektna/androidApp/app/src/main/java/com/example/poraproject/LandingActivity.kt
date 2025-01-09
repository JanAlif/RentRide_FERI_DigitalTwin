package com.example.poraproject

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.poraproject.MapActivity.Companion.NOTIFICATION_PERMISSION_REQUEST_CODE
import com.example.poraproject.databinding.LandingActivityBinding
import com.example.poraproject.databinding.MapActivityBinding

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