package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

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

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            profileImage.setImageURI(it)
            // In a real app, save the URI string to the database
        }
    }

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

        val db = AppDatabase.getDatabase(this)
        
        // Load user data
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
                    // profileImage.setImageURI(...) if URI was saved
                }
            }
        }

        btnChangePicture.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        val btnUpdate = findViewById<Button>(R.id.btnUpdateProfile)
        val btnUpdatePassword = findViewById<Button>(R.id.btnUpdatePassword)
        val btnDelete = findViewById<Button>(R.id.btnDeleteAccount)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("TextbookConnectPrefs", MODE_PRIVATE)
            prefs.edit().clear().apply()
            startActivity(Intent(this, MainActivity::class.java))
            finishAffinity() // Close all activities in the stack
        }

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

        // Setup Navigation Bar
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
