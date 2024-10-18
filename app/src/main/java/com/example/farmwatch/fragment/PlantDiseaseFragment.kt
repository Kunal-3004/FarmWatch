package com.example.farmwatch.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.farmwatch.Api.PlantAPIResponse
import com.example.farmwatch.Api.PlantApiInstance
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.FragmentPlantdiseaseBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class PlantDiseaseFragment : Fragment() {
    private lateinit var inputImageView: ImageView
    private lateinit var outputTextView: TextView
    private lateinit var photoFile: File
    private val plantViewModel: PlantViewModel by viewModels()
    private val REQUEST_PICK_IMAGE = 1000
    private val REQUEST_CAPTURE_IMAGE = 1001
    private var _binding: FragmentPlantdiseaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlantdiseaseBinding.inflate(inflater, container, false)
        val view = binding.root

        if (PermissionChecker.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                0)
        }

        inputImageView = view.findViewById(R.id.imageView)
        outputTextView = view.findViewById(R.id.textView)

        view.findViewById<View>(R.id.button).setOnClickListener {
            onPickImage()
        }
        view.findViewById<View>(R.id.button2).setOnClickListener {
            onStartCamera()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        plantViewModel.response.observe(requireActivity()) { apiResponse ->
            outputTextView.text = apiResponse.text
        }

        val toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).supportActionBar?.setHomeButtonEnabled(true)
        toolbar.setNavigationIcon(R.drawable.ic_menu)
        toolbar.setNavigationOnClickListener {
            (activity as HomeActivity).openDrawer()
        }
    }

    private fun onPickImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_PICK_IMAGE)
    }

    private fun onStartCamera() {
        photoFile = createPhotoFile()
        val fileUri = FileProvider.getUriForFile(requireContext(), "com.example.fileprovider", photoFile)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
    }

    private fun createPhotoFile(): File {
        val photoDir = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMAGE_HELPER")
        if (!photoDir.exists()) {
            photoDir.mkdirs()
        }

        val date = Date()
        val name = SimpleDateFormat("ddMMyyyy_HHmmss").format(date)
        return File(photoDir, "$name.png")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_IMAGE -> {
                    val uri = data?.data
                    val bitmap = uri?.let { loadFromUri(it) }
                    inputImageView.setImageBitmap(bitmap)
                    bitmap?.let { runClassifier(it) }
                }
                REQUEST_CAPTURE_IMAGE -> {
                    Log.d("ImageClassifier", "Received callback from camera")
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    inputImageView.setImageBitmap(bitmap)
                    runClassifier(bitmap)
                }
            }
        }
    }

    private fun loadFromUri(uri: Uri): Bitmap? {
        return try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O_MR1) {
                val source = ImageDecoder.createSource(requireContext().contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun runClassifier(bitmap: Bitmap) {
        val base64Image = bitmapToBase64(bitmap)
        plantViewModel.uploadImage(base64Image)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).updateBottomNavVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class PlantViewModel : ViewModel() {
        private val _response = MutableLiveData<PlantAPIResponse>()
        val response: LiveData<PlantAPIResponse> get() = _response

        fun uploadImage(base64Image: String) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val apiResponse = PlantApiInstance.api.uploadImage(base64Image)
                    if (apiResponse.isSuccessful && apiResponse.body() != null) {
                        _response.postValue(apiResponse.body())
                    } else {
                        Log.e("PlantViewModel", "API Response Error: ${apiResponse.message()}")
                    }
                } catch (e: Exception) {
                    Log.e("PlantViewModel", "Exception: ${e.message}")
                }
            }
        }
    }
}
