package com.example.farmwatch.Api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

data class SoilAPIResponse(
    @SerializedName("moisture")
    val moisture: Int
)

data class SoilAPIRequest(
    val d0: Int,
    val d1: Int,
    val d2: Int,
    val d3: Int
)
interface SoilApiInterface {
    @POST("/predict")
    suspend fun getMoisture(@Body request: SoilAPIRequest): Response<SoilAPIResponse>
}

object SoilApiService {
    private const val BASE_URL = "https://api.imgflip.com"

    val api: SoilApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoilApiInterface::class.java)
    }
}
