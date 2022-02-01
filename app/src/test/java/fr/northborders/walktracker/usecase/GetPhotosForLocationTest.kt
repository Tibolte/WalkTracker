package fr.northborders.walktracker.usecase

import arrow.core.Either.Right
import fr.northborders.walktracker.features.photos.domain.GetPhotosForLocation
import fr.northborders.walktracker.features.photos.domain.PhotoRepository
import fr.northborders.walktracker.features.photos.domain.model.Photo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetPhotosForLocationTest {
    private lateinit var getPhotosForLocation: GetPhotosForLocation

    @MockK private lateinit var photoRepository: PhotoRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getPhotosForLocation = GetPhotosForLocation(photoRepository)
        coEvery { photoRepository.searchPhotoForLocation(any(), any()) } returns Right(makeFakePhoto("1"))
    }

    @Test
    fun `should get data from repository`() = runBlocking {
        val lat = ""
        val lon = ""
        getPhotosForLocation.run(GetPhotosForLocation.Params(lat, lon))

        // verify that the repo function has been called
        coVerify(exactly = 1) { photoRepository.searchPhotoForLocation(lat, lon) }
    }

    private fun makeFakePhoto(id: String) = Photo(id, id, id, id)
}