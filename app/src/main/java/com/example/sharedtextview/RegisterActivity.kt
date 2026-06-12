/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Registration Screen and input fields.
 * 2. Address Sync Feature:
 *    - If "Same as Physical Address" is checked, copy text from physical to postal.
 *    - Disable postal address input while sync is active.
 *    - Automatically update postal address as physical address text changes.
 * 3. On Register Click:
 *    - Collect data from all input fields (Names, Email, Addresses, School Info, Password).
 *    - Validate that critical fields (Email, Name, Password) are not empty.
 *    - Create a new User object with the provided data.
 *    - Save the user to the Room database.
 *    - Show success message and navigate back to Login Screen.
 */

package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.User
import kotlinx.coroutines.launch

/**
 * RegisterActivity handles the creation of new user accounts.
 */
class RegisterActivity : AppCompatActivity() {

    /**
     * Initializes the UI and sets up registration logic.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
        // PSEUDO: Adjust padding for system UI bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etFirstName = findViewById<EditText>(R.id.etFirstName)
        val etLastName = findViewById<EditText>(R.id.etLastName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhysicalAddress = findViewById<EditText>(R.id.etPhysicalAddress)
        val cbSameAddress = findViewById<android.widget.CheckBox>(R.id.cbSameAddress)
        val etPostalAddress = findViewById<EditText>(R.id.etPostalAddress)
        
        // PSEUDO: Handle the address synchronization checkbox
        cbSameAddress.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etPostalAddress.setText(etPhysicalAddress.text.toString())
                etPostalAddress.isEnabled = false
            } else {
                etPostalAddress.isEnabled = true
            }
        }

        // PSEUDO: Keep postal address in sync with physical address in real-time if checked
        etPhysicalAddress.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (cbSameAddress.isChecked) {
                    etPostalAddress.setText(s.toString())
                }
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        
        val etUniversity = findViewById<EditText>(R.id.etUniversity)
        val etCampus = findViewById<EditText>(R.id.etCampus)
        val etFaculty = findViewById<EditText>(R.id.etFaculty)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val db = AppDatabase.getDatabase(this)

        // PSEUDO: Handle Register button click
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString()
            val firstName = etFirstName.text.toString()
            val lastName = etLastName.text.toString()
            val physicalAddress = etPhysicalAddress.text.toString()
            val postalAddress = etPostalAddress.text.toString()
            val university = etUniversity.text.toString()
            val campus = etCampus.text.toString()
            val faculty = etFaculty.text.toString()
            val password = etPassword.text.toString()

            // PSEUDO: Validate mandatory inputs
            if (email.isEmpty() || firstName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // PSEUDO: Create user data object
            val user = User(
                email = email,
                firstName = firstName,
                lastName = lastName,
                physicalAddress = physicalAddress,
                postalAddress = postalAddress,
                university = university,
                campus = campus,
                faculty = faculty,
                password = password
            )

            // PSEUDO: Save user to database asynchronously
            lifecycleScope.launch {
                db.userDao().insertUser(user)
                Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                // PSEUDO: Go back to login screen
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
