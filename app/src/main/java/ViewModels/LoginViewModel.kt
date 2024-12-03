package ViewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
class LoginViewModel : ViewModel() {
    val auth = FirebaseAuth.getInstance()

    private val _loginResult = MutableLiveData<Result<FirebaseUser?>>()
    val loginResult: LiveData<Result<FirebaseUser?>> = _loginResult

    private val _registrationResult = MutableLiveData<Result<String>>()
    val registrationResult: LiveData<Result<String>> = _registrationResult

    private val _emailVerificationSent = MutableLiveData<Result<Boolean>>()
    val emailVerificationSent: LiveData<Result<Boolean>> = _emailVerificationSent

    fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendEmailVerification()
                    _registrationResult.postValue(Result.success("Registration successful"))
                } else {
                    _registrationResult.postValue(Result.failure(task.exception ?: Exception("Unknown error")))
                }
            }
    }

    fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user?.isEmailVerified == true) {
                        _loginResult.postValue(Result.success(user))
                    } else {
                        _loginResult.postValue(Result.failure(Exception("Email not verified")))
                        auth.signOut()
                    }
                } else {
                    _loginResult.postValue(Result.failure(task.exception ?: Exception("Unknown error")))
                }
            }
    }

    private fun sendEmailVerification() {
        auth.currentUser?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _emailVerificationSent.postValue(Result.success(true))
                } else {
                    _emailVerificationSent.postValue(Result.failure(task.exception ?: Exception("Unknown error")))
                }
            }
    }
}