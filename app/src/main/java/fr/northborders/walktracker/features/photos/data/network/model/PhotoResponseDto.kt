package fr.northborders.walktracker.features.photos.data.network.model

import fr.northborders.walktracker.features.photos.data.db.PhotoEntity
import fr.northborders.walktracker.features.photos.domain.model.Photo

data class PhotoResponseDto(
    val photos: PhotoListDto
)

data class PhotoListDto(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: List<PhotoDto>
)

data class PhotoDto(
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

    fun toPhotoEntity(): PhotoEntity {
        return PhotoEntity(
            id = id,
            secret = secret,
            server = server,
            farm = farm
        )
    }
}