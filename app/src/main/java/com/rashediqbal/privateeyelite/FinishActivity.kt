package com.rashediqbal.privateeyelite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class FinishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)


        findViewById<TextView>(R.id.finishBtn).setOnClickListener {
            startActivity(Intent(this@FinishActivity,MainActivity::class.java))
            finish()
        }

    }
}