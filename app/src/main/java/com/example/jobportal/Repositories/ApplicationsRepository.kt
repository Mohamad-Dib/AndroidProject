package com.example.jobportal.Repositories

import com.example.jobportal.Models.JobApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ApplicationsRepository {


        private val auth: FirebaseAuth = FirebaseAuth.getInstance()
        private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

        fun fetchApplications(
            onSuccess: (List<JobApplication>) -> Unit,
            onFailure: (Exception) -> Unit
        ) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val employerId = currentUser.uid

                firestore.collection("Applications")
                    .whereEqualTo("EmployerId", employerId)
                    .orderBy("DateTime", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener { documents ->
                        val applications = documents.map { it.toObject(JobApplication::class.java) }
                        onSuccess(applications)
                    }
                    .addOnFailureListener { exception ->
                        onFailure(exception)
                    }
            } else {
                onFailure(Exception("User not authenticated"))
            }
        }

}