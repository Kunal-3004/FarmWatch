package com.example.farmwatch.Api

import com.example.farmwatch.Data.AirAPIRequest
import com.example.farmwatch.Data.AirAPIResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AirApi {
    @POST("/predict")
    fun getPM(@Body request: AirAPIRequest): Call<AirAPIResponse>
}