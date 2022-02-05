package fr.northborders.walktracker.core.cache

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.northborders.walktracker.features.photos.data.db.PhotoDao
import fr.northborders.walktracker.features.photos.data.db.PhotoEntity
import fr.northborders.walktracker.features.walks.WalkEntity

@Database(
    entities = [PhotoEntity::class, WalkEntity::class],
    version = 1)
abstract class MainDatabase: RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}