package com.rashediqbal.privateeyelite

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class BackgroundProcess(context: Context, workerParams: WorkerParameters) :Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}