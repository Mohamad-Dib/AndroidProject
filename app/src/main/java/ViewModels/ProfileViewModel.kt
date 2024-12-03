package ViewModels

import androidx.lifecycle.ViewModel
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
class ProfileViewModel:ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _profileData = MutableLiveData<Map<String, Any>>()
    val profileData: LiveData<Map<String, Any>> = _profileData

    private val _photoUploadStatus = MutableLiveData<String>()
    val photoUploadStatus: LiveData<String> = _photoUploadStatus

    private val _cvUploadStatus = MutableLiveData<String>()
    val cvUploadStatus: LiveData<String> = _cvUploadStatus

    private val _cvDownloadUrl = MutableLiveData<String>()
    val cvDownloadUrl: LiveData<String> = _cvDownloadUrl

    fun loadProfileData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        _profileData.postValue(document.data ?: emptyMap())
                    }
                }
                .addOnFailureListener {
                    _profileData.postValue(emptyMap())
                }
        }
    }

    fun saveProfileData(data: Map<String, Any>) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("users").document(userId).set(data)
                .addOnSuccessListener {
                    _profileData.postValue(data)
                }
        }
    }

    fun uploadPhoto(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("profile_photos/$userId/photo.jpg")

        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    db.collection("users").document(userId)
                        .update("profileImageUrl", uri.toString())
                        .addOnSuccessListener {
                            _photoUploadStatus.postValue("Photo uploaded successfully!")
                        }
                        .addOnFailureListener {
                            _photoUploadStatus.postValue("Failed to save photo URL!")
                        }
                }
            }
            .addOnFailureListener {
                _photoUploadStatus.postValue("Photo upload failed: ${it.message}")
            }
    }

    fun uploadCv(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val ref = storage.reference.child("cvs/$userId/cv.pdf")

        ref.putFile(fileUri)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { uri ->
                    db.collection("users").document(userId)
                        .update("cvUrl", uri.toString())
                        .addOnSuccessListener {
                            _cvUploadStatus.postValue("CV uploaded successfully!")
                            _cvDownloadUrl.postValue(uri.toString())
                        }
                        .addOnFailureListener {
                            _cvUploadStatus.postValue("Failed to save CV URL!")
                        }
                }
            }
            .addOnFailureListener {
                _cvUploadStatus.postValue("CV upload failed: ${it.message}")
            }
    }

}