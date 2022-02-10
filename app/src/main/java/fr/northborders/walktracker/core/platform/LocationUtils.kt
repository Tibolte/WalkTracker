package fr.northborders.walktracker.core.platform

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import fr.northborders.walktracker.core.util.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

fun createLocationRequest() = LocationRequest.create().apply {
    interval = TimeUnit.SECONDS.toMillis(Constants.INTERVAL_TIME.toLong())
    fastestInterval = TimeUnit.SECONDS.toMillis(Constants.FASTEST_INTERVAL_TIME.toLong())
    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    smallestDisplacement = Constants.SMALLEST_DISPLACEMENT_100_METERS
}

@SuppressLint("MissingPermission")
fun FusedLocationProviderClient.locationFlow() = callbackFlow<Location> {
    val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result ?: return
            for (location in result.locations) {
                try {
                    trySend(location) // emit location into the Flow using ProducerScope.offer
                } catch (e: Exception) {
                    // nothing to do
                    // Channel was probably already closed by the time offer was called
                }
            }
        }
    }

    requestLocationUpdates(
        createLocationRequest(),
        callback,
        Looper.getMainLooper()
    ).addOnFailureListener { e ->
        close(e) // in case of exception, close the Flow
    }

    awaitClose {
        removeLocationUpdates(callback) // clean up when Flow collection ends
    }
}