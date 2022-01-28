package fr.northborders.walktracker.data.network.model

data class PhotoDto(
    val id: String,
    val secret: String,
    val server: String,
    val farm: String
)