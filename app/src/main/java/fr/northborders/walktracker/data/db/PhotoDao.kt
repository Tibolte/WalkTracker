package fr.northborders.walktracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PhotoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(photoEntities: List<PhotoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(photoEntity: PhotoEntity)

    @Query("SELECT * FROM photos WHERE photoId = :photoId")
    suspend fun findById(photoId: String): PhotoEntity

    @Query("SELECT * FROM photos")
    suspend fun getAllPhotos(): List<PhotoEntity>

    @Query("DELETE FROM photos")
    suspend fun clearAll()
}