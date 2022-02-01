package fr.northborders.walktracker.features.photos.data.network

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.exception.Failure.ServerError
import fr.northborders.walktracker.core.exception.Failure.NetworkConnection
import fr.northborders.walktracker.core.exception.Failure.DatabaseError
import fr.northborders.walktracker.core.platform.NetworkHandler
import fr.northborders.walktracker.features.photos.data.db.PhotoDao
import fr.northborders.walktracker.features.photos.data.network.model.PhotoListDto
import fr.northborders.walktracker.features.photos.domain.PhotoRepository
import fr.northborders.walktracker.features.photos.domain.model.Photo
import javax.inject.Inject

class PhotoRepositoryImpl @Inject constructor(
    private val networkHandler: NetworkHandler,
    private val service: PhotoService,
    private val photoDao: PhotoDao
): PhotoRepository {

    override suspend fun searchPhotoForLocation(lat: String, lon: String): Either<Failure, Photo> {
        return when (networkHandler.isNetworkAvailable()) {
            true -> {
                val response = service.search(lat = lat, lon = lon, radius = "0.1")
                when (response.isSuccessful) {
                    true -> {
                        val photoListDto = response.body()?.photos
                        if (photoListDto != null) {
                            Right(extractAndSavePhoto(photoListDto))
                        } else{
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

    override suspend fun deletePhotos(): Either<Failure, List<Photo>> {
        return try {
            photoDao.clearAll()
            Right(emptyList())
        } catch (e: Exception) {
            Left(DatabaseError)
        }
    }

    private suspend fun extractAndSavePhoto(photos: PhotoListDto): Photo {
        val photosFromDb = photoDao.getAllPhotos()
        if (photosFromDb.isEmpty()) {
            val photoToInsert = photos.photo.random()
            photoDao.insert(photoToInsert.toPhotoEntity())
            return photoToInsert.toPhoto()
        } else {
            for (photo in photos.photo) {
                if (photosFromDb.all { photoEntity -> photoEntity.id != photo.id }) {
                    // return the photo from server if not found in db
                    photoDao.insert(photo.toPhotoEntity())
                    return photo.toPhoto()
                }
            }
        }
        return Photo("", "", "", "")
//        val data = response.body()
//        if (data?.photos != null) {
//            val photosFromDb = photoDao.getAllPhotos()
//            if (photosFromDb.isEmpty()) {
//                val photoToInsert = data.photos.photo.random()
//                photoDao.insert(photoToInsert.toPhotoEntity())
//                return photoToInsert.toPhoto()
//            } else {
//                for (photo in data.photos.photo) {
//                    if (photosFromDb.all { photoEntity -> photoEntity.id != photo.id }) {
//                        // return the photo from server if not found in db
//                        photoDao.insert(photo.toPhotoEntity())
//                        return photo.toPhoto()
//                    }
//                }
//            }
//        }
//        return Photo("", "", "", "")
    }
}