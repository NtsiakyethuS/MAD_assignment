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

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        
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
        
        cbSameAddress.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etPostalAddress.setText(etPhysicalAddress.text.toString())
                etPostalAddress.isEnabled = false
            } else {
                etPostalAddress.isEnabled = true
            }
        }

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

            if (email.isEmpty() || firstName.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

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

            lifecycleScope.launch {
                db.userDao().insertUser(user)
                Toast.makeText(this@RegisterActivity, "Registration Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                finish()
            }
        }
    }
}
