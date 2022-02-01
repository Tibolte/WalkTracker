package fr.northborders.walktracker.domain

import arrow.core.Either
import arrow.core.None
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.domain.model.Photo

interface PhotoRepository {

    suspend fun searchPhotoForLocation(lat: String, lon: String): Either<Failure, Photo>

    suspend fun getAllPhotos(): Either<Failure, List<Photo>>

    suspend fun deletePhotos(): Either<Failure, List<Photo>>
}