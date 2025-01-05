package com.example.jobportal.Fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jobportal.Adapters.ApplicationsAdapter
import com.example.jobportal.Models.JobApplication
import com.example.jobportal.R
import com.example.jobportal.Repositories.ApplicationsRepository

class ApplicationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var applicationsAdapter: ApplicationsAdapter
    private val applicationsList = mutableListOf<JobApplication>()

    private val applicationsRepository = ApplicationsRepository()

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

        // Fetch Applications
        fetchApplications()

        return view
    }

    private fun fetchApplications() {
        applicationsRepository.fetchApplications(
            onSuccess = { applications ->
                applicationsList.clear()
                applicationsList.addAll(applications)
                applicationsAdapter.notifyDataSetChanged()
                if (applications.isEmpty()) {
                    Toast.makeText(requireContext(), "No applications found for your jobs.", Toast.LENGTH_SHORT).show()
                }
            },
            onFailure = { exception ->
                Log.e("ApplicationsFragment", "Error fetching applications: ${exception.message}", exception)
                Toast.makeText(requireContext(), "Failed to fetch applications.", Toast.LENGTH_SHORT).show()
            }
        )
    }
}