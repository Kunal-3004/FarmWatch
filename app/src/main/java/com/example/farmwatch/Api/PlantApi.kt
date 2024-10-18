package com.example.farmwatch.Api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST


data class PlantAPIResponse(
    val text: String
)

interface PlantApiInterface {
    @POST("/predict")
    suspend fun uploadImage(@Body image: String): Response<PlantAPIResponse>
}

object PlantApiInstance {
    private const val BASE_URL = "https://your.api.url/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: PlantApiInterface by lazy {
        retrofit.create(PlantApiInterface::class.java)
    }
}
