package com.example.jobportal
import android.app.Application
import com.cloudinary.android.MediaManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Cloudinary MediaManager
        val config = mapOf(
            "cloud_name" to "db9vfak9v",
            "api_key" to "254762354156986",
            "api_secret" to "J8lqtGSWIS3uqtMnQgHghoplK70"
        )
        MediaManager.init(this, config)
    }
}
