package ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.jobportal.Models.JobPosting
import com.example.jobportal.Repositories.JobRepository
import com.example.jobportal.Repositories.ProfileRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class MainViewModel:ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val jobRepository = JobRepository()
    private val profileRepository = ProfileRepository()

    private val _jobPostings = MutableLiveData<List<JobPosting>>()
    val jobPostings: LiveData<List<JobPosting>> = _jobPostings

    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> = _isProfileComplete


    fun checkProfileCompletion() {
        profileRepository.isProfileComplete { isComplete ->
            _isProfileComplete.postValue(isComplete)
        }
    }

    fun fetchJobPostings() {
        jobRepository.fetchJobPostings(
            onSuccess = { jobs -> _jobPostings.postValue(jobs) },
            onFailure = { /* Handle error */ }
        )
    }



}