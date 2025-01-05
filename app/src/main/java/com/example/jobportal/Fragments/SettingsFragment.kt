package com.example.jobportal.Fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.jobportal.Dialogs.JobPostingsDialog
import com.example.jobportal.Login
import com.example.jobportal.R
import com.example.jobportal.Repositories.JobRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot


class SettingsFragment : Fragment() {

    private val jobRepository = JobRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_settings_fragement, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views using findViewById
        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val viewJobPostingsButton = view.findViewById<Button>(R.id.viewJobPostingsButton)

        // Logout Button
        logoutButton.setOnClickListener {
            logoutUser()
        }

        // View My Job Postings Button
        viewJobPostingsButton.setOnClickListener {
            fetchAndShowJobPostings(view)
        }
    }

    private fun logoutUser() {
        jobRepository.logoutUser()
        startActivity(Intent(requireContext(), Login::class.java))
        requireActivity().finish()
    }

    private fun fetchAndShowJobPostings(view: View) {
        val userId = jobRepository.getCurrentUserId()

        if (userId == null) {
            showSnackbar(view, "User is not logged in.")
            return
        }

        jobRepository.fetchJobPostingsByUser(
            userId = userId,
            onSuccess = { result ->
                if (result.isEmpty) {
                    showSnackbar(view, "No job postings found.")
                } else {
                    showJobPostingsDialog(result)
                }
            },
            onFailure = { exception ->
                showSnackbar(view, "Error fetching job postings: ${exception.message}")
            }
        )
    }

    private fun showJobPostingsDialog(jobPostings: QuerySnapshot) {
        val dialog = JobPostingsDialog.newInstance(jobPostings.documents)
        dialog.show(parentFragmentManager, "JobPostingsDialog")
    }

    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
}