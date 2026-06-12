/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Password Recovery Screen.
 * 2. Collect the user's email address from the input field.
 * 3. Search Database:
 *    - Check if the provided email belongs to a registered user.
 * 4. Generate Temporary Password:
 *    - If the user exists, generate a random 8-character string (temp password).
 *    - Update the user's account in the database with this new temporary password.
 * 5. Notify User:
 *    - Show a dialog box displaying the temporary password.
 *    - Instruct the user to log in and change their password in the profile section.
 */

package com.example.sharedtextview

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ForgotPasswordActivity handles the password reset flow for users who lost access.
 */
class ForgotPasswordActivity : AppCompatActivity() {

    /**
     * Sets up UI and handles the reset request logic.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.forgot_password_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etForgotEmail)
        val btnReset = findViewById<Button>(R.id.btnResetPassword)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        val db = AppDatabase.getDatabase(this)

        // PSEUDO: Handle the reset password button click
        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                // PSEUDO: Verify user email exists in the system
                val user = db.userDao().getUserByEmail(email)
                if (user != null) {
                    // PSEUDO: Generate a temporary random password
                    val tempPassword = UUID.randomUUID().toString().substring(0, 8)
                    
                    // PSEUDO: Overwrite the old password with the temporary one in the database
                    val updatedUser = user.copy(password = tempPassword)
                    db.userDao().updateUser(updatedUser)

                    // PSEUDO: Show the temporary password to the user via a dialog
                    android.app.AlertDialog.Builder(this@ForgotPasswordActivity)
                        .setTitle("Password Reset Successful")
                        .setMessage("A temporary password has been generated for you:\n\n$tempPassword\n\nPlease use this to log in and change your password in your profile.")
                        .setPositiveButton("OK") { _, _ -> finish() }
                        .show()
                } else {
                    Toast.makeText(this@ForgotPasswordActivity, "Email not found in our database", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // PSEUDO: Allow user to go back to login screen
        tvBack.setOnClickListener {
            finish()
        }
    }
}
