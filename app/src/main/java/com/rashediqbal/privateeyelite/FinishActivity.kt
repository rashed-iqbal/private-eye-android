package com.rashediqbal.privateeyelite

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class FinishActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val sessionManager = SessionManager(this)
        val isLogin = sessionManager.checkLogin()


        findViewById<TextView>(R.id.finishBtn).setOnClickListener {
            if (isLogin){
                val processUtils = ProcessUtils(this)
                processUtils.backgroundProcess()
            }
            startActivity(Intent(this@FinishActivity,MainActivity::class.java))
            finish()
        }

    }
}