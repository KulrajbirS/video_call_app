package com.example.video_call_app

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.app.ActivityCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {

    val permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO)
    val requestcode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.loginbtn)

        if (!isPermissionGrantes()) {
            askPermission()
        }

        Firebase.initialize(this)

        button.setOnClickListener {
            val username = R.id.usernameEdit
            val intent = Intent(this, CallActivity::class.java)
            intent.putExtra("username", username)
            startActivity(intent)
        }

    }

    private fun askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestcode)
    }

    private fun isPermissionGrantes(): Boolean {

        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED)
                return false
        }

        return true
    }
}