package com.example.jobportal

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Observe profile completeness
        sharedViewModel.isProfileComplete.observe(this) { isComplete ->
            if (isComplete == true) {
                switchFragment(JobPostingFragment()) // Switch to HomeFragment when profile is complete
            } else {
                switchFragment(ProfileFragment()) // Lock user in ProfileFragment otherwise
            }
        }

        // Check profile completeness when the activity starts
        checkProfileCompletion()

        // Handle navigation item selection
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    switchFragment(ProfileFragment())
                    true
                }
                R.id.nav_home -> {
                    if (sharedViewModel.isProfileComplete.value == true) {
                        switchFragment(JobPostingFragment())
                    } else {
                        showToast("Please complete your profile first.")
                    }
                    true
                }
                else -> false
            }
        }
    }


    private fun checkProfileCompletion() {
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

                    sharedViewModel.isProfileComplete.value = isComplete
                }
                .addOnFailureListener { e ->
                    showToast("Error checking profile: ${e.message}")
                    sharedViewModel.isProfileComplete.value = false // Treat as incomplete on failure
                }
        } else {
            sharedViewModel.isProfileComplete.value = false
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment?.javaClass != fragment.javaClass) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}