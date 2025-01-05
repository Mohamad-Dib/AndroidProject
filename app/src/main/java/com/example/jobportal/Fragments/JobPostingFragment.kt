package com.example.jobportal.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.example.jobportal.R
import com.example.jobportal.Repositories.JobRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class JobPostingFragment : Fragment() {

    private val jobRepository = JobRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_job_posting, container, false)

        // UI references
        val jobTitleEditText = view.findViewById<EditText>(R.id.jobTitleEditText)
        val companyEditText = view.findViewById<EditText>(R.id.companyEditText)
        val locationEditText = view.findViewById<EditText>(R.id.locationEditText)
        val jobDescriptionEditText = view.findViewById<EditText>(R.id.jobDescriptionEditText)
        val requiredSkillsEditText = view.findViewById<EditText>(R.id.requiredSkillsEditText)
        val salaryEditText = view.findViewById<EditText>(R.id.salaryEditText)
        val jobTypeSpinner = view.findViewById<Spinner>(R.id.jobTypeSpinner)
        val saveJobButton = view.findViewById<Button>(R.id.saveJobButton)

        saveJobButton.setOnClickListener {
            val jobTitle = jobTitleEditText.text.toString()
            val companyName = companyEditText.text.toString()
            val location = locationEditText.text.toString()
            val jobDescription = jobDescriptionEditText.text.toString()
            val requiredSkills = requiredSkillsEditText.text.toString()
            val salary = salaryEditText.text.toString()
            val jobType = jobTypeSpinner.selectedItem.toString()

            if (jobTitle.isNotEmpty() && companyName.isNotEmpty() && location.isNotEmpty()) {
                val userId = jobRepository.getCurrentUserId()
                if (userId != null) {
                    val jobMap = mapOf(
                        "jobTitle" to jobTitle,
                        "companyName" to companyName,
                        "location" to location,
                        "jobDescription" to jobDescription,
                        "requiredSkills" to requiredSkills,
                        "salary" to salary,
                        "jobType" to jobType,
                        "postedBy" to userId,
                        "postedDate" to System.currentTimeMillis()
                    )

                    jobRepository.addJobPosting(
                        jobDetails = jobMap,
                        onSuccess = {
                            Toast.makeText(requireContext(), "Job posted successfully!", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { exception ->
                            Toast.makeText(requireContext(), "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill all required fields.", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}