package com.rashediqbal.privateeyelite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<ImageButton>(R.id.welcomeBtn).setOnClickListener {
            startActivity(Intent(this,AuthActivity::class.java))
        }
    }
}