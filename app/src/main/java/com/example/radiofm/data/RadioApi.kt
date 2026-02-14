package com.example.radiofm.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RadioApi {
    @GET("search")
    suspend fun searchStations(@Query("q") query: String): RadioGardenSearchResponse

    @GET("ara/content/channel/{channelId}")
    suspend fun getChannelInfo(@Path("channelId") channelId: String): RadioGardenChannelResponse

    companion object {
        const val BASE_URL = "https://radio.garden/api/"
        const val STREAM_BASE_URL = "https://radio.garden/api/ara/content/listen/"
    }
}

data class RadioGardenSearchResponse(
    val hits: RadioGardenHits
)

data class RadioGardenHits(
    val hits: List<RadioGardenHit>
)

data class RadioGardenHit(
    val _id: String?,
    val _source: RadioGardenSource?
)

data class RadioGardenSource(
    val page: RadioGardenPage?,
    val type: String?
)

data class RadioGardenPage(
    val url: String?,
    val title: String?,
    val type: String?
)

data class RadioGardenChannelResponse(
    val data: RadioGardenChannelData
)

data class RadioGardenChannelData(
    val title: String,
    val url: String,
    val place: RadioGardenPlace
)

data class RadioGardenPlace(
    val title: String
)
