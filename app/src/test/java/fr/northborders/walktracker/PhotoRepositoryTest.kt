package fr.northborders.walktracker

import arrow.core.Either.Left
import arrow.core.Either.Right
import com.google.common.truth.Truth
import fr.northborders.walktracker.core.exception.Failure.ServerError
import fr.northborders.walktracker.core.exception.Failure.NetworkConnection
import fr.northborders.walktracker.core.platform.NetworkHandler
import fr.northborders.walktracker.data.db.PhotoDao
import fr.northborders.walktracker.data.network.PhotoRepositoryImpl
import fr.northborders.walktracker.data.network.PhotoService
import fr.northborders.walktracker.data.network.model.PhotoDto
import fr.northborders.walktracker.data.network.model.PhotoListDto
import fr.northborders.walktracker.data.network.model.PhotoResponseDto
import fr.northborders.walktracker.domain.model.Photo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Response

class PhotoRepositoryTest {

    private lateinit var photoRepositoryImpl: PhotoRepositoryImpl

    @MockK private lateinit var networkHandler: NetworkHandler
    @MockK private lateinit var photoService: PhotoService
    @MockK private lateinit var photoDao: PhotoDao
    @MockK private lateinit var photosResponseDto: Response<PhotoResponseDto>

    private val photoListResponse = PhotoResponseDto(
        PhotoListDto(1, 1, 5, 50, listOf(
            makeFakePhotoDto("1"),
            makeFakePhotoDto("2")
        )))

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        photoRepositoryImpl = PhotoRepositoryImpl(networkHandler, photoService, photoDao)
    }

    @Test
    fun `should return server error by default`() = runBlocking {

        every { networkHandler.isNetworkAvailable() } returns true
        every { photosResponseDto.body() } returns null
        every { photosResponseDto.isSuccessful } returns true
        coEvery { photoService.search(any(), any(), any(), any()) } returns photosResponseDto

        val result = photoRepositoryImpl.searchPhotoForLocation("", "")

        Truth.assertThat(result).isEqualTo(Left(ServerError))
    }

    @Test
    fun `should return the first photo from the server response`() = runBlocking {
        every { networkHandler.isNetworkAvailable() } returns true
        every { photosResponseDto.body() } returns photoListResponse
        every { photosResponseDto.isSuccessful } returns true
        coEvery { photoService.search(any(), any(), any(), any()) } returns photosResponseDto

        val result = photoRepositoryImpl.searchPhotoForLocation("", "")
        Truth.assertThat(result).isEqualTo(Right(makeFakePhoto("1")))
    }

    @Test fun `photos service should return network failure when no connection`() = runBlocking {
        every { networkHandler.isNetworkAvailable() } returns false

        val result = photoRepositoryImpl.searchPhotoForLocation("", "")

        Truth.assertThat(result).isEqualTo(Left(NetworkConnection))
    }

    @Test fun `photos service should return server error if no successful response`() = runBlocking {
        every { networkHandler.isNetworkAvailable() } returns true
        every { photosResponseDto.isSuccessful } returns false
        coEvery { photoService.search(any(), any(), any(), any()) } returns photosResponseDto

        val result = photoRepositoryImpl.searchPhotoForLocation("", "")

        Truth.assertThat(result).isEqualTo(Left(ServerError))
    }

    private fun makeFakePhotoDto(id: String) = PhotoDto(id, id, id, id)
    private fun makeFakePhoto(id: String) = Photo(id, id, id, id)
}