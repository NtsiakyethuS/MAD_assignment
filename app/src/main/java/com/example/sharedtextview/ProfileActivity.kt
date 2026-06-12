/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Profile Screen and load the current user's data from the database.
 * 2. Update Profile:
 *    - Collect modified fields (Name, Address, School info).
 *    - Update the user record in the database.
 * 3. Security (Password Update):
 *    - Compare "New Password" and "Verify Password" fields.
 *    - If they match, update the user's password in the database.
 * 4. User Interaction (Feedback & Complaints):
 *    - Show dialogs to collect ratings or complaint details.
 *    - Save these entries into the admin database tables.
 * 5. Logout & Account Management:
 *    - Clear "Remember Me" preferences on logout.
 *    - Allow permanent account deletion (removes user from database).
 * 6. Admin Access:
 *    - If the user has admin rights, show the "Admin Dashboard" button.
 */

package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.User
import com.example.sharedtextview.database.Complaint
import com.example.sharedtextview.database.Feedback
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * ProfileActivity manages user account details, settings, and support requests.
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText
    private lateinit var physicalAddress: EditText
    private lateinit var postalAddress: EditText
    private lateinit var university: EditText
    private lateinit var campus: EditText
    private lateinit var faculty: EditText
    private lateinit var newPassword: EditText
    private lateinit var verifyPassword: EditText
    
    private var currentUserEmail: String? = null

    // PSEUDO: Launcher for updating profile picture
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImage.setImageURI(it)
        }
    }

    /**
     * Initializes UI and loads current profile data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        
        currentUserEmail = intent.getStringExtra("USER_EMAIL")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        profileImage = findViewById(R.id.profileImage)
        val btnChangePicture = findViewById<Button>(R.id.btnChangePicture)
        firstName = findViewById(R.id.profileFirstName)
        lastName = findViewById(R.id.profileLastName)
        email = findViewById(R.id.profileEmail)
        physicalAddress = findViewById(R.id.profilePhysicalAddress)
        postalAddress = findViewById(R.id.profilePostalAddress)
        university = findViewById(R.id.profileUniversity)
        campus = findViewById(R.id.profileCampus)
        faculty = findViewById(R.id.profileFaculty)
        newPassword = findViewById(R.id.profileNewPassword)
        verifyPassword = findViewById(R.id.profileVerifyPassword)

        val btnUpdate = findViewById<Button>(R.id.btnUpdateProfile)
        val btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)
        val btnDelete = findViewById<Button>(R.id.btnDeleteAccount)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnComplaint = findViewById<Button>(R.id.btnSubmitComplaint)
        val btnFeedback = findViewById<Button>(R.id.btnSubmitFeedback)
        val btnAdmin = findViewById<Button>(R.id.btnAdminDashboard)

        val db = AppDatabase.getDatabase(this)
        
        // PSEUDO: Fetch user info from database and fill text fields
        currentUserEmail?.let { userEmail ->
            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(userEmail)
                user?.let {
                    firstName.setText(it.firstName)
                    lastName.setText(it.lastName)
                    email.setText(it.email)
                    physicalAddress.setText(it.physicalAddress)
                    postalAddress.setText(it.postalAddress)
                    university.setText(it.university)
                    campus.setText(it.campus)
                    faculty.setText(it.faculty)
                    
                    // PSEUDO: Show Admin Dashboard button if user is an administrator
                    if (it.isAdmin || it.email == "root" || it.email == "admin@textbook.com") {
                        btnAdmin.visibility = View.VISIBLE
                    }
                }
            }
        }

        btnAdmin.setOnClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
        }

        // PSEUDO: Complaint Dialog Logic
        btnComplaint.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Submit Complaint")
            
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)
            
            val etSubject = EditText(this)
            etSubject.hint = "Subject"
            layout.addView(etSubject)
            
            val etMessage = EditText(this)
            etMessage.hint = "Detail your complaint..."
            etMessage.minLines = 3
            layout.addView(etMessage)
            
            builder.setView(layout)
            builder.setPositiveButton("Submit") { _, _ ->
                val subject = etSubject.text.toString()
                val message = etMessage.text.toString()
                if (subject.isNotEmpty() && message.isNotEmpty()) {
                    lifecycleScope.launch {
                        db.adminDao().insertComplaint(Complaint(userEmail = currentUserEmail!!, subject = subject, message = message))
                        Toast.makeText(this@ProfileActivity, "Complaint submitted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        // PSEUDO: Feedback/Rating Dialog Logic
        btnFeedback.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Rate our App")
            
            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.setPadding(50, 40, 50, 10)
            layout.gravity = android.view.Gravity.CENTER_HORIZONTAL
            
            val ratingBar = RatingBar(this)
            ratingBar.numStars = 5
            ratingBar.stepSize = 1.0f
            layout.addView(ratingBar)
            
            val etComment = EditText(this)
            etComment.hint = "Your feedback..."
            layout.addView(etComment)
            
            builder.setView(layout)
            builder.setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating.toInt()
                val comment = etComment.text.toString()
                lifecycleScope.launch {
                    db.adminDao().insertFeedback(Feedback(userEmail = currentUserEmail!!, rating = rating, comment = comment))
                    Toast.makeText(this@ProfileActivity, "Thank you for your feedback!", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel", null)
            builder.show()
        }

        btnChangePicture.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // PSEUDO: Clear session and return to Login screen
        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("TextbookConnectPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity()
        }

        // PSEUDO: Collect UI data and update user profile in DB
        btnUpdate.setOnClickListener {
            lifecycleScope.launch {
                val existingUser = currentUserEmail?.let { db.userDao().getUserByEmail(it) }
                if (existingUser != null) {
                    val updatedUser = existingUser.copy(
                        firstName = firstName.text.toString(),
                        lastName = lastName.text.toString(),
                        physicalAddress = physicalAddress.text.toString(),
                        postalAddress = postalAddress.text.toString(),
                        university = university.text.toString(),
                        campus = campus.text.toString(),
                        faculty = faculty.text.toString()
                    )
                    db.userDao().updateUser(updatedUser)
                    Toast.makeText(this@ProfileActivity, "Profile Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // PSEUDO: Validate and update user password
        btnUpdatePassword.setOnClickListener {
            val pass = newPassword.text.toString()
            val verify = verifyPassword.text.toString()

            if (pass.isEmpty() || verify.isEmpty()) {
                Toast.makeText(this, "Please fill both password fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pass != verify) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val existingUser = currentUserEmail?.let { db.userDao().getUserByEmail(it) }
                if (existingUser != null) {
                    val updatedUser = existingUser.copy(password = pass)
                    db.userDao().updateUser(updatedUser)
                    newPassword.text.clear()
                    verifyPassword.text.clear()
                    Toast.makeText(this@ProfileActivity, "Password Updated", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // PSEUDO: Permanent account deletion
        btnDelete.setOnClickListener {
            currentUserEmail?.let {
                lifecycleScope.launch {
                    db.userDao().deleteUser(it)
                    Toast.makeText(this@ProfileActivity, "Account Deleted", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@ProfileActivity, MainActivity::class.java))
                    finish()
                }
            }
        }

        // PSEUDO: Navigation interactions
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_profile
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_sell -> {
                    val intent = Intent(this, SellActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_profile -> true
                else -> false
            }
        }
    }
}
