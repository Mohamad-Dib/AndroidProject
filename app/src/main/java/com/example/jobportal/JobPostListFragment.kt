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
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.e("JobPostListFragment", "No job postings found.")
                    Toast.makeText(requireContext(), "No job postings found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in documents) {
                        val job = document.toObject(JobPosting::class.java)
                        if (job != null) {
                            jobPostList.add(job)
                        }
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
