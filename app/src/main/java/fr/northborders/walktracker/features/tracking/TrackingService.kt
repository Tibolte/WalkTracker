package fr.northborders.walktracker.features.tracking

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Intent
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import fr.northborders.walktracker.core.util.Constants.ACTION_PAUSE_SERVICE
import fr.northborders.walktracker.core.util.Constants.ACTION_START_OR_RESUME_SERVICE
import fr.northborders.walktracker.core.util.Constants.ACTION_STOP_SERVICE
import fr.northborders.walktracker.core.util.Constants.EXTRA_PHOTO
import fr.northborders.walktracker.core.util.Constants.INTENT_BROADCAST_PHOTO
import fr.northborders.walktracker.core.util.Constants.NOTIFICATION_CHANNEL_ID
import fr.northborders.walktracker.core.util.Constants.NOTIFICATION_CHANNEL_NAME
import fr.northborders.walktracker.core.util.Constants.NOTIFICATION_ID
import fr.northborders.walktracker.core.util.Constants.TIMER_UPDATE_INTERVAL
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.platform.locationFlow
import fr.northborders.walktracker.core.util.Utils
import fr.northborders.walktracker.features.photos.domain.GetPhotosForLocation
import fr.northborders.walktracker.features.photos.domain.model.Photo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

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

    private var timeWalkInSeconds = MutableLiveData<Long>()
    private var timeWalked = 0L
    private var lapTime = 0L
    private var isTimerEnabled = false
    private var lastSecondTimestamp = 0L

    companion object {
        var isServiceRunning = false
        var timeWalkInMillis = MutableLiveData<Long>()
        val pathPoints = MutableLiveData<Polylines>()
        val isTracking = MutableLiveData<Boolean>()
    }

    private fun postInitialValues() {
        timeWalkInSeconds.postValue(0L)
        timeWalkInMillis.postValue(0L)
        pathPoints.postValue(mutableListOf())
        isTracking.postValue(false)
        lastSecondTimestamp = 0L
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    Timber.d("ACTION_START_OR_RESUME_SERVICE")
                    startForegroundService()
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("ACTION_PAUSE_SERVICE")
                    pauseService()
                }
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

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    private fun stopService() {
        isServiceRunning = false
        postInitialValues()
        timeWalkInSeconds.removeObserver(notificationObserver)
        stopForeground(true)
        stopSelf()
    }

    private fun startForegroundService() {
        isTracking.postValue(true)
        isServiceRunning = true
        isTimerEnabled = true
        startTimer()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotification(notificationManager)
        }

        subscribeToLocationUpdates()

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timeWalkInSeconds.observe(this, notificationObserver)
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                if (!last().contains(pos)) {
                    last().add(pos)
                    pathPoints.postValue(this)
                }
            }
        }
    }

    private fun startTimer() {
        addEmptyPolyline()

        val timeStarted = System.currentTimeMillis()

        CoroutineScope(Dispatchers.Main).launch {
            while (isTracking.value!!) {
                lapTime = System.currentTimeMillis() - timeStarted

                timeWalkInMillis.postValue(timeWalked + lapTime)

                if (timeWalkInMillis.value!! >= lastSecondTimestamp + 1000L) {
                    timeWalkInSeconds.postValue(timeWalkInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }

                Timber.d("Time walked: ${timeWalkInSeconds.value}")
                Timber.d("lap time: ${lapTime / 1000}")

                delay(TIMER_UPDATE_INTERVAL)
            }
            timeWalked += lapTime
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
            fusedLocationProviderClient.locationFlow()
                .conflate()
                .catch { e ->
                    Timber.e("Unable to get location", e)
                }
                .asLiveData()
                .observe(this, Observer { location ->
                    Timber.d("Got new location: $location")
                    if (isTimerEnabled) {
                        Timber.d("ADD NEW POLYLINE: $location")
                        addPathPoint(location)
                        val latitude = location.latitude.toBigDecimal().toPlainString()
                        val longitude = location.longitude.toBigDecimal().toPlainString()
                        getPhotosForLocation(GetPhotosForLocation.Params(latitude, longitude), lifecycleScope) {
                            it.fold(
                                ::handlePhotoError,
                                ::handleNewPhoto
                            )
                        }
                    }
                })
        }
    }

    private fun handlePhotoError(failure: Failure) {
        Timber.e("Couldn't retrieve photo for last location ($failure)")
    }

    private fun handleNewPhoto(photo: Photo) {
        // TODO observe live data as well?
        Timber.d("Retrieved photo for last location: $photo")
        val intent = Intent(INTENT_BROADCAST_PHOTO)
        intent.putExtra(EXTRA_PHOTO, photo.toPhotoUI())
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }
}