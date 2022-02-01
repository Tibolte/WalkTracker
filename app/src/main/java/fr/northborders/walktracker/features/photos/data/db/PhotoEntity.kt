package fr.northborders.walktracker.features.photos.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import fr.northborders.walktracker.features.photos.domain.model.Photo

@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val photoId: Int = 0,
    val id: String,
    val secret: String,
    val server: String,
    val farm: String
) {
    fun toPhoto(): Photo {
        return Photo(
            id,
            secret,
            server,
            farm
        )
    }
}