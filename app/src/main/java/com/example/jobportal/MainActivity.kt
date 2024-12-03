package com.example.jobportal

import ViewModels.MainViewModel
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Observe profile completeness
        viewModel.isProfileComplete.observe(this) { isComplete ->
            if (isComplete) {
                switchFragment(JobPostingFragment()) // Switch to HomeFragment when profile is complete
            } else {
                switchFragment(ProfileFragment()) // Lock user in ProfileFragment otherwise
            }
        }

        // Handle navigation item selection
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    switchFragment(ProfileFragment())
                    true
                }
                R.id.nav_home -> {
                    if (viewModel.isProfileComplete.value == true) {
                        switchFragment(JobPostingFragment())
                    } else {
                        showToast("Please complete your profile first.")
                    }
                    true
                }
                else -> false
            }
        }

        // Check profile completeness on startup
        viewModel.checkProfileCompletion()
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
