package fr.northborders.walktracker.domain

import arrow.core.Either
import arrow.core.None
import fr.northborders.walktracker.core.exception.Failure
import fr.northborders.walktracker.core.interactor.UseCase
import javax.inject.Inject

class DeletePhotos @Inject constructor(private val photoRepository: PhotoRepository): UseCase<None, None>() {
    override suspend fun run(params: arrow.core.None): Either<Failure, arrow.core.None> =
        photoRepository.deletePhotos()
}