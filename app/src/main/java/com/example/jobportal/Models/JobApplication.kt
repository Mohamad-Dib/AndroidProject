package com.example.jobportal.Models

import com.google.firebase.firestore.PropertyName

data class JobApplication(
    @PropertyName("ApplicationTitle") val applicationTitle: String = "",
    @PropertyName("profileImageUrl") val profileImageUrl: String = "",
    @PropertyName("seekerCv") val seekerCv: String = "",
    @PropertyName("DateTime") val dateTime: Long = 0L,
    @PropertyName("EmployerId") val employerId: String = "",
    @PropertyName("JobTitle") val jobTitle: String = "",
    @PropertyName("SeekerId") val seekerId: String = ""
)
