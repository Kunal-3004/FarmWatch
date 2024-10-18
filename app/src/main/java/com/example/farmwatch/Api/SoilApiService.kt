package com.example.farmwatch.Api

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import com.google.gson.annotations.SerializedName

data class SoilAPIResponse(
    @SerializedName("moisture")
    val moisture: Float
)

data class SoilAPIRequest(
    val d0: Float,
    val d1: Float,
    val d2: Float,
    val d3: Float
)
interface SoilApiInterface {
    @POST("/predict")
    suspend fun getMoisture(@Body request: SoilAPIRequest): Response<SoilAPIResponse>
}

object SoilApiService {
    private const val BASE_URL = "https://soilapi-4.onrender.com"

    val api: SoilApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SoilApiInterface::class.java)
    }
}
