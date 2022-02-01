package fr.northborders.walktracker.data.network

import fr.northborders.walktracker.data.network.model.PhotoResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PhotoService {

    @GET("$BASE_URL$SEARCH_ENDPOINT$URL_PARAMS")
    suspend fun search(
        @Query("api_key") apiKey: String = API_KEY,
        @Query("lat") lat: String,
        @Query("lon") lon: String,
        @Query("radius ") radius: String
    ): Response<PhotoResponseDto>

    companion object {
        const val BASE_URL = "https://www.flickr.com/services/rest/"
        const val SEARCH_ENDPOINT = "?method=flickr.photos.search"
        const val URL_PARAMS = "&nojsoncallback=1&format=json"
        const val API_KEY = "35a080d62e537c7f7fce62c2bd4c5907"
    }
}