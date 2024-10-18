package com.example.farmwatch.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.farmwatch.Api.SoilAPIResponse
import com.example.farmwatch.repository.SoilRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SoilViewModel(private val repository: SoilRepository) : ViewModel() {

    val soilMoistureData: LiveData<SoilAPIResponse> = repository.moistureLiveData
    fun fetchSoilMoisture(d0: Float, d1: Float, d2: Float, d3: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.fetchSoilData(d0, d1, d2, d3)
        }
    }
}

class SoilViewModelFactory(private val repository: SoilRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SoilViewModel(repository) as T
    }
}
