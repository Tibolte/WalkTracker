package fr.northborders.walktracker.features.photos.data.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import fr.northborders.walktracker.core.cache.MainDatabase
import org.junit.After

abstract class DbTest {

    val db = Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext(),
        MainDatabase::class.java
    ).build()

    @After
    fun closeDb() {
        db.close()
    }
}