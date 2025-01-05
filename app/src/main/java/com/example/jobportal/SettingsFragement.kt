package com.example.jobportal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
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

        // Add more settings-related options here if needed
    }

    private fun logoutUser() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(requireContext(), Login::class.java)) // Replace with your LoginActivity
        requireActivity().finish()
    }

    private fun fetchAndShowJobPostings(view: View) {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            showSnackbar(view, "User is not logged in.")
            return
        }

        db.collection("JobPosting")
            .whereEqualTo("postedBy", userId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    showSnackbar(view, "No job postings found.")
                } else {
                    showJobPostingsDialog(result)
                }
            }
            .addOnFailureListener { exception ->
                showSnackbar(view, "Error fetching job postings: ${exception.message}")
            }
    }

    private fun showJobPostingsDialog(jobPostings: QuerySnapshot) {
        // Pass the documents to the dialog
        val dialog = JobPostingsDialog.newInstance(jobPostings.documents) // Pass documents directly
        dialog.show(parentFragmentManager, "JobPostingsDialog")
    }




    private fun showSnackbar(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }
}
