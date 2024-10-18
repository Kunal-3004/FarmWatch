package com.example.farmwatch

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.farmwatch.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        binding.fullnameTextView.text = document.getString("fullname") ?: "N/A"
                        binding.emailTextView.text = document.getString("email") ?: "N/A"
                        binding.numberTextView.text = document.getString("number") ?: "N/A"
                        binding.cityTextView.text = document.getString("city") ?: "N/A"

                        val profilePicUrl = document.getString("profilePicture")
                        if (!profilePicUrl.isNullOrEmpty()) {
                            Glide.with(this)
                                .load(profilePicUrl)
                                .into(binding.profilePicture)
                        } else {
                            binding.profilePicture.setImageResource(R.drawable.ic_user_profile)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error getting user data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        binding.editProfileButton.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivity(intent)
        }
    }
}
