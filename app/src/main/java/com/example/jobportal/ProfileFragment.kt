package com.example.jobportal

import ViewModels.ProfileViewModel
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // UI references
        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val middleNameEditText = view.findViewById<EditText>(R.id.middleNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val dobEditText = view.findViewById<EditText>(R.id.dobEditText)
        val phoneEditText = view.findViewById<EditText>(R.id.phoneEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val uploadPhotoButton = view.findViewById<Button>(R.id.uploadImageButton)
        val uploadCvButton = view.findViewById<Button>(R.id.uploadCvButton)

        // Observe profile data
        viewModel.profileData.observe(viewLifecycleOwner) { data ->
            firstNameEditText.setText(data["firstName"] as? String)
            middleNameEditText.setText(data["middleName"] as? String)
            lastNameEditText.setText(data["lastName"] as? String)
            dobEditText.setText(data["dob"] as? String)
            phoneEditText.setText(data["phoneNumber"] as? String)
            addressEditText.setText(data["address"] as? String)
            val profileImageUrl = data["profileImageUrl"] as? String
            if (!profileImageUrl.isNullOrEmpty()) {
                Glide.with(this).load(profileImageUrl).circleCrop().into(profileImageView)
            }
        }

        // Observe photo upload status
        viewModel.photoUploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
        }

        // Observe CV upload status
        viewModel.cvUploadStatus.observe(viewLifecycleOwner) { status ->
            Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
        }

        // Load profile data
        viewModel.loadProfileData()

        // Save profile data
        saveButton.setOnClickListener {
            val data = mapOf(
                "firstName" to firstNameEditText.text.toString(),
                "middleName" to middleNameEditText.text.toString(),
                "lastName" to lastNameEditText.text.toString(),
                "dob" to dobEditText.text.toString(),
                "phoneNumber" to phoneEditText.text.toString(),
                "address" to addressEditText.text.toString()
            )
            viewModel.saveProfileData(data)
        }

        // Upload profile photo
        uploadPhotoButton.setOnClickListener {
            pickPhoto()
        }

        // Upload CV
        uploadCvButton.setOnClickListener {
            pickCv()
        }

        return view
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE)
    }

    private fun pickCv() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_CV)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fileUri = data?.data
        when (requestCode) {
            REQUEST_CODE_PICK_IMAGE -> fileUri?.let { viewModel.uploadPhoto(it) }
            REQUEST_CODE_PICK_CV -> fileUri?.let { viewModel.uploadCv(it) }
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 101
        private const val REQUEST_CODE_PICK_CV = 102
    }
}
