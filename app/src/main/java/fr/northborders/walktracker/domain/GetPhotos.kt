package fr.northborders.walktracker.domain

import arrow.core.Either
import arrow.core.None
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.interactor.UseCase
import fr.northborders.walktracker.domain.model.Photo
import javax.inject.Inject

class GetPhotos @Inject constructor(private val photoRepository: PhotoRepository): UseCase<List<Photo>, None>() {

    override suspend fun run(params: arrow.core.None): Either<Failure, List<Photo>>
        = photoRepository.getAllPhotos()

}