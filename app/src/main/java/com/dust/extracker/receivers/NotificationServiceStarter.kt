package com.dust.extracker.receivers

import android.app.ActivityManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.dust.extracker.services.NotificationJobService

class NotificationServiceStarter : BroadcastReceiver() {

    private val jobServiceId = 5005
    private val tag = "NotificationServiceStarter"

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { con ->
            startNotificationJobService(con)
        }
    }

    private fun startNotificationJobService(context: Context) {
        Log.i(tag, "starting service from receiver")
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

        val cName = ComponentName(context.applicationContext, NotificationJobService::class.java)
        val info = JobInfo.Builder(jobServiceId, cName)
            .setPersisted(true)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .build()
        scheduler.schedule(info)

    }

    private fun checkServiceIsRunning(context: Context): Boolean {
        try {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            manager.getRunningServices(Int.MAX_VALUE).forEach {
                if (it.service.className == NotificationJobService::class.java.name)
                    return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

}