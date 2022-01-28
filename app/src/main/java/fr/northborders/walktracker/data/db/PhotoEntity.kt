package fr.northborders.walktracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val photoId: Int,
    val id: String,
    val secret: String,
    val server: String,
    val farm: String,
)