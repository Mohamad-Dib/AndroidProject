package com.example.jobportal.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jobportal.Models.JobPosting
import com.example.jobportal.R

class JobPostAdapter(
    private val jobPosts: List<JobPosting>,
    private val onItemClick: (JobPosting) -> Unit
) : RecyclerView.Adapter<JobPostAdapter.JobPostViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    class JobPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.jobTitleTextView)
        val company: TextView = itemView.findViewById(R.id.companyNameTextView)
        val location: TextView = itemView.findViewById(R.id.locationTextView)
//        val detailsContainer: ViewGroup = itemView.findViewById(R.id.detailsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JobPostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.job_post_item, parent, false)
        return JobPostViewHolder(view)
    }


    //    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
//        val job = jobPosts[position]
//
//        holder.title.text = job.jobTitle
//        holder.company.text = job.companyName
//        holder.location.text = job.location
//
//        // Toggle details visibility based on the expanded state
////        holder.detailsContainer.visibility = if (expandedPositions.contains(position)) View.VISIBLE else View.GONE
//
//        holder.itemView.setOnClickListener {
//            if (expandedPositions.contains(position)) {
//                expandedPositions.remove(position)
//            } else {
//                expandedPositions.add(position)
//            }
//            notifyItemChanged(position) // Update only the clicked item
//        }
//    }
    override fun onBindViewHolder(holder: JobPostViewHolder, position: Int) {
        val job = jobPosts[position]
        holder.title.text = job.jobTitle
        holder.company.text = job.companyName
        holder.location.text = job.location
        holder.itemView.setOnClickListener {
            onItemClick(job)
        }
//    holder.optionalDetails.text = "Response time is typically 4 days" // Customize this as needed
    }

    override fun getItemCount(): Int = jobPosts.size
}
