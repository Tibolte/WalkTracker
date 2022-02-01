package fr.northborders.walktracker.features.photos.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After

abstract class DbTest {

    val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        PhotoDatabase::class.java
    ).build()

    @After
    fun closeDb() {
        db.close()
    }
}