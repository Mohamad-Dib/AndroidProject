package com.example.jobportal.Repositories


import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileRepository {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    fun fetchUserProfile(onSuccess: (Map<String, Any>?) -> Unit, onFailure: (Exception) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    onSuccess(document.data)
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } else {
            onFailure(Exception("User not authenticated"))
        }
    }

    fun updateUserProfile(profileData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .set(profileData)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        } else {
            onComplete(false)
        }
    }

    fun isProfileComplete(onComplete: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val isComplete = document.exists() &&
                            !document.getString("firstName").isNullOrEmpty() &&
                            !document.getString("lastName").isNullOrEmpty() &&
                            !document.getString("dob").isNullOrEmpty() &&
                            !document.getString("phoneNumber").isNullOrEmpty() &&
                            !document.getString("address").isNullOrEmpty()
                    onComplete(isComplete)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } else {
            onComplete(false)
        }
    }


    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun loadUserProfile(
        userId: String,
        onSuccess: (Map<String, Any?>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onSuccess(document.data ?: emptyMap())
                } else {
                    onFailure(Exception("Profile not found"))
                }
            }
            .addOnFailureListener(onFailure)
    }

    fun saveUserProfile(
        userId: String,
        profileData: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId).set(profileData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun uploadFileToStorage(
        path: String,
        fileUri: Uri,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val storageReference = storage.reference.child(path)
        val uploadTask = storageReference.putFile(fileUri)

        uploadTask.addOnSuccessListener {
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                onSuccess(uri.toString())
            }
        }.addOnFailureListener(onFailure)
    }

    fun updateProfileField(
        userId: String,
        field: String,
        value: Any,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        firestore.collection("users").document(userId).update(field, value)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}
