package com.example.jobportal

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.FirebaseFirestore
class JobPostingsDialog : DialogFragment() {

    companion object {
        // Create a new instance of the dialog, passing in a list of DocumentSnapshots
        fun newInstance(jobPostings: List<DocumentSnapshot>): JobPostingsDialog {
            val dialog = JobPostingsDialog()
            val args = Bundle()
            args.putSerializable("jobPostings", ArrayList(jobPostings)) // Pass list of DocumentSnapshots
            dialog.arguments = args
            return dialog
        }
    }

    private lateinit var jobPostings: List<DocumentSnapshot>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Retrieve the list of DocumentSnapshots from arguments
        val documents = arguments?.getSerializable("jobPostings") as? ArrayList<*>
        jobPostings = documents?.filterIsInstance<DocumentSnapshot>() ?: emptyList()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.activity_job_postings_dialog, null)

        val jobPostingsList = dialogView.findViewById<LinearLayout>(R.id.jobPostingsList)

        // Loop through each job posting and add it to the list
        for (document in jobPostings) {
            val jobTitle = document.getString("jobTitle") ?: "No Title"
            val jobId = document.id

            // Dynamically create TextView for each job posting
            val jobTitleText = TextView(requireContext())
            jobTitleText.text = jobTitle
            jobTitleText.setPadding(16, 16, 16, 16)

            // Optionally, you can add a delete button next to each job title if required
            val deleteButton = Button(requireContext())
            deleteButton.text = "Delete"
            deleteButton.setOnClickListener {
                deleteJobPosting(jobId)
            }

            // Create a linear layout to hold the TextView and delete button
            val jobLayout = LinearLayout(requireContext())
            jobLayout.orientation = LinearLayout.HORIZONTAL
            jobLayout.addView(jobTitleText)
            jobLayout.addView(deleteButton)

            // Add the layout to the dialog's LinearLayout
            jobPostingsList.addView(jobLayout)
        }

        builder.setView(dialogView)
            .setPositiveButton("Close") { _, _ -> }
            .setCancelable(true)

        return builder.create()
    }

    private fun deleteJobPosting(jobId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("JobPosting").document(jobId).delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Job posting deleted successfully", Toast.LENGTH_SHORT).show()
                dismiss() // Close the dialog after deletion
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error deleting job posting: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
