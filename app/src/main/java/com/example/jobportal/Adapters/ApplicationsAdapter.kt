package com.example.jobportal.Adapters

import com.example.jobportal.Models.JobApplication
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jobportal.R

class ApplicationsAdapter(
    private val applications: List<JobApplication>
) : RecyclerView.Adapter<ApplicationsAdapter.ApplicationsViewHolder>() {

    class ApplicationsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        val applicationTitleTextView: TextView = itemView.findViewById(R.id.applicationTitleTextView)
        val downloadCvButton: Button = itemView.findViewById(R.id.downloadCvButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.application_item, parent, false)
        return ApplicationsViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApplicationsViewHolder, position: Int) {
        val application = applications[position]

        Log.d("ApplicationsAdapter", "Binding Application: $application")

        // Set application title
        holder.applicationTitleTextView.text = application.applicationTitle
        // Load profile photo using Glide
        Glide.with(holder.itemView.context)
            .load(application.profileImageUrl)
            .placeholder(R.drawable.ic_default_profile) // Placeholder image

            .into(holder.profileImageView)

        // Handle CV download button click
        holder.downloadCvButton.setOnClickListener {
            Log.d("ApplicationsAdapter", "Download CV URL: ${application.seekerCv}")
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(application.seekerCv)
            holder.itemView.context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = applications.size
}
