package fr.northborders.walktracker.features.tracking.domain

import android.annotation.SuppressLint
import android.location.Location
import arrow.core.Either
import arrow.core.Either.Right
import arrow.core.Either.Left
import arrow.core.None
import com.google.android.gms.location.FusedLocationProviderClient
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.exception.Failure.FeatureFailure
import fr.northborders.walktracker.core.interactor.UseCase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import javax.inject.Inject

class GetLocation @Inject constructor(private val fusedLocationProviderClient: FusedLocationProviderClient)
    : UseCase<Location, None>() {

    @SuppressLint("MissingPermission")
    override suspend fun run(params: None): Either<Failure, Location> {
        return try {
            Right(fusedLocationProviderClient.lastLocation.await())
        } catch (e: Exception) {
            Left(LocationFailure)
        }
    }

    object LocationFailure : FeatureFailure()
}