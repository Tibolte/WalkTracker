package fr.northborders.walktracker.domain

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import fr.northborders.walktracker.Constants.ACTION_PAUSE_SERVICE
import fr.northborders.walktracker.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.Constants.NOTIFICATION_CHANNEL_ID
import fr.northborders.walktracker.Constants.NOTIFICATION_CHANNEL_NAME
import fr.northborders.walktracker.Constants.NOTIFICATION_ID
import fr.northborders.walktracker.R
import fr.northborders.walktracker.presentation.MainActivity
import timber.log.Timber

class TrackingService : LifecycleService() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("ACTION_START_OR_RESUME_SERVICE")
                    startForegroundService()
                }
                ACTION_PAUSE_SERVICE -> Timber.d("ACTION_PAUSE_SERVICE")
                ACTION_STOP_SERVICE -> Timber.d("ACTION_STOP_SERVICE")
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForegroundService() {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }

        val activityPendingIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), 0
        )

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false) // The notification doesn't disappear on click
            .setOngoing(true) // Don't swipe away
            .setSmallIcon(R.drawable.ic_launcher_background) // TODO find a logo
            .setContentTitle("Walk in progress") // TODO setContentText with time?
            .setContentIntent(activityPendingIntent)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(notificationManager: NotificationManager) {
        // IMPORTANCE_LOW so the notification doesn't have a sound
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }
}