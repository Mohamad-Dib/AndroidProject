package ViewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
class MainViewModel:ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _isProfileComplete = MutableLiveData<Boolean>()
    val isProfileComplete: LiveData<Boolean> = _isProfileComplete

    fun checkProfileCompletion() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val isComplete = document.exists() &&
                            !document.getString("firstName").isNullOrEmpty() &&
                            !document.getString("lastName").isNullOrEmpty() &&
                            !document.getString("dob").isNullOrEmpty() &&
                            !document.getString("phoneNumber").isNullOrEmpty() &&
                            !document.getString("address").isNullOrEmpty()
                    _isProfileComplete.postValue(isComplete)
                }
                .addOnFailureListener {
                    _isProfileComplete.postValue(false) // Treat as incomplete on failure
                }
        } else {
            _isProfileComplete.postValue(false)
        }
    }



}