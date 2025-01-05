Job Portal Android App
Overview
The Job Portal Android app connects job seekers and employers seamlessly. It enables employers to post job openings and manage applicants while allowing job seekers to search for job opportunities and apply directly within the app.

This project was developed as part of an Android Development Course at Saint Joseph University of Beirut (USJ). It demonstrates the application of modern Android development practices and tools.

Features
Authentication: Secure login and registration using Firebase Authentication.
Job Posting Management:
Employers can create, view, and manage job postings.
Job seekers can browse job postings and apply directly.
Profile Management:
Users can update their profiles with personal details, a profile picture, and a CV.
Settings Management:
View job postings.
Logout functionality.
Tech Stack
Languages
Kotlin
Libraries and Tools
Firebase:
Firestore (Database)
Authentication
Storage
Cloudinary:
For profile image uploads and management.
Jetpack Components:
ViewModel, LiveData, ConstraintLayout
Glide:
Efficient image loading and caching.
Material Design:
For a polished and user-friendly interface.
Architecture
MVVM (Model-View-ViewModel):
Ensures a clean separation of concerns.
Repository Pattern:
Centralizes data access logic for maintainability.
Challenges and Solutions
Asynchronous Firebase Operations
Challenge: Ensuring UI responsiveness while interacting with Firebase.
Solution: Used callbacks and LiveData to handle Firebase operations and update the UI seamlessly.
Image and File Management
Challenge: Handling profile image uploads and CV uploads.
Solution: Integrated Cloudinary for image uploads and Firebase Storage for CVs with status feedback.
Setup Instructions
Clone the Repository
git clone https://github.com/Mohamad-Dib/AndroidProject.git
cd AndroidProject

Configure Firebase

Go to the Firebase Console: https://console.firebase.google.com/
Create a new project.
Enable the following Firebase services in the project:
Authentication:
Go to the Authentication tab.
Enable Email/Password sign-in under Sign-in methods.
Firestore Database:
Navigate to Firestore Database.
Click on "Create Database" and choose "Start in test mode."
Storage:
Navigate to Storage.
Click on "Get Started" and set up the storage bucket.
Download the google-services.json file from the Firebase project settings.
Place the google-services.json file in the app/ directory of your project.
Configure Cloudinary

Sign up for a Cloudinary account: https://cloudinary.com/
Generate your API credentials (cloud name, API key, and API secret).
Add the credentials to the MyApplication.kt file in the following format:
val config = mapOf(
"cloud_name" to "your-cloud-name",
"api_key" to "your-api-key",
"api_secret" to "your-api-secret"
)
MediaManager.init(this, config)
Build and Run

Open the project in Android Studio.
Sync Gradle files.
Connect an Android device or emulator.
Click Run.
Contributing
Contributions are welcome! To contribute:

Fork the repository.
Create a new branch for your feature or bugfix.
Commit your changes and push the branch.
Open a pull request.
License
This project is licensed under the MIT License. See the LICENSE file for details.

Acknowledgments
This project was developed as part of the Android Development Course at Saint Joseph University of Beirut (USJ). Special thanks to my teammate Mohamad Mokbel for collaborating on this project and contributing significantly to its development.

Contact
For inquiries, reach out to Mohamad Dib via GitHub.

