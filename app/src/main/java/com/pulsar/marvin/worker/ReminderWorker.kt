package com.pulsar.marvin.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pulsar.marvin.MainActivity
import com.pulsar.marvin.R

class ReminderWorker(
  context: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    showNotification()
    return Result.success()
  }

  private fun showNotification() {
    val notificationManager =
      applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    val channelId = "daily_reminder_channel"
    val channel = NotificationChannel(
      channelId,
      "Daily Reminder",
      NotificationManager.IMPORTANCE_DEFAULT
    )
    notificationManager.createNotificationChannel(channel)

    val intent = Intent(applicationContext, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(applicationContext, channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle("Marvin")
      .setContentText("Time to track your goal!")
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)
      .build()

    notificationManager.notify(1, notification)
  }
}
