package com.rashediqbal.privateeyelite

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class BackgroundReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val sessionManager = SessionManager(context!!)
        val data = sessionManager.getUser()

        val db = Firebase.firestore

        val mRequest = NetworkRequest.Builder().build()
        val mCM = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val mCallback = object :ConnectivityManager.NetworkCallback(){
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                db.collection("target_users").document(data["target"]!!).update("last_update",getCurrentTime())
                    .addOnSuccessListener {
                        Toast.makeText(context, "Last Updated Successfully", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        mCM.registerNetworkCallback(mRequest,mCallback)

        Toast.makeText(context, "Background Triggered!", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("SimpleDateFormat")
    private fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z")
        return simpleDateFormat.format(Date())
    }
}