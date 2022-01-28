package fr.northborders.walktracker.domain.model

data class Photo(
    val id: String,
    val secret: String,
    val server: String,
    val farm: String
)