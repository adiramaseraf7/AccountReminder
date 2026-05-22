package com.ptnw.accountreminder.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.ptnw.accountreminder.MainActivity
import com.ptnw.accountreminder.R
import com.ptnw.accountreminder.data.AccountDatabase
import java.util.concurrent.TimeUnit

class ReminderWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val dao = AccountDatabase.getDatabase(applicationContext).accountDao()
        val now = System.currentTimeMillis()
        val accounts = dao.getAllSync()

        accounts.forEach { account ->
            val daysLeft = ((account.expiredDate - now) / (1000 * 60 * 60 * 24)).toInt()
            if (daysLeft in 0..account.reminderDays) {
                sendNotification(account.name, daysLeft, account.id)
            }
        }
        return Result.success()
    }

    private fun sendNotification(name: String, daysLeft: Int, id: Int) {
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "account_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Pengingat Akun", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, id,
            intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val message = when {
            daysLeft == 0 -> "Akun \"$name\" EXPIRED HARI INI!"
            daysLeft == 1 -> "Akun \"$name\" expired BESOK! Segera extend."
            else          -> "Akun \"$name\" akan expired dalam $daysLeft hari. Segera extend!"
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Pengingat Extend Akun")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()

        manager.notify(id, notification)
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(1, TimeUnit.MINUTES)
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "account_reminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
