package fr.northborders.walktracker

import arrow.core.Either.Left
import arrow.core.Either.Right
import com.google.common.truth.Truth
import fr.northborders.walktracker.core.exception.Failure.ServerError
import fr.northborders.walktracker.core.exception.Failure.NetworkConnection
import fr.northborders.walktracker.core.exception.Failure.DatabaseError
import fr.northborders.walktracker.core.platform.NetworkHandler
import fr.northborders.walktracker.features.photos.data.db.PhotoDao
import fr.northborders.walktracker.features.photos.data.db.PhotoEntity
import fr.northborders.walktracker.features.photos.data.network.PhotoRepositoryImpl
import fr.northborders.walktracker.features.photos.data.network.PhotoService
import fr.northborders.walktracker.features.photos.data.network.model.PhotoDto
import fr.northborders.walktracker.features.photos.data.network.model.PhotoListDto
import fr.northborders.walktracker.features.photos.data.network.model.PhotoResponseDto
import fr.northborders.walktracker.features.photos.domain.model.Photo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.lang.Exception

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
    fun `should return one photo from the server response if db is empty`() = runBlocking {
        every { networkHandler.isNetworkAvailable() } returns true
        every { photosResponseDto.body() } returns photoListResponse
        every { photosResponseDto.isSuccessful } returns true
        coEvery { photoDao.getAllPhotos() } returns emptyList()
        coEvery { photoDao.insert(any()) } returns Unit
        coEvery { photoService.search(any(), any(), any(), any()) } returns photosResponseDto

        val result = photoRepositoryImpl.searchPhotoForLocation("", "")
        Truth.assertThat(result).isInstanceOf(Right::class.java)
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

    @Test fun `photos repository should return DatabaseError if no photos are found`() = runBlocking {
        coEvery { photoDao.getAllPhotos() } returns emptyList()

        val result = photoRepositoryImpl.getAllPhotos()

        Truth.assertThat(result).isEqualTo(Left(DatabaseError))
    }

    @Test fun `photos repository should return list if photos are found`() = runBlocking {
        coEvery { photoDao.getAllPhotos() } returns listOf(makeFakePhotoEntity("1"))

        val result = photoRepositoryImpl.getAllPhotos()

        Truth.assertThat(result).isEqualTo(Right(listOf(makeFakePhoto("1"))))
    }

    @Test fun `photos repository should return error if photos db deletion fails`() = runBlocking {
        coEvery { photoDao.clearAll()} throws Exception()

        val result = photoRepositoryImpl.deletePhotos()

        Truth.assertThat(result).isEqualTo(Left(DatabaseError))
    }

    @Test fun `photos repository should return Right(None) if photos db deletion succeeds`() = runBlocking {
        coEvery { photoDao.clearAll()} returns Unit

        val result = photoRepositoryImpl.deletePhotos()

        Truth.assertThat(result).isEqualTo(Right(emptyList<Photo>()))
    }

    private fun makeFakePhotoEntity(id: String) = PhotoEntity(1, id, id, id, id)
    private fun makeFakePhotoDto(id: String) = PhotoDto(id, id, id, id)
    private fun makeFakePhoto(id: String) = Photo(id, id, id, id)
}