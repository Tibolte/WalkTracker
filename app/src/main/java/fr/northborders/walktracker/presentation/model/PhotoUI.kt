package fr.northborders.walktracker.presentation.model

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@Parcelize
data class PhotoUI(
    val id: String,
    val secret: String,
    val server: String,
    val farm: String
) : Parcelable {
    fun buildUri(): String {
        return ("https://farm$farm.staticflickr.com/$server/${id}_$secret.jpg")
    }
}