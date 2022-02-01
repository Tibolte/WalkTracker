package fr.northborders.walktracker.features.photos.domain.model

import fr.northborders.walktracker.features.photos.presentation.model.PhotoUI

data class Photo(
    val id: String,
    val secret: String,
    val server: String,
    val farm: String
) {
    fun toPhotoUI(): PhotoUI {
        return PhotoUI(id, secret, server, farm)
    }
}