package com.example.jobportal

import JobApplication
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ApplicationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private val applicationsList = mutableListOf<JobApplication>()

    private lateinit var applicationsDatabase: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_applications, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.applicationsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        applicationsAdapter = ApplicationsAdapter(applicationsList)
        recyclerView.adapter = applicationsAdapter

        // Initialize Firestore
        applicationsDatabase = FirebaseFirestore.getInstance()

        // Fetch Applications
        fetchApplications()

        return view
    }

    private fun fetchApplications() {
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val employerId = currentUser.uid

            applicationsDatabase.collection("Applications")
                .whereEqualTo("EmployerId", employerId)
                .orderBy("DateTime", com.google.firebase.firestore.Query.Direction.DESCENDING) // Newer to older
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(requireContext(), "No applications found for your jobs.", Toast.LENGTH_SHORT).show()
                    } else {
                        for (document in documents) {
                            val application = document.toObject(JobApplication::class.java)
                            Log.d("ApplicationsFragment", "Fetched Application Title: ${application.applicationTitle}")
                            applicationsList.add(application)
                        }
                        applicationsAdapter.notifyDataSetChanged()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ApplicationsFragment", "Error fetching applications: ${e.message}", e)
                    Toast.makeText(requireContext(), "Failed to fetch applications.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show()
        }
    }



}
