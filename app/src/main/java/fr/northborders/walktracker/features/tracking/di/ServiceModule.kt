package fr.northborders.walktracker.features.tracking.di

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import fr.northborders.walktracker.core.util.Constants
import fr.northborders.walktracker.R
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.features.photos.domain.GetPhotosForLocation
import fr.northborders.walktracker.features.photos.domain.PhotoRepository
import fr.northborders.walktracker.MainActivity

@Module
@InstallIn(ServiceComponent::class)
class ServiceModule {
//
//    @ServiceScoped
//    @Provides
//    fun provideFusedLocationProviderClient(
//        @ApplicationContext app: Context
//    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    ): PendingIntent = PendingIntent.getActivity(
        app, 0, Intent(app, MainActivity::class.java), 0
    )

    @ServiceScoped
    @Provides
    fun provideNotificationManager(
        @ApplicationContext app: Context
    ) = app.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    @ServiceScoped
    @Provides
    fun provideNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, Constants.NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false) // The notification doesn't disappear on click
        .setOngoing(true) // Don't swipe away
        .setSmallIcon(R.drawable.ic_walk)
        .setContentTitle(app.getString(R.string.walk_in_progress))
        .setContentText("00:00:00")
        .setContentIntent(pendingIntent)

    @ServiceScoped
    @Provides
    fun provideNotificationObserver(
        notificationManager: NotificationManager,
        notificationBuilder: NotificationCompat.Builder
    ) = Observer<Long> {
        val notification = notificationBuilder
            .setContentText(Utils.getFormattedStopWatchTime(it * 1000L))
        notificationManager.notify(Constants.NOTIFICATION_ID, notification.build())
    }

    @ServiceScoped
    @Provides
    fun provideGetPhotosForLocation(photoRepository: PhotoRepository) = GetPhotosForLocation(photoRepository)
}