package fr.northborders.walktracker.usecase

import arrow.core.Either.Right
import com.google.common.truth.Truth
import fr.northborders.walktracker.domain.GetPhoto
import fr.northborders.walktracker.domain.PhotoRepository
import fr.northborders.walktracker.domain.model.Photo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class GetPhotoTest {
    private lateinit var getPhoto: GetPhoto

    @MockK private lateinit var photoRepository: PhotoRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        getPhoto = GetPhoto(photoRepository)
        coEvery { photoRepository.searchPhotoForLocation(any(), any()) } returns Right(makeFakePhoto("1"))
    }

    @Test
    fun `should get data from repository`() = runBlocking {
        val lat = ""
        val lon = ""
        getPhoto.run(GetPhoto.Params(lat, lon))

        // verify that the repo function has been called
        coVerify(exactly = 1) { photoRepository.searchPhotoForLocation(lat, lon) }
    }

    private fun makeFakePhoto(id: String) = Photo(id, id, id, id)
}