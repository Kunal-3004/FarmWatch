package com.example.farmwatch.Data

data class AirAPIResponse(
    val PM: Float
)
data class AirAPIRequest(
    val temperature: Float,
    val maxTemp: Float,
    val minTemp: Float,
    val seaLevel: Float,
    val humidity: Float,
    val visibility: Float,
    val windSpeed: Float,
    val maxWindSpeed: Float
)