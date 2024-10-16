package com.example.farmwatch.introViewPager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.PagerAdapter
import com.example.farmwatch.R

class IntroAdapter(val context: Context): PagerAdapter() {

    val images = listOf<Int>()
    val texts = listOf("","")

    override fun getCount(): Int {
        return texts.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ConstraintLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any{
        val layoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = layoutInflater.inflate(R.layout.slider_layout, container, false)

        view.findViewById<ImageView>(R.id.intro_img).setImageResource(images[position])
        view.findViewById<TextView>(R.id.intro_text).text = texts[position]

        container.addView(view)

        return view
    }
}