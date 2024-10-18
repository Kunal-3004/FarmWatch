package com.example.farmwatch.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.farmwatch.Api.SoilAPIRequest
import com.example.farmwatch.Api.SoilAPIResponse
import com.example.farmwatch.Api.SoilApiService

class SoilRepository {
    private val _moistureLiveData = MutableLiveData<SoilAPIResponse>()
    val moistureLiveData: LiveData<SoilAPIResponse> = _moistureLiveData

    suspend fun fetchSoilData(d0: Float, d1: Float, d2: Float, d3: Float) {
        val request = SoilAPIRequest(d0, d1, d2, d3)
        val response = SoilApiService.api.getMoisture(request)

        if (response.isSuccessful && response.body() != null) {
            _moistureLiveData.postValue(response.body())
        }
    }
}
