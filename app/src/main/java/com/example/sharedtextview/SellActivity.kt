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
import com.example.sharedtextview.database.Book
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class SellActivity : AppCompatActivity() {

    private lateinit var bookImage: ImageView
    private lateinit var bookTitle: EditText
    private lateinit var bookAuthor: EditText
    private lateinit var bookEdition: EditText
    private lateinit var bookPrice: EditText
    private var selectedImageUri: String? = null
    private var currentUserEmail: String? = null

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            bookImage.setImageURI(it)
            selectedImageUri = it.toString()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sell)
        
        currentUserEmail = intent.getStringExtra("USER_EMAIL")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.sell_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize views
        bookImage = findViewById(R.id.bookImage)
        val btnUploadPicture = findViewById<Button>(R.id.btnUploadBookPicture)
        bookTitle = findViewById(R.id.sellBookTitle)
        bookAuthor = findViewById(R.id.sellBookAuthor)
        bookEdition = findViewById(R.id.sellBookEdition)
        bookPrice = findViewById(R.id.sellBookPrice)
        val btnPost = findViewById<Button>(R.id.btnPostBook)

        val db = AppDatabase.getDatabase(this)

        btnUploadPicture.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnPost.setOnClickListener {
            val title = bookTitle.text.toString().trim()
            val author = bookAuthor.text.toString().trim()
            val edition = bookEdition.text.toString().trim()
            val price = bookPrice.text.toString().trim()

            if (title.isEmpty() || author.isEmpty() || edition.isEmpty() || price.isEmpty() || currentUserEmail == null) {
                Toast.makeText(this, "Please fill in all book details", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val user = db.userDao().getUserByEmail(currentUserEmail!!)
                    val newBook = Book(
                        title = title,
                        author = author,
                        edition = edition,
                        price = price,
                        imageUri = selectedImageUri,
                        sellerEmail = currentUserEmail!!,
                        faculty = user?.faculty ?: "General"
                    )
                    db.bookDao().insertBook(newBook)
                    Toast.makeText(this@SellActivity, "Book Posted Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // Setup Navigation Bar
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_sell
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
                R.id.navigation_sell -> true
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }
}
