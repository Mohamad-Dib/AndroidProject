package com.example.jobportal

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val isProfileComplete: MutableLiveData<Boolean> = MutableLiveData(false)
}
