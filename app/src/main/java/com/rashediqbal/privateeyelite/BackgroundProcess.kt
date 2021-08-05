package com.rashediqbal.privateeyelite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class BackgroundProcess(context: Context, workerParams: WorkerParameters) : Worker(
    context,
    workerParams
) {
    override fun doWork(): Result {

        try {
            val savePhoneData = SavePhoneData(applicationContext)
            savePhoneData.saveData()
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

}