package com.example.jobportal

import ViewModels.LoginViewModel
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        if (viewModel.auth.currentUser != null && viewModel.auth.currentUser?.isEmailVerified == true) {
            navigateToDashboard()
            return
        }

        setContentView(R.layout.activity_login)

        // Find views
        val emailEditText = findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = findViewById<EditText>(R.id.passwordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        registerButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.registerUser(email, password)
            } else {
                showToast("Please fill out all fields.")
            }
        }

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                viewModel.loginUser(email, password)
            } else {
                showToast("Please fill out all fields.")
            }
        }

        // Observe ViewModel
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.registrationResult.observe(this) { result ->
            result.onSuccess { message ->
                showToast(message)
            }.onFailure { exception ->
                showToast("Registration failed: ${exception.message}")
            }
        }

        viewModel.loginResult.observe(this) { result ->
            result.onSuccess { user ->
                showToast("Login successful!")
                navigateToDashboard()
            }.onFailure { exception ->
                showToast(exception.message ?: "Login failed.")
            }
        }

        viewModel.emailVerificationSent.observe(this) { result ->
            result.onSuccess {
                showToast("Verification email sent! Please check your inbox.")
            }.onFailure { exception ->
                showToast("Failed to send verification email: ${exception.message}")
            }
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
