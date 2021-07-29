package com.rashediqbal.privateeyelite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class BackgroundProcess(context: Context, workerParams: WorkerParameters) :Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        val sessionManager = SessionManager(applicationContext)
        val data = sessionManager.getUser()
        val db = Firebase.firestore
        db.collection("target_users").document(data["target"]!!).update("last_update",getCurrentTime())
        return Result.success()
    }

    private fun getCurrentTime(): String {
        val simpleDateFormat = SimpleDateFormat("yyyy.MMMM.dd GGG hh:mm aaa")
        return simpleDateFormat.format(Date())
    }
}