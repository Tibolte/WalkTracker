package fr.northborders.walktracker.usecase

import arrow.core.Either
import arrow.core.None
import fr.northborders.walktracker.domain.DeletePhotos
import fr.northborders.walktracker.domain.PhotoRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class DeletePhotosTest {
    private lateinit var deletePhotos: DeletePhotos

    @MockK
    private lateinit var photoRepository: PhotoRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        deletePhotos = DeletePhotos(photoRepository)
        coEvery { photoRepository.deletePhotos() } returns Either.Right(
            None
        )
    }

    @Test
    fun `should call delete repository function`() = runBlocking {
        deletePhotos.run(None)

        coVerify(exactly = 1) { photoRepository.deletePhotos() }
    }
}