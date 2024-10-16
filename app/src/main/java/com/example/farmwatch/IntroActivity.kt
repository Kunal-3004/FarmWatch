package com.example.farmwatch

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.farmwatch.Adapter.IntroAdapter
import com.example.farmwatch.Data.IntroData
import androidx.viewpager2.widget.ViewPager2
import com.example.farmwatch.databinding.ActivityIntroBinding
import com.google.firebase.auth.FirebaseAuth

class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth

    private val introSliderAdapter = IntroAdapter(
        listOf(
            IntroData(
                "Welcome to the\n\bFarm Watch App\b",
                "Best Guide and Helper for any Farmer. Provides various features at one place!",
                R.drawable.intro_first
            ),
            IntroData(
                "Read Articles",
                "Read Online articles related to Farming Concepts, Technologies and other useful knowledge.",
                R.drawable.intro_read
            ),
            IntroData(
                "Weather Prediction",
                "Get Notified for  Weather Conditions.",
                R.drawable.intro_weather
            ),
            IntroData(
                "Soil Analysis",
                "Get Notified for Soil Conditions.",
                R.drawable.intro_soil
            ),
            IntroData(
                "Air Quality",
                "Get Notified for Air Quality.",
                R.drawable.intro_air
            ),
            IntroData(
                "Crop Production Analysis",
                "Get Notified for agricultural data to evaluate crop yield, productivity, and performance across different regions and seasons.",
                R.drawable.intro_cpa
            ),
            IntroData(
                "Let's Grow Together",
                "- Farm Watch App",
                R.drawable.intro_help
            )

        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }

        val nextBtn = binding.nextBtn
        val sliderViewPager=binding.sliderViewPager
        val skipIntro=binding.skipIntro



        sliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)
        sliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })

        if(sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount){
            Log.d("IntroActivity", sliderViewPager.currentItem.toString())
            Log.d("IntroActivity", introSliderAdapter.itemCount.toString())
            binding.nextBtn.text = "Get Started"
        } else{
            nextBtn.text = "Next"
        }

        nextBtn.setOnClickListener {
            if (sliderViewPager.currentItem + 1 < introSliderAdapter.itemCount) {
                sliderViewPager.currentItem += 1
                nextBtn.text = "Next"
                if(sliderViewPager.currentItem + 1 == introSliderAdapter.itemCount){
                    Log.d("IntroActivity", sliderViewPager.currentItem.toString())
                    Log.d("IntroActivity", introSliderAdapter.itemCount.toString())
                    nextBtn.text = "Get Started"
                }
            } else {

                Intent(this, LoginActivity::class.java).also {
                    startActivity(it)
                }
                val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean("firstTime", false)
                editor.apply()
                finish()
            }
        }
        skipIntro.setOnClickListener {
            Intent(this, LoginActivity::class.java).also {
                startActivity(it)
            }
            val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putBoolean("firstTime", false)
            editor.apply()
            finish()
        }
    }

    private fun setupIndicators() {
        val sliderballs_container=binding.sliderballsContainer
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams: LinearLayout.LayoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }

            sliderballs_container.addView(indicators[i])


        }

    }

    private fun setCurrentIndicator(index: Int) {
        val sliderballs_container=binding.sliderballsContainer
        val nextBtn=binding.nextBtn
        val childCount = sliderballs_container.childCount
        for (i in 0 until childCount) {
            val imageView = sliderballs_container.get(i) as ImageView
            if (i == index) {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            } else {
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }

        if(index == introSliderAdapter.itemCount - 1){
            nextBtn.text = "Get Started"
        } else{
            nextBtn.text = "Next"
        }
    }
}