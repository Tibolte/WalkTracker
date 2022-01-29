package fr.northborders.walktracker.usecase

import arrow.core.Either
import arrow.core.None
import fr.northborders.walktracker.domain.GetPhotos
import fr.northborders.walktracker.domain.PhotoRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetPhotosTest {
    private lateinit var getPhotos: GetPhotos

    @MockK
    private lateinit var photoRepository: PhotoRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getPhotos = GetPhotos(photoRepository)
        coEvery { photoRepository.getAllPhotos() } returns Either.Right(
            emptyList()
        )
    }

    @Test
    fun `should get data from repository`() = runBlocking {
        getPhotos.run(None)

        coVerify(exactly = 1) { photoRepository.getAllPhotos() }
    }
}