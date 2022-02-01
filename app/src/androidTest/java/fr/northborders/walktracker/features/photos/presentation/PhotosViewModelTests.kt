package fr.northborders.walktracker.features.photos.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either.Right
import com.google.common.truth.Truth
import fr.northborders.walktracker.features.photos.domain.DeletePhotos
import fr.northborders.walktracker.features.photos.domain.GetPhotos
import fr.northborders.walktracker.features.photos.domain.model.Photo
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.RelaxedMockK
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PhotosViewModelTests {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var photosViewModel: PhotosViewModel

    @RelaxedMockK
    lateinit var getPhotos: GetPhotos
    @RelaxedMockK
    lateinit var deletePhotos: DeletePhotos

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        photosViewModel = PhotosViewModel(getPhotos, deletePhotos)
    }

    @Test
    fun loading_photos_should_update_live_data() = runBlocking {
        val photos = listOf(makeFakePhoto("1"), makeFakePhoto("2"))

        coEvery { getPhotos.run(any()) } returns Right(photos)

        photosViewModel.photos.observeForever {
            Truth.assertThat(it.size).isEqualTo(2)
            Truth.assertThat(it[0].id).isEqualTo("1")
            Truth.assertThat(it[1].id).isEqualTo("2")
        }

        photosViewModel.loadPhotos()
    }

    @Test
    fun deleting_photos_should_update_live_data() = runBlocking {
        coEvery { deletePhotos.run(any()) } returns Right(emptyList())

        photosViewModel.photos.observeForever {
            Truth.assertThat(it).isEmpty()
        }

        photosViewModel.deletePhotos()
    }

    private fun makeFakePhoto(id: String) = Photo(id, id, id, id)
}