package fr.northborders.walktracker.data.network

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.None
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.exception.Failure.ServerError
import fr.northborders.walktracker.core.exception.Failure.NetworkConnection
import fr.northborders.walktracker.core.exception.Failure.DatabaseError
import fr.northborders.walktracker.core.platform.NetworkHandler
import fr.northborders.walktracker.data.db.PhotoDao
import fr.northborders.walktracker.domain.PhotoRepository
import fr.northborders.walktracker.domain.model.Photo
import retrofit2.Call
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val networkHandler: NetworkHandler,
    private val service: PhotoService,
    private val photoDao: PhotoDao
): PhotoRepository {

    // TODO compare with pictures in database
    override suspend fun searchPhotoForLocation(lat: String, lon: String): Either<Failure, Photo> {
        return when (networkHandler.isNetworkAvailable()) {
            true -> {
                val response = service.search(lat = lat, lon = lon, radius = "0.1")
                when (response.isSuccessful) {
                    true -> {
                        val data = response.body()
                        if (data != null) {
                            Right(data.photos.photo.first().toPhoto())
                        } else {
                            Left(ServerError)
                        }
                    }
                    false -> Left(ServerError)
                }
            }
            false -> Left(NetworkConnection)
        }
    }

    override suspend fun getAllPhotos(): Either<Failure, List<Photo>> {
        val photos = photoDao.getAllPhotos()
        return if (photos.isNotEmpty()) {
            val toPhotosDomain = photos.map { it.toPhoto() }
            Right(toPhotosDomain)
        } else {
            Left(DatabaseError)
        }
    }

    override suspend fun deletePhotos(): Either<Failure, None> {
        return try {
            photoDao.clearAll()
            Right(None)
        } catch (e: Exception) {
            Left(DatabaseError)
        }
    }
}