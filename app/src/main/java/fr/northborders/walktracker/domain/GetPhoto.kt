package fr.northborders.walktracker.domain

import arrow.core.Either
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.interactor.UseCase
import fr.northborders.walktracker.domain.model.Photo
import fr.northborders.walktracker.domain.GetPhoto.Params
import javax.inject.Inject

class GetPhoto @Inject constructor(private val photoRepository: PhotoRepository): UseCase<Photo, Params>() {

    override suspend fun run(params: Params): Either<Failure, Photo> =
        photoRepository.searchPhotoForLocation(params.lat, params.lon)

    data class Params(val lat: String, val lon: String)
}