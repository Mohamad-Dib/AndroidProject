package com.example.jobportal.Models
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
data class JobPosting(
    val jobTitle: String = "",
    val companyName: String = "",
    val location: String = "",
    val jobDescription: String = "",
    val requiredSkills: String = "",
    val salary: String? = null,
    val jobType: String = "",
    val postedBy: String = "",
    val postedDate: Long = 0L
)

