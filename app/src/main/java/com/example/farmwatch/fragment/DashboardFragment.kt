package com.example.farmwatch.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.findNavController
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar: Toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)

        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as HomeActivity).openDrawer()
        }

        binding.card1.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_airQualityFragment)
        }

        binding.card2.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_cropProductionAnalysisFragment)
        }

        binding.card3.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_vegetableClassificationFragment)
        }

        binding.card4.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_riceTypeClassificationFragment)
        }
    }
    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).updateBottomNavVisibility() // Update Bottom Navigation visibility
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}