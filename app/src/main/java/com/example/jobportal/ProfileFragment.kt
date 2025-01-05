package com.example.jobportal

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
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabase: FirebaseFirestore
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var loadingProgressBar: ProgressBar  // Reference to ProgressBar


    private var currentPublicId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        initializeFirebase()

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
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)


        // Load existing profile data
        loadProfileData(
            firstNameEditText, middleNameEditText, lastNameEditText,
            dobEditText, phoneEditText, addressEditText, profileImageView
        )

        // Set up DatePicker for DOB
        setupDatePicker(dobEditText)

        // Save profile data
        saveButton.setOnClickListener {
            saveProfileData(
                firstNameEditText.text.toString(),
                middleNameEditText.text.toString(),
                lastNameEditText.text.toString(),
                dobEditText.text.toString(),
                phoneEditText.text.toString(),
                addressEditText.text.toString()
            )
        }

        // Upload profile photo
        uploadPhotoButton.setOnClickListener {
            pickPhoto()
        }
        uploadCvButton.setOnClickListener {
            pickCv()
        }

        return view
    }


    private fun initializeFirebase() {
        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseFirestore.getInstance()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }
    private fun showLoading() {
        loadingProgressBar.visibility = View.VISIBLE
        // Hide the UI elements (replace R.id.fragment_profile with the actual ID of the profile container)
        view?.findViewById<View>(R.id.profileImageView)?.visibility = View.GONE
    }

    private fun hideLoading() {
        loadingProgressBar.visibility = View.GONE
        // Show the UI elements once the profile is loaded
        view?.findViewById<View>(R.id.profileImageView)?.visibility = View.VISIBLE
    }




    private fun setupDatePicker(dobEditText: EditText) {
        dobEditText.setOnClickListener {
            val calendar = Calendar.getInstance()

            // Parse existing date if available
            dobEditText.text?.toString()?.let { dobText ->
                if (dobText.isNotEmpty()) {
                    try {
                        val parts = dobText.split("-")
                        calendar.set(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    dobEditText.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).apply {
                datePicker.maxDate = Calendar.getInstance().timeInMillis
            }.show()
        }
    }

    private fun loadProfileData(
        firstNameEditText: EditText,
        middleNameEditText: EditText,
        lastNameEditText: EditText,
        dobEditText: EditText,
        phoneEditText: EditText,
        addressEditText: EditText,
        profileImageView: ImageView
    ) {
        val userId = auth.currentUser?.uid
        userId?.let {
            userDatabase.collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstNameEditText.setText(document.getString("firstName"))
                        middleNameEditText.setText(document.getString("middleName"))
                        lastNameEditText.setText(document.getString("lastName"))
                        dobEditText.setText(document.getString("dob"))
                        phoneEditText.setText(document.getString("phoneNumber"))
                        addressEditText.setText(document.getString("address"))
                        currentPublicId = document.getString("profileImagePublicId")
                        val profileImageUrl = document.getString("profileImageUrl")
                        if (!profileImageUrl.isNullOrEmpty()) {
                            Glide.with(this).load(profileImageUrl).circleCrop().into(profileImageView)
                        }
                        val cvUrl = document.getString("cvUrl")
                        val cvStatusTextView = view?.findViewById<TextView>(R.id.cvUploadStatusTextView)
                        if (!cvUrl.isNullOrEmpty()) {
                            cvStatusTextView?.text = "CV uploaded. Tap to download."
                            cvStatusTextView?.setOnClickListener {
                                downloadCv(cvUrl)
                            }
                        } else {
                            cvStatusTextView?.text = "No CV uploaded yet."
                        }

                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun pickCv() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf" // For CVs as PDFs
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_PICK_CV)
    }

    private fun saveProfileData(
        firstName: String,
        middleName: String,
        lastName: String,
        dob: String,
        phoneNumber: String,
        address: String
    ) {
        if (firstName.isNotEmpty() && lastName.isNotEmpty() && dob.isNotEmpty() && phoneNumber.isNotEmpty() && address.isNotEmpty()) {
            val userMap = mapOf(
                "firstName" to firstName,
                "middleName" to middleName,
                "lastName" to lastName,
                "dob" to dob,
                "phoneNumber" to phoneNumber,
                "address" to address
            )
            auth.currentUser?.uid?.let { userId ->
                userDatabase.collection("users").document(userId).set(userMap, SetOptions.merge())
                    .addOnSuccessListener {
                        sharedViewModel.isProfileComplete.value = true
                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "Please fill out all required fields.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickPhoto() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent, "Select Profile Photo"), REQUEST_CODE_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK_IMAGE && resultCode == AppCompatActivity.RESULT_OK) {
            val imageUri = data?.data
            if (imageUri != null) {
                deleteOldPhotoAndUpload(imageUri)
            } else {
                Toast.makeText(requireContext(), "Image selection failed", Toast.LENGTH_SHORT).show()
            }
        }
        if (requestCode == REQUEST_CODE_PICK_CV && resultCode == AppCompatActivity.RESULT_OK) {
            val fileUri = data?.data
            if (fileUri != null) {
                uploadCvToFirebase(fileUri)
            } else {
                Toast.makeText(requireContext(), "CV selection failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteOldPhotoAndUpload(imageUri: Uri) {
        if (!currentPublicId.isNullOrEmpty()) {
            Thread {
                try {
                    val result = MediaManager.get().cloudinary.uploader()
                        .destroy(currentPublicId, mapOf("invalidate" to true))
                    if (result["result"] == "ok") {
                        requireActivity().runOnUiThread {
                            uploadPhoto(imageUri)
                        }
                    } else {
                        requireActivity().runOnUiThread {
                            Toast.makeText(requireContext(), "Failed to delete old photo", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        Toast.makeText(requireContext(), "Error deleting old photo: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        } else {
            uploadPhoto(imageUri)
        }
    }

    private fun uploadPhoto(imageUri: Uri) {
        MediaManager.get().upload(imageUri)
            .option("folder", "profile_photos/")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {
                    Toast.makeText(requireContext(), "Uploading photo...", Toast.LENGTH_SHORT).show()
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    val progress = (bytes.toFloat() / totalBytes) * 100
                    println("Upload Progress: $progress%")
                }

                override fun onSuccess(requestId: String?, resultData: Map<*, *>) {
                    val secureUrl = resultData["secure_url"] as String
                    val publicId = resultData["public_id"] as String
                    savePhotoUrl(secureUrl, publicId)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Upload failed: ${error?.description}", Toast.LENGTH_SHORT).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    Toast.makeText(requireContext(), "Upload rescheduled: ${error?.description}", Toast.LENGTH_SHORT).show()
                }
            })
            .dispatch()
    }

    private fun savePhotoUrl(photoUrl: String, publicId: String) {
        auth.currentUser?.uid?.let { userId ->
            userDatabase.collection("users").document(userId)
                .update(
                    mapOf(
                        "profileImageUrl" to photoUrl,
                        "profileImagePublicId" to publicId
                    )
                )
                .addOnSuccessListener {
                    view?.findViewById<ImageView>(R.id.profileImageView)?.let {
                        Glide.with(this).load(photoUrl).into(it)
                    }
                    currentPublicId = publicId
                    Toast.makeText(requireContext(), "Profile photo updated successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save photo URL: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
    private fun uploadCvToFirebase(fileUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val storageReference = FirebaseStorage.getInstance().reference.child("cvs/$userId/cv.pdf")
            val uploadTask = storageReference.putFile(fileUri)

            uploadTask.addOnSuccessListener {
                storageReference.downloadUrl.addOnSuccessListener { downloadUrl ->
                    saveCvUrlToFirestore(downloadUrl.toString())
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "CV upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }
    private fun saveCvUrlToFirestore(cvUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                .update("cvUrl", cvUrl)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "CV uploaded successfully!", Toast.LENGTH_SHORT).show()
                    view?.findViewById<TextView>(R.id.cvUploadStatusTextView)?.text = "CV uploaded successfully"
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save CV info: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    private fun downloadCv(cvUrl: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(cvUrl))
        startActivity(browserIntent)
    }
    companion object {
        private const val REQUEST_CODE_PICK_IMAGE = 101
        private const val REQUEST_CODE_PICK_CV = 102

    }
}