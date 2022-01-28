package fr.northborders.walktracker.data.network

import fr.northborders.walktracker.data.network.model.PhotoResponseDto
import retrofit2.http.Query

interface PhotoService {

    suspend fun search(
        @Query("api_key") apiKey: String,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("radius ") radius: String
    ): PhotoResponseDto

    companion object {
        const val BASE_URL = "https://www.flickr.com/services/rest/"
        const val SEARCH_ENDPOINT = "?method=flickr.photos.search"
        const val URL_PARAMS = "&nojsoncallback=1&format=json"
        const val API_KEY = ""
    }
}