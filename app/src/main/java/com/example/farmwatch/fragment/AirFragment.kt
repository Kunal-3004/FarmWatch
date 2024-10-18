package com.example.farmwatch.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.farmwatch.Adapter.Retrofit.RetrofitAir
import com.example.farmwatch.Api.AirApi
import com.example.farmwatch.Data.AirAPIRequest
import com.example.farmwatch.Data.AirAPIResponse
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.FragmentAirBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AirFragment : Fragment() {
    private var _binding: FragmentAirBinding? = null
    private val binding get() = _binding!!
    private lateinit var apiService: AirApi

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAirBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        apiService = RetrofitAir.api

        val toolbar: Toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        binding.predictAir.setOnClickListener {
            if(check()){
                submitPredictionRequest()
            }
        }
    }
    private fun check(): Boolean {
        val temperature=binding.val1
        val maxTemp=binding.val2
        val minTemp=binding.val3
        val seaLevel=binding.seaLvl
        val humidity=binding.humidity
        val visibility=binding.visibility
        val windSpeed=binding.wspeed
        val maxWindSpeed=binding.maxWspeed

        if (temperature.text.toString().isEmpty()) {
            temperature.error = "Cannot be empty"
            return false
        }
        if (maxTemp.text.toString().isEmpty()) {
            maxTemp.error = "Cannot be empty"
            return false
        }
        if (minTemp.text.toString().isEmpty()) {
            minTemp.error = "Cannot be empty"
        }
        if(seaLevel.text.toString().isEmpty()){
            seaLevel.error="Cannot be empty"
            return false
        }
        if(humidity.text.toString().isEmpty()){
            humidity.error="Cannot be empty"
            return false
        }
        if(visibility.text.toString().isEmpty()){
            visibility.error="Cannot be empty"
            return false
        }
        if(windSpeed.text.toString().isEmpty()){
            windSpeed.error="Cannot be empty"
            return false
        }
        if (maxWindSpeed.text.toString().isEmpty()) {
            maxWindSpeed.error = "Cannot be empty"
            return false
        }

        return true
    }
    private fun submitPredictionRequest() {
        val temperature = binding.val1.text.toString().toFloatOrNull()
        val maxTemp = binding.val2.text.toString().toFloatOrNull()
        val minTemp = binding.val3.text.toString().toFloatOrNull()
        val seaLevel = binding.seaLvl.text.toString().toFloatOrNull()
        val humidity = binding.humidity.text.toString().toFloatOrNull()
        val visibility = binding.visibility.text.toString().toFloatOrNull()
        val windSpeed = binding.wspeed.text.toString().toFloatOrNull()
        val maxWindSpeed = binding.maxWspeed.text.toString().toFloatOrNull()

        if (temperature == null || maxTemp == null || minTemp == null ||
            seaLevel == null || humidity == null || visibility == null ||
            windSpeed == null || maxWindSpeed == null) {
            showToast("Please enter valid values for all fields.")
            return
        }

        val request = AirAPIRequest(
            temperature = temperature,
            maxTemp = maxTemp,
            minTemp = minTemp,
            seaLevel = seaLevel,
            humidity = humidity,
            visibility = visibility,
            windSpeed = windSpeed,
            maxWindSpeed = maxWindSpeed
        )

        apiService.getPM(request).enqueue(object : Callback<AirAPIResponse> {
            override fun onResponse(call: Call<AirAPIResponse>, response: Response<AirAPIResponse>) {
                if (response.isSuccessful) {
                    val predictionResponse = response.body()
                    if (predictionResponse != null) {
                        showResultDialog(predictionResponse.PM.toString())
                    } else {
                        showToast("Prediction response is null")
                    }
                } else {
                    if (response.code() == 500) {
                        Log.e("PredictionError", "Internal Server Error: ${response.message()}")
                        showToast("Internal Server Error: Please try again later.")
                    } else {
                        Log.e("PredictionError", "Code: ${response.code()}, Message: ${response.message()}")
                        showToast("Failed to get prediction: ${response.message()}")
                    }
                }
            }

            override fun onFailure(call: Call<AirAPIResponse>, t: Throwable) {
                showToast("Error: ${t.message}")
            }
        })
    }
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    private fun showResultDialog(result: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Air Quality Prediction Result")
            .setMessage("The predicted PM value is: $result")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).updateBottomNavVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
