Job Portal Android App

Overview
The Job Portal Android app connects job seekers and employers seamlessly. It enables employers to post job openings and manage applicants while allowing job seekers to search for job opportunities and apply directly within the app.

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
Asynchronous Firebase Operations:

Challenge: Ensuring UI responsiveness while interacting with Firebase.
Solution: Used callbacks and LiveData to handle Firebase operations and update the UI seamlessly.
Image and File Management:

Challenge: Handling profile image uploads and CV uploads.
Solution: Integrated Cloudinary for image uploads and Firebase Storage for CVs with status feedback.
Dark Mode:

Challenge: Dynamic theme switching.
Solution: Used AppCompatDelegate for theme toggling and persisted user preferences using SharedPreferences.
Setup Instructions
1. Clone the Repository
bash
Copy code
git clone https://github.com/Mohamad-Dib/AndroidProject.git
cd AndroidProject
2. Configure Firebase
Go to the Firebase Console.
Create a Firebase project.
Download the google-services.json file for your project and place it in the app/ directory.
3. Configure Cloudinary
Sign up for a Cloudinary account.
Add your Cloudinary credentials (cloud name, API key, and API secret) to the MyApplication.kt file.
4. Build and Run
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

Contact
For inquiries, reach out to Mohamad Dib via GitHub.

