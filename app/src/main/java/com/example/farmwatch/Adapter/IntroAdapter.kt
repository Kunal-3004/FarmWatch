package com.example.farmwatch.Adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.farmwatch.Data.IntroData
import com.example.farmwatch.R

class IntroAdapter(private val introSlides: List<IntroData>): RecyclerView.Adapter<IntroAdapter.IntroViewHolder>() {
    inner class IntroViewHolder(view: View) : RecyclerView.ViewHolder(view){

        private val textTitle = view.findViewById<TextView>(R.id.sliderTitle)
        private val textDescription = view.findViewById<TextView>(R.id.sliderDescription)
        private val imageIcon = view.findViewById<ImageView>(R.id.imageSlider)

        fun bind(introSlider: IntroData){
            textTitle.text = introSlider.title
            textDescription.text = introSlider.description
            imageIcon.setImageResource(introSlider.image)

        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntroViewHolder {
        return IntroViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.slider_screen, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return introSlides.size
    }

    override fun onBindViewHolder(holder: IntroViewHolder, position: Int) {
        holder.bind(introSlides[position])
    }
}