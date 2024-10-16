package com.example.farmwatch

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager.widget.ViewPager
import com.example.farmwatch.introViewPager.IntroAdapter

class IntroActivity : AppCompatActivity() {
    private lateinit var viewPager:ViewPager
    private lateinit var layout: LinearLayout
    private lateinit var right: Button
    private lateinit var left: Button
    private lateinit var adapter: IntroAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_intro)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        left = findViewById(R.id.left)
        right = findViewById(R.id.right)

        left.visibility = View.INVISIBLE

        layout = findViewById(R.id.indicatorLayout)
        viewPager = findViewById(R.id.pager)

        adapter = IntroAdapter(this)

        viewPager.adapter = adapter


        left.setOnClickListener {
            if (getItem(0) > 0) {
                viewPager.setCurrentItem(getItem(-1), true)
            }
        }
        right.setOnClickListener {
            if (getItem(0) < 4) {
                viewPager.setCurrentItem(getItem(1), true)

            }
        }

        setupIndicator(0)
        viewPager.addOnPageChangeListener(viewListener)
    }

    fun setupIndicator(position:Int){
        val dots = arrayOfNulls<TextView?>(5)
        layout.removeAllViews()
        for (i in dots.indices){
            dots[i] = TextView(this)
            dots[i]?.text = Html.fromHtml("&#8226", Html.FROM_HTML_MODE_LEGACY)
            dots[i]?.textSize = 30F
            dots[i]?.setTextColor(resources.getColor(R.color.inactive, this.theme))
            layout.addView(dots[i])
        }
        dots[position]?.setTextColor(resources.getColor(R.color.active, this.theme))
    }

    val viewListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            return
        }

        override fun onPageSelected(position: Int) {
            setupIndicator(position)
            if (position>0){
                left.visibility = View.VISIBLE
            }else{
                left.visibility = View.INVISIBLE
            }

            if (position<4){
                right.visibility = View.VISIBLE
            }else{
                right.visibility = View.INVISIBLE
            }
        }

        override fun onPageScrollStateChanged(state: Int) {
            return
        }

    }

    fun getItem(i:Int): Int {
        return viewPager.currentItem + i
    }
}