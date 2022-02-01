package fr.northborders.walktracker.features.photos.data.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import fr.northborders.walktracker.PhotoFactory.Factory.makePhotoEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PhotoDaoTest: DbTest() {

    private lateinit var photoDao: PhotoDao

    @Before
    fun createDb() {
        photoDao = db.photoDao()
    }

    @Test
    fun insert_photos_and_get_all_photos() = runBlocking {
        val photo1 = makePhotoEntity()
        val photo2 = makePhotoEntity()
        val photo3 = makePhotoEntity()

        photoDao.insertList(listOf(photo1, photo2, photo3))
        val actual = photoDao.getAllPhotos()

        Truth.assertThat(actual).contains(photo1)
        Truth.assertThat(actual).contains(photo2)
        Truth.assertThat(actual).contains(photo3)
    }

    @Test
    fun inserting_an_empty_list_does_nothing() = runBlocking {
        val photo = makePhotoEntity()
        val expected = listOf(photo)

        photoDao.insertList(listOf(photo))
        photoDao.insertList(emptyList())
        val actual = photoDao.getAllPhotos()

        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun insert_photo_list_and_replace_duplicate_photos() = runBlocking {
        val photo = makePhotoEntity()
        val expected = listOf(photo)

        photoDao.insertList(listOf(photo))
        photoDao.insertList(listOf(photo))
        photoDao.insertList(listOf(photo))
        val actual = photoDao.getAllPhotos()

        Truth.assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun insert_photos_and_clear_all() = runBlocking {
        val expected = emptyList<PhotoEntity>()

        photoDao.insertList((0..5).map { makePhotoEntity() })
        photoDao.clearAll()
        val actual = photoDao.getAllPhotos()

        Truth.assertThat(actual).isEqualTo(expected)
    }
}