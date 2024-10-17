package com.example.farmwatch.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.FragmentSoilBinding
import com.example.farmwatch.repository.SoilRepository
import com.example.farmwatch.viewmodel.SoilViewModel
import com.example.farmwatch.viewmodel.SoilViewModelFactory
import com.google.android.material.navigation.NavigationView

class SoilFragment : Fragment() {
    private var _binding: FragmentSoilBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SoilViewModel by viewModels {
        SoilViewModelFactory(SoilRepository())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSoilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val moistureTextView = binding.resultSoil

        viewModel.soilMoistureData.observe(viewLifecycleOwner, Observer { soilData ->
            if (soilData != null) {
                moistureTextView.text = "Moisture: ${soilData.moisture}"
            } else {
                Toast.makeText(requireContext(), "Failed to fetch moisture data", Toast.LENGTH_LONG)
                    .show()
            }
        })

        val toolbar: Toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        val navigationView: NavigationView = (activity as HomeActivity).binding.navView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miItem1 -> {
                    findNavController().navigate(R.id.nav_weather)
                    true
                }

                R.id.miItem2 -> {
                    findNavController().navigate(R.id.nav_air)
                    true
                }

                R.id.miItem3 -> {
                    findNavController().navigate(R.id.nav_soil)
                    true
                }

                R.id.miItem4 -> {
                    (activity as HomeActivity).logout()
                    true
                }

                else -> false
            }
        }
        binding.predictSoil.setOnClickListener {
            val d0 = binding.val1.text.toString().toIntOrNull() ?: 0
            val d1 = binding.val2.text.toString().toIntOrNull() ?: 0
            val d2 = binding.val3.text.toString().toIntOrNull() ?: 0
            val d3 = binding.val4.text.toString().toIntOrNull() ?: 0

            viewModel.fetchSoilMoisture(d0, d1, d2, d3)
            binding.resultSoil.visibility=View.VISIBLE
        }
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
