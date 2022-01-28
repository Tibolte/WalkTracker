package fr.northborders.walktracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PhotoEntity::class],
    version = 1)
abstract class PhotoDatabase: RoomDatabase() {
    abstract fun photoDao(): PhotoDao
}