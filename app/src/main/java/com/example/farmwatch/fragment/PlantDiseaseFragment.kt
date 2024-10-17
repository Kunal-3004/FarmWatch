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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.example.farmwatch.HomeActivity
import com.example.farmwatch.R
import com.example.farmwatch.databinding.FragmentPlantdiseaseBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class PlantDiseaseFragment : Fragment() {
    private lateinit var inputImageView: ImageView
    private lateinit var outputTextView: TextView
    private lateinit var photoFile: File
    private lateinit var imageLabeler: ImageLabeler
    private val REQUEST_PICK_IMAGE = 1000
    private val REQUEST_CAPTURE_IMAGE = 1001
    private var _binding: FragmentPlantdiseaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plantdisease, container, false)
        inputImageView = view.findViewById(R.id.imageView)
        outputTextView = view.findViewById(R.id.textView)

        val localModel = LocalModel.Builder()
            .setAssetFilePath("model_flowers.tflite")
            .build()
        val options = CustomImageLabelerOptions.Builder(localModel)
            .setConfidenceThreshold(0.7f)
            .setMaxResultCount(5)
            .build()
        imageLabeler = ImageLabeling.getClient(options)

        if (PermissionChecker.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 0)
        }

        // Set click listeners for picking an image or starting the camera
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

        val toolbar: Toolbar = binding.toolbar
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
        val fileUri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.fileprovider",
            photoFile
        )
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
        startActivityForResult(intent, REQUEST_CAPTURE_IMAGE)
    }

    private fun createPhotoFile(): File {
        val photoFile = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES), "IMAGE_HELPER")
        if (!photoFile.exists()) {
            photoFile.mkdirs()
        }
        val date = Date()
        val name = SimpleDateFormat("ddMMyyyy_HHmmss").format(date)
        return File(photoFile.path + File.separator + name)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            val bitmap: Bitmap? = when (requestCode) {
                REQUEST_PICK_IMAGE -> data?.data?.let { loadFromUri(it) }
                REQUEST_CAPTURE_IMAGE -> BitmapFactory.decodeFile(photoFile.absolutePath)
                else -> null
            }
            bitmap?.let {
                inputImageView.setImageBitmap(it)
                runClassifier(it)
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
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        imageLabeler.process(inputImage)
            .addOnSuccessListener { imageLabels ->
                if (imageLabels.isNotEmpty()) {
                    val builder = StringBuilder()
                    for (label in imageLabels) {
                        builder.append(label.text)
                            .append(": ")
                            .append(label.confidence)
                            .append("\n")
                    }
                    outputTextView.text = builder.toString()
                } else {
                    outputTextView.text = "Could not classify"
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
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