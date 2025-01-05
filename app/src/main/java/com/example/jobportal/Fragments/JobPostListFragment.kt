package com.example.jobportal.Fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.jobportal.Adapters.JobPostAdapter
import com.example.jobportal.Models.JobPosting
import com.example.jobportal.R
import com.example.jobportal.Repositories.JobRepository
import java.text.DateFormat

class JobPostListFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var jobPostAdapter: JobPostAdapter
    private val jobPostList = mutableListOf<JobPosting>()
    private val jobRepository = JobRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_job_post_list_fragment, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.jobPostRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        jobPostAdapter = JobPostAdapter(jobPostList) { jobPosting ->
            showJobDetailsDialog(jobPosting)
        }
        recyclerView.adapter = jobPostAdapter

        // Swipe to Refresh Layout
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        // Fetch initial job postings
        fetchJobPosts()

        swipeRefreshLayout.setOnRefreshListener {
            fetchJobPosts {
                swipeRefreshLayout.isRefreshing = false
            }
        }

        return view
    }

    private fun fetchJobPosts(onComplete: (() -> Unit)? = null) {
        jobRepository.fetchJobPostings(
            onSuccess = { jobPostings ->
                jobPostList.clear()
                jobPostList.addAll(jobPostings)
                jobPostAdapter.notifyDataSetChanged()
                onComplete?.invoke()
            },
            onFailure = { exception ->
                Toast.makeText(requireContext(), "Failed to load job postings: ${exception.message}", Toast.LENGTH_SHORT).show()
                onComplete?.invoke()
            }
        )
    }

    private fun showJobDetailsDialog(jobPosting: JobPosting) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_job_details, null)

        // Initialize TextViews and Buttons
        val jobTitleTextView = dialogView.findViewById<TextView>(R.id.jobTitleTextView)
        val companyTextView = dialogView.findViewById<TextView>(R.id.companyNameTextView)
        val locationTextView = dialogView.findViewById<TextView>(R.id.locationTextView)
        val jobDescriptionTextView = dialogView.findViewById<TextView>(R.id.jobDescriptionTextView)
        val requiredSkillsTextView = dialogView.findViewById<TextView>(R.id.requiredSkillsTextView)
        val salaryTextView = dialogView.findViewById<TextView>(R.id.salaryTextView)
        val jobTypeTextView = dialogView.findViewById<TextView>(R.id.jobTypeTextView)
        val postedDateTextView = dialogView.findViewById<TextView>(R.id.postedDateTextView)
        val applyNowButton = dialogView.findViewById<Button>(R.id.applyNowButton)
        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)

        // Set data
        jobTitleTextView.text = jobPosting.jobTitle
        companyTextView.text = "Company: ${jobPosting.companyName}"
        locationTextView.text = "Location: ${jobPosting.location}"
        jobDescriptionTextView.text = "Description: ${jobPosting.jobDescription}"
        requiredSkillsTextView.text = "Required Skills: ${jobPosting.requiredSkills}"
        salaryTextView.text = jobPosting.salary?.let { "Salary: $it" } ?: "Salary: Not Specified"
        jobTypeTextView.text = "Job Type: ${jobPosting.jobType}"
        postedDateTextView.text = "Posted Date: ${DateFormat.getDateInstance().format(jobPosting.postedDate)}"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        applyNowButton.setOnClickListener {
            val userId = jobRepository.getCurrentUserId()
            if (userId != null) {
                jobRepository.getCurrentUserProfile(
                    userId = userId,
                    onSuccess = { userProfile ->
                        if (userProfile != null) {
                            val applicationData = mapOf(
                                "JobTitle" to jobPosting.jobTitle,
                                "EmployerId" to jobPosting.postedBy,
                                "SeekerId" to userId,
                                "DateTime" to System.currentTimeMillis(),
                                "ApplicationTitle" to "${userProfile["firstName"] ?: "Unknown"} ${userProfile["lastName"] ?: ""} applied to your job posting",
                                "seekerCv" to (userProfile["cvUrl"] as? String ?: "Not Available"),
                                "profileImageUrl" to (userProfile["profileImageUrl"] as? String ?: "Not Available")
                            )

                            jobRepository.submitApplication(
                                applicationData = applicationData,
                                onSuccess = {
                                    Toast.makeText(requireContext(), "Application submitted successfully!", Toast.LENGTH_SHORT).show()
                                    dialog.dismiss()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(requireContext(), "Failed to submit application: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            Toast.makeText(requireContext(), "User profile not found.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onFailure = { exception ->
                        Toast.makeText(requireContext(), "Error fetching user profile: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
                )

            } else {
                Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }
}
