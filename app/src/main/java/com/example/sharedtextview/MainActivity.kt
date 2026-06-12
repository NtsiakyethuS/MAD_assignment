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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PREFS_NAME = "TextbookConnectPrefs"
    private val KEY_REMEMBER_ME = "remember_me"
    private val KEY_EMAIL = "saved_email"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isRemembered = prefs.getBoolean(KEY_REMEMBER_ME, false)
        val savedEmail = prefs.getString(KEY_EMAIL, null)

        if (isRemembered && savedEmail != null) {
            // Auto-login
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

        // Prepopulate admin and data
        lifecycleScope.launch {
            com.example.sharedtextview.database.DataPopulator.populateBooks(this@MainActivity)
        }

        binding.button.setOnClickListener {
            val email = binding.editTextTextEmailAddress.text.toString().trim()
            val password = binding.editTextTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val user = db.userDao().getUserByEmail(email)
                    if (user != null) {
                        if (user.password == password) {
                            if (binding.cbRememberMe.isChecked) {
                                prefs.edit().apply {
                                    putBoolean(KEY_REMEMBER_ME, true)
                                    putString(KEY_EMAIL, email)
                                    apply()
                                }
                            } else {
                                prefs.edit().clear().apply()
                            }

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

        binding.button2.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.textView4.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }
}
