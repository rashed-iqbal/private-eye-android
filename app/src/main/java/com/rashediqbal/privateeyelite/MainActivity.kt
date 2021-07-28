package com.rashediqbal.privateeyelite

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val session = SessionManager(this)

        Log.d("MySession","${session.checkLogin()} ${session.getUser()}")

        when {
            session.checkLogin() -> {
                TODO("Handle if user logged in")
            }

            session.checkCredential() -> {

                startActivity(Intent(this,PermissionActivity::class.java))
                finish()

            }

            else -> {
                startActivity(Intent(this,WelcomeActivity::class.java))
                finish()

            }
        }



    }
}