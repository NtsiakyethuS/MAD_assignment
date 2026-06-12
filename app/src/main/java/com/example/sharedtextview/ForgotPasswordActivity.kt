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

class ForgotPasswordActivity : AppCompatActivity() {
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

        btnReset.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val user = db.userDao().getUserByEmail(email)
                if (user != null) {
                    // Generate a temporary password (first 8 characters of a UUID)
                    val tempPassword = UUID.randomUUID().toString().substring(0, 8)
                    
                    // Update user in database
                    val updatedUser = user.copy(password = tempPassword)
                    db.userDao().updateUser(updatedUser)

                    // In a real app, you'd send an email here. 
                    // For this assignment, we show it in a Toast and a Dialog.
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

        tvBack.setOnClickListener {
            finish()
        }
    }
}
