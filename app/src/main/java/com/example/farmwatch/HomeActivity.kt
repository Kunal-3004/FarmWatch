package com.example.farmwatch

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.farmwatch.databinding.ActivityHomeBinding
import com.example.farmwatch.fragment.AirFragment
import com.example.farmwatch.fragment.PlantDiseaseFragment
import com.example.farmwatch.fragment.SoilFragment
import com.example.farmwatch.fragment.WeatherFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        drawerLayout = binding.drawer
        navigationView = binding.navView

        toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            binding.appBarMain.toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_dash, R.id.nav_soil, R.id.nav_weather, R.id.nav_air, R.id.nav_cpa, R.id.nav_rtc, R.id.nav_vegetable
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.miItem4 -> {
                    logout()
                }
                R.id.miItem1 -> {
                    navController.navigate(R.id.nav_weather)
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.miItem2 -> {
                    navController.navigate(R.id.nav_air)
                    bottomNavigationView.visibility = View.GONE
                }
                R.id.miItem3 -> {
                    navController.navigate(R.id.nav_soil)
                    bottomNavigationView.visibility = View.GONE
                }
            }
            drawerLayout.closeDrawers()
            true
        }

        bottomNavigationView = binding.appBarMain.bottomNav
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dash -> {
                    navController.navigate(R.id.nav_dash)
                    true
                }
                R.id.nav_soil -> {
                    navController.navigate(R.id.nav_soil)
                    true
                }
                R.id.nav_weather -> {
                    navController.navigate(R.id.nav_weather)
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateBottomNavVisibility()
    }

    fun updateBottomNavVisibility() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main)
        val currentFragment = navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

        bottomNavigationView.visibility = if (currentFragment is SoilFragment ||
            currentFragment is WeatherFragment ||
            currentFragment is AirFragment || currentFragment is PlantDiseaseFragment) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }


    fun logout() {
        FirebaseAuth.getInstance().signOut()
        Intent(this, LoginActivity::class.java).also {
            it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(it)
            finish()
        }
    }
    fun openDrawer() {
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            if (navController.currentDestination?.id == R.id.nav_dash) {
                super.onBackPressed()
            } else {
                navController.navigate(R.id.nav_dash)
                updateBottomNavVisibility()
            }
        }
    }
}
