package com.rashediqbal.privateeyelite

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ProcessUtils (private val context: Context) {

        fun backgroundProcess() {
            val constrains = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicWork =
                PeriodicWorkRequest.Builder(BackgroundProcess::class.java, 15, TimeUnit.MINUTES)
                    .addTag("BACKGROUND_PROCESS")
                    .setConstraints(constrains)
                    .build()

            val workManager = WorkManager.getInstance(context)
            workManager.enqueue(periodicWork)
        }

}