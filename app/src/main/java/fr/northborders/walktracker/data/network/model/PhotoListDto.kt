package fr.northborders.walktracker.data.network.model

data class PhotoListDto(
    val page: Int,
    val pages: Int,
    val perpage: Int,
    val total: Int,
    val photo: List<PhotoDto>
)