package com.example.farmwatch

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.farmwatch.databinding.ActivityEditBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class EditActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var profilePicUri: Uri? = null
    private val IMAGE_PICK_CODE =1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.profilePictureEdit.setOnClickListener {
            pickImageFromGallery()
        }

        binding.saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            profilePicUri = data?.data
            binding.profilePictureEdit.setImageURI(profilePicUri)
        }
    }

    private fun saveProfileData() {
        val userId = auth.currentUser?.uid
        val userUpdates = hashMapOf<String, Any>(
            "fullname" to binding.fullName.text.toString(),
            "number" to binding.phoneEdt.text.toString(),
            "city" to binding.cityEdt.text.toString(),
        )
        profilePicUri?.let { uri ->
            uploadProfilePicture(uri) { url ->
                userUpdates["profilePicture"] = url
                updateUserInFirestore(userId, userUpdates)
            }
        } ?: updateUserInFirestore(userId, userUpdates)
    }

    private fun uploadProfilePicture(uri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures/${auth.currentUser?.uid}.jpg")
        storageRef.putFile(uri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    onSuccess(downloadUrl.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserInFirestore(userId: String?, updates: HashMap<String, Any>) {
        userId?.let {
            db.collection("users").document(it).update(updates)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
