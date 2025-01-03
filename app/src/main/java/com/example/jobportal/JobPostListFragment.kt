package com.example.jobportal

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jobportal.databinding.ActivityJobPostListFragmentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JobPostListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jobPostAdapter: JobPostAdapter
    private val jobPostList = mutableListOf<JobPosting>()

    private lateinit var jobDatabase: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_job_post_list_fragment, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.jobPostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobPostAdapter = JobPostAdapter(jobPostList) { jobPosting ->
            // Inflate the custom dialog layout
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_job_details, null)

            // Initialize all the TextViews and the close button
            val jobTitleTextView = dialogView.findViewById<TextView>(R.id.jobTitleTextView)
            val companyTextView = dialogView.findViewById<TextView>(R.id.companyNameTextView)
            val locationTextView = dialogView.findViewById<TextView>(R.id.locationTextView)
            val jobDescriptionTextView = dialogView.findViewById<TextView>(R.id.jobDescriptionTextView)
            val requiredSkillsTextView = dialogView.findViewById<TextView>(R.id.requiredSkillsTextView)
            val salaryTextView = dialogView.findViewById<TextView>(R.id.salaryTextView)
            val jobTypeTextView = dialogView.findViewById<TextView>(R.id.jobTypeTextView)
            val postedByTextView = dialogView.findViewById<TextView>(R.id.postedByTextView)
            val postedDateTextView = dialogView.findViewById<TextView>(R.id.postedDateTextView)
            val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)
            val applyNowButton = dialogView.findViewById<Button>(R.id.applyNowButton)

            postedByTextView.visibility = View.GONE

// Set data to the TextViews
            jobTitleTextView.text = jobPosting.jobTitle
            companyTextView.text = "Company: ${jobPosting.companyName}"
            locationTextView.text = "Location: ${jobPosting.location}"
            jobDescriptionTextView.text = "Description: ${jobPosting.jobDescription}"
            requiredSkillsTextView.text = "Required Skills: ${jobPosting.requiredSkills}"
            salaryTextView.text = jobPosting.salary?.let { "Salary: $it" } ?: "Salary: Not Specified"
            jobTypeTextView.text = "Job Type: ${jobPosting.jobType}"
            postedByTextView.text = "Posted By: ${jobPosting.postedBy}"
            postedDateTextView.text = "Posted Date: ${java.text.DateFormat.getDateInstance().format(jobPosting.postedDate)}"


            // Create and show the dialog
            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            // Close button listener
            closeButton.setOnClickListener {
                dialog.dismiss()
            }

            applyNowButton.setOnClickListener {
                val auth = FirebaseAuth.getInstance()
                val currentUser = auth.currentUser

                if (currentUser != null) {
                    val seekerId = currentUser.uid // Authenticated user's UID

                    // Fetch additional user details from Firestore
                    jobDatabase.collection("users").document(seekerId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Fetch firstName, lastName, cvUrl, and profileImageUrl
                                val firstName = document.getString("firstName") ?: "Unknown"
                                val lastName = document.getString("lastName") ?: ""
                                val cvUrl = document.getString("cvUrl") ?: "Not Available"
                                val profileImageUrl = document.getString("profileImageUrl") ?: "Not Available"

                                // Create application title
                                val applicationTitle = "$firstName $lastName applied to your ${jobPosting.jobTitle} post. Click to download their CV."

                                // Prepare application data
                                val applicationData = hashMapOf(
                                    "JobTitle" to jobPosting.jobTitle, // Job title
                                    "EmployerId" to jobPosting.postedBy, // Employer ID
                                    "SeekerId" to seekerId, // Authenticated user's UID
                                    "DateTime" to System.currentTimeMillis(), // Application timestamp
                                    "ApplicationTitle" to applicationTitle, // Custom application message
                                    "seekerCv" to cvUrl, // CV URL
                                    "profileImageUrl" to profileImageUrl // Profile photo URL
                                )

                                // Save application to Firestore
                                jobDatabase.collection("Applications").add(applicationData)
                                    .addOnSuccessListener {
                                        Toast.makeText(requireContext(), "Applied successfully!", Toast.LENGTH_SHORT).show()
                                        dialog.dismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(requireContext(), "Failed to apply: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(requireContext(), "User profile not found.", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to fetch user details: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
                }
            }

            dialog.show()
        }

        recyclerView.adapter = jobPostAdapter

        // Initialize Firestore
        jobDatabase = FirebaseFirestore.getInstance()

        // Swipe to Refresh Layout
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        // Fetch initial job postings
        fetchJobPosts()

        swipeRefreshLayout.setOnRefreshListener {
            fetchJobPosts {
                swipeRefreshLayout.isRefreshing = false // Stop refreshing animation
            }
        }

        return view
    }

    private fun fetchJobPosts(onComplete: (() -> Unit)? = null) {
        // Clear the list to avoid duplicates
        jobPostList.clear()

        jobDatabase.collection("JobPosting")
            .orderBy("postedDate", com.google.firebase.firestore.Query.Direction.DESCENDING) // Newer to older

            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.e("JobPostListFragment", "No job postings found.")
                    Toast.makeText(requireContext(), "No job postings found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val job = document.toObject(JobPosting::class.java)
                        jobPostList.add(job)
                    }
                    jobPostAdapter.notifyDataSetChanged()
                }
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("JobPostListFragment", "Error fetching job postings: ${e.message}", e)
                Toast.makeText(requireContext(), "Failed to load job postings.", Toast.LENGTH_SHORT).show()
                onComplete?.invoke()
            }
    }
}
