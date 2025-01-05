package com.example.jobportal.Repositories

import com.example.jobportal.Models.JobPosting
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

class JobRepository {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun addJobPosting(jobPosting: JobPosting, onComplete: (Boolean) -> Unit) {
        firestore.collection("JobPosting")
            .add(jobPosting)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun addJobPosting(
        jobDetails: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("JobPosting")
            .add(jobDetails)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun fetchJobPostings(
        onSuccess: (List<JobPosting>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("JobPosting")
            .orderBy("postedDate", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val jobPostings = documents.toObjects(JobPosting::class.java)
                onSuccess(jobPostings)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun submitApplication(
        applicationData: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("Applications")
            .add(applicationData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun getCurrentUserProfile(
        userId: String,
        onSuccess: (Map<String, Any>?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val data = document.data // This is already a Map<String, Any> or null
                onSuccess(data)
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }


    fun fetchJobPostingsByUser(
        userId: String,
        onSuccess: (QuerySnapshot) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("JobPosting")
            .whereEqualTo("postedBy", userId)
            .get()
            .addOnSuccessListener(onSuccess)
            .addOnFailureListener(onFailure)
    }

    fun logoutUser() {
        auth.signOut()
    }

}