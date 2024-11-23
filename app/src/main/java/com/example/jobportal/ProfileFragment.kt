package com.example.jobportal

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var userDatabase: FirebaseFirestore
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()
        userDatabase = FirebaseFirestore.getInstance()
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]


        // UI references
        val firstNameEditText = view.findViewById<EditText>(R.id.firstNameEditText)
        val middleNameEditText = view.findViewById<EditText>(R.id.middleNameEditText)
        val lastNameEditText = view.findViewById<EditText>(R.id.lastNameEditText)
        val dobEditText = view.findViewById<EditText>(R.id.dobEditText)
        val phoneEditText = view.findViewById<EditText>(R.id.phoneEditText)
        val addressEditText = view.findViewById<EditText>(R.id.addressEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)


        dobEditText.setOnClickListener {
            // Get current date
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            // Show DatePickerDialog
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format date as YYYY-MM-DD
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    dobEditText.setText(formattedDate) // Set selected date to dobEditText
                },
                year,
                month,
                day
            )

            // Optional: Restrict the user to past dates only
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis

            datePickerDialog.show()
        }

        dobEditText.setOnClickListener {
            // Get current date as default
            val calendar = Calendar.getInstance()

            // Check if dobEditText already has a date
            val dobText = dobEditText.text.toString()
            if (dobText.isNotEmpty()) {
                // Try to parse the existing date (assumes format YYYY-MM-DD)
                try {
                    val parts = dobText.split("-")
                    val year = parts[0].toInt()
                    val month = parts[1].toInt() - 1 // Months are 0-based in Calendar
                    val day = parts[2].toInt()
                    calendar.set(year, month, day)
                } catch (e: Exception) {
                    // If parsing fails, use the current date
                    e.printStackTrace()
                }
            }

            // Show DatePickerDialog with pre-filled date
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    // Format selected date as YYYY-MM-DD
                    val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                    dobEditText.setText(formattedDate) // Set selected date to dobEditText
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            // Optional: Restrict to past dates
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis

            datePickerDialog.show()
        }

        // Load user info from Firebase
        auth.currentUser?.let { user ->
            userDatabase.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        firstNameEditText.setText(document.getString("firstName"))
                        middleNameEditText.setText(document.getString("middleName"))
                        lastNameEditText.setText(document.getString("lastName"))
                        dobEditText.setText(document.getString("dob"))
                        phoneEditText.setText(document.getString("phoneNumber"))
                        addressEditText.setText(document.getString("address"))
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to load profile: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Save updated user info
        saveButton.setOnClickListener {
            val firstName = firstNameEditText.text.toString()
            val middleName = middleNameEditText.text.toString()
            val lastName = lastNameEditText.text.toString()
            val dob = dobEditText.text.toString()
            val phoneNumber = phoneEditText.text.toString()
            val address = addressEditText.text.toString()

            if (firstName.isNotEmpty() && lastName.isNotEmpty() && dob.isNotEmpty() && phoneNumber.isNotEmpty() && address.isNotEmpty()) {
                val userMap = hashMapOf(
                    "firstName" to firstName,
                    "middleName" to middleName,
                    "lastName" to lastName,
                    "dob" to dob,
                    "phoneNumber" to phoneNumber,
                    "address" to address
                )

                userDatabase.collection("users").document(auth.currentUser!!.uid).set(userMap)
                    .addOnSuccessListener {
                        sharedViewModel.isProfileComplete.value = true
                        Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Error updating profile: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(requireContext(), "Please fill out all required fields.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
