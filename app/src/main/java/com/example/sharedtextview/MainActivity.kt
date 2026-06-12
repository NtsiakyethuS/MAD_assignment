/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize the Login Activity and check if "Remember Me" is active.
 * 2. If "Remember Me" is true and a saved email exists, skip login and go to Home.
 * 3. Pre-populate the database with initial book data and admin users if empty.
 * 4. On Login Click:
 *    a. Get email and password from input fields.
 *    b. Validate inputs are not empty.
 *    c. Search database for the user by email.
 *    d. If user exists and password matches:
 *       - Save login status if "Remember Me" is checked.
 *       - Navigate to Home Screen.
 *    e. If password fails or user is missing, show an error message.
 * 5. On Register Click: Open Registration Screen.
 * 6. On Forgot Password Click: Open Password Recovery Screen.
 */

package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

/**
 * MainActivity handles the login process and application entry point.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PREFS_NAME = "TextbookConnectPrefs"
    private val KEY_REMEMBER_ME = "remember_me"
    private val KEY_EMAIL = "saved_email"

    /**
     * Called when the activity is first created.
     * Sets up UI, auto-login check, and button listeners.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // PSEUDO: Read shared preferences for auto-login
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isRemembered = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val savedEmail = prefs.getString(KEY_EMAIL, null)

        // PSEUDO: If auto-login is valid, move to Home Activity immediately
        if (isRemembered && savedEmail != null) {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                putExtra("USER_EMAIL", savedEmail)
            })
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = AppDatabase.getDatabase(this)

        // PSEUDO: Launch background task to ensure database has initial data
        lifecycleScope.launch {
            com.example.sharedtextview.database.DataPopulator.populateBooks(this@MainActivity)
        }

        // PSEUDO: Handle Login Button Interaction
        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    // PSEUDO: Query database for the user
                    val user = db.userDao().getUserByEmail(email)
                    if (user != null) {
                        // PSEUDO: Check if password is correct
                        if (user.password == password) {
                            // PSEUDO: Handle "Remember Me" logic
                            if (binding.cbRememberMe.isChecked) {
                                prefs.edit().apply {
                                    putBoolean(KEY_REMEMBER_ME, true)
                                    putString(KEY_EMAIL, email)
                                    apply()
                                }
                            } else {
                                prefs.edit().clear().apply()
                            }

                            // PSEUDO: Transition to Home Activity
                            startActivity(Intent(this@MainActivity, HomeActivity::class.java).apply {
                                putExtra("USER_EMAIL", email)
                            })
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "you hav inputed the wrong password", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@MainActivity, "User not found. Please register.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // PSEUDO: Navigate to Registration
        binding.button2.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // PSEUDO: Navigate to Forgot Password
        binding.textView4.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
