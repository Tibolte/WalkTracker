package fr.northborders.walktracker.features.photos.domain

import arrow.core.Either
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.features.photos.domain.model.Photo

interface PhotoRepository {

    suspend fun searchPhotoForLocation(lat: String, lon: String): Either<Failure, Photo>

    suspend fun getAllPhotos(): Either<Failure, List<Photo>>

    suspend fun deletePhotos(): Either<Failure, List<Photo>>
}