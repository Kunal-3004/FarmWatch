package com.example.farmwatch.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.farmwatch.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var myMap: GoogleMap
    private val FINE_PERMISSION_CODE = 1
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        getLastLoc()

        return view
    }

    private fun getLastLoc() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), FINE_PERMISSION_CODE)
            return
        }

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = LatLng(location.latitude, location.longitude)
                if (::myMap.isInitialized) {
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 12f))
                    findNearbyPlaces("Mandi", currentLocation!!)
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        myMap = googleMap
        currentLocation?.let {
            myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 12f))
        }
    }

    private fun findNearbyPlaces(type: String, location: LatLng) {
        val apiKey = getString(R.string.my_map_api_key) // Retrieve API key from resources
        val locationString = "${location.latitude},${location.longitude}"
        val radius = 5000
        lifecycleScope.launch {
            try {
                val response = RetrofitInstance.api.getNearbyPlaces(
                    location = locationString,
                    radius = radius,
                    type = type,
                    apiKey = apiKey
                )

                if (response.isSuccessful) {
                    val places = response.body()?.results
                    places?.forEach { place: Place ->
                        val placeLocation = LatLng(place.geometry.location.lat, place.geometry.location.lng)
                        myMap.addMarker(MarkerOptions().position(placeLocation).title(place.name))
                    }

                    if (!places.isNullOrEmpty()) {
                        val firstPlace = places[0].geometry.location
                        myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(firstPlace.lat, firstPlace.lng), 12f))
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to get places", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(requireContext(), "An error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLoc()
            } else {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
data class PlacesResponse(
    val results: List<Place>
)

data class Place(
    val geometry: Geometry,
    val name: String
)

data class Geometry(
    val location: LocationLatLng
)

data class LocationLatLng(
    val lat: Double,
    val lng: Double
)

interface PlacesApiService {
    @GET("place/nearbysearch/json")
    suspend fun getNearbyPlaces(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): Response<PlacesResponse>
}
object RetrofitInstance {
    private const val BASE_URL = "https://maps.googleapis.com/maps/api/"

    val api: PlacesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlacesApiService::class.java)
    }
}
