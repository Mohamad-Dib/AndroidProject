package com.example.jobportal.Fragments

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.jobportal.R
import com.example.jobportal.Repositories.ProfileRepository
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var repository: ProfileRepository
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        repository = ProfileRepository()
        progressBar = view.findViewById(R.id.loadingProgressBar)

        // UI References
        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val dobEditText = view.findViewById<EditText>(R.id.dobEditText)
        val phoneEditText = view.findViewById<EditText>(R.id.phoneEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val profileImageView = view.findViewById<ImageView>(R.id.profileImageView)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val uploadCvButton = view.findViewById<Button>(R.id.uploadCvButton)

        // Load Profile Data
        loadProfileData(
            firstNameEditText, lastNameEditText, dobEditText,
            phoneEditText, addressEditText, profileImageView
        )

        // Date Picker
        setupDatePicker(dobEditText)

        // Save Profile
        saveButton.setOnClickListener {
            val userId = repository.getCurrentUserId()
            if (userId != null) {
                val profileData = mapOf(
                    "firstName" to firstNameEditText.text.toString(),
                    "lastName" to lastNameEditText.text.toString(),
                    "dob" to dobEditText.text.toString(),
                    "phoneNumber" to phoneEditText.text.toString(),
                    "address" to addressEditText.text.toString()
                )
                saveProfileData(userId, profileData)
            }
        }

        // Upload CV
        uploadCvButton.setOnClickListener {
            pickFileForCv()
        }

        return view
    }

    private fun setupDatePicker(dobEditText: EditText) {
        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    dobEditText.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun loadProfileData(
        firstNameEditText: EditText,
        lastNameEditText: EditText,
        dobEditText: EditText,
        phoneEditText: EditText,
        addressEditText: EditText,
        profileImageView: ImageView
    ) {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            repository.loadUserProfile(
                userId,
                onSuccess = { profile ->
                    firstNameEditText.setText(profile["firstName"] as? String ?: "")
                    lastNameEditText.setText(profile["lastName"] as? String ?: "")
                    dobEditText.setText(profile["dob"] as? String ?: "")
                    phoneEditText.setText(profile["phoneNumber"] as? String ?: "")
                    addressEditText.setText(profile["address"] as? String ?: "")
                    val profileImageUrl = profile["profileImageUrl"] as? String
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(this).load(profileImageUrl).circleCrop().into(profileImageView)
                    }
                },
                onFailure = { exception ->
                    Toast.makeText(requireContext(), "Failed to load profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    private fun saveProfileData(userId: String, profileData: Map<String, Any>) {
        progressBar.visibility = View.VISIBLE
        repository.saveUserProfile(
            userId,
            profileData,
            onSuccess = {
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { exception ->
                progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Failed to update profile: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun pickFileForCv() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/pdf"
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_CV)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_CV && resultCode == AppCompatActivity.RESULT_OK) {
            val fileUri = data?.data
            if (fileUri != null) {
                uploadCv(fileUri)
            }
        }
    }

    private fun uploadCv(fileUri: Uri) {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            repository.uploadFileToStorage(
                "cvs/$userId/cv.pdf",
                fileUri,
                onSuccess = { downloadUrl ->
                    repository.updateProfileField(
                        userId,
                        "cvUrl",
                        downloadUrl,
                        onSuccess = {
                            Toast.makeText(requireContext(), "CV uploaded successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { exception ->
                            Toast.makeText(requireContext(), "Failed to upload CV: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onFailure = { exception ->
                    Toast.makeText(requireContext(), "CV upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    companion object {
        private const val REQUEST_CODE_PICK_CV = 102
    }
}
