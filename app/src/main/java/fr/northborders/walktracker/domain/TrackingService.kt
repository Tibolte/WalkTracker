package fr.northborders.walktracker.domain

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.Constants.ACTION_PAUSE_SERVICE
import fr.northborders.walktracker.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.Constants.EXTRA_PHOTO
import fr.northborders.walktracker.Constants.FASTEST_INTERVAL_TIME
import fr.northborders.walktracker.Constants.INTENT_BROADCAST_PHOTO
import fr.northborders.walktracker.Constants.INTERVAL_TIME
import fr.northborders.walktracker.Constants.NOTIFICATION_CHANNEL_ID
import fr.northborders.walktracker.Constants.NOTIFICATION_CHANNEL_NAME
import fr.northborders.walktracker.Constants.NOTIFICATION_ID
import fr.northborders.walktracker.Constants.SMALLEST_DISPLACEMENT_100_METERS
import fr.northborders.walktracker.Constants.TIMER_UPDATE_INTERVAL
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.domain.GetPhotosForLocation.Params
import fr.northborders.walktracker.domain.model.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder
    @Inject
    lateinit var notificationObserver: Observer<Long>
    @Inject
    lateinit var getPhotosForLocation: GetPhotosForLocation

    lateinit var locationCallback: LocationCallback

    private var timeWalkInSeconds = MutableLiveData<Long>()
    private var timeWalkInMillis: Long = 0L
    private var isTracking: Boolean = false
    private var lastSecondTimestamp = 0L

    companion object {
        var isServiceRunning = false
    }

    private fun postInitialValues() {
        timeWalkInSeconds.postValue(0L)
        timeWalkInMillis = 0L
        isTracking = false
        lastSecondTimestamp = 0L
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Timber.d("GOT NEW LOCATION: $locationResult!!.lastLocation")

                val lastLocation = locationResult.lastLocation
                val latitude = lastLocation.latitude.toBigDecimal().toPlainString()
                val longitude = lastLocation.longitude.toBigDecimal().toPlainString()
                getPhotosForLocation(Params(latitude, longitude), lifecycleScope) {
                    it.fold (
                            ::handlePhotoError,
                            ::handleNewPhoto
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("ACTION_START_OR_RESUME_SERVICE")
                    startForegroundService()
                }
                ACTION_PAUSE_SERVICE -> Timber.d("ACTION_PAUSE_SERVICE")
                ACTION_STOP_SERVICE -> {
                    Timber.d("ACTION_STOP_SERVICE")
                    stopService()
                }
            }
        }

        return super.onStartCommand(intent, flags, START_NOT_STICKY)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
    }

    private fun stopService() {
        isServiceRunning = false
        postInitialValues()
        timeWalkInSeconds.removeObserver(notificationObserver)
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        stopForeground(true)
        stopSelf()
    }

    private fun startForegroundService() {
        isTracking = true
        isServiceRunning = true
        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }

        subscribeToLocationUpdates()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timeWalkInSeconds.observe(this, notificationObserver)
    }

    private fun startTimer() {
        val timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking) {
                timeWalkInMillis = System.currentTimeMillis() - timeStarted

                if (timeWalkInMillis >= lastSecondTimestamp + 1000L) {
                    timeWalkInSeconds.postValue(timeWalkInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                Timber.d("Time walked: ${timeWalkInSeconds.value}")

                delay(TIMER_UPDATE_INTERVAL)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(notificationManager: NotificationManager) {
        // IMPORTANCE_LOW so the notification doesn't have a sound
        val channel =
            NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW)

        notificationManager.createNotificationChannel(channel)
    }

    @SuppressLint("MissingPermission")
    private fun subscribeToLocationUpdates() {
        if (Utils.hasLocationPermissions(this)) {
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                smallestDisplacement = SMALLEST_DISPLACEMENT_100_METERS // 100 meters
                interval = TimeUnit.SECONDS.toMillis(INTERVAL_TIME.toLong())
                fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_INTERVAL_TIME.toLong())
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.getMainLooper()
            )
        }
    }

    private fun handlePhotoError(failure: Failure) {
        Timber.e("Couldn't retrieve photo for last location ($failure)")
    }

    private fun handleNewPhoto(photo: Photo) {
        Timber.d("Retrieved photo for last location: $photo")
        val intent = Intent(INTENT_BROADCAST_PHOTO)
        intent.putExtra(EXTRA_PHOTO, photo.toPhotoUI())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}