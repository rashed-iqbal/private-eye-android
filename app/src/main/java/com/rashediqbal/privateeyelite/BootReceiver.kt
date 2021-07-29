package com.rashediqbal.privateeyelite

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == Intent.ACTION_BOOT_COMPLETED) {
            val sessionManager = SessionManager(context!!)
            val isLogin = sessionManager.checkLogin()
            if (isLogin){
                val processUtils = ProcessUtils(context)
                processUtils.backgroundProcess()
            }

        }
    }
}