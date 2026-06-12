/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize the Sell/Post Book Screen and retrieve the current user's email.
 * 2. Image Selection:
 *    - Open the system gallery when "Upload Picture" is clicked.
 *    - Store the URI of the selected image and display it in the preview.
 * 3. Book Posting:
 *    - Collect details: Title, Author, Edition, and Price.
 *    - Validate that all fields are filled.
 *    - Create a new Book entry.
 *    - Automatically assign the book to the user's faculty (fetched from their profile).
 *    - Save the book listing to the database.
 * 4. Completion: Show a success message and close the screen.
 * 5. Navigation: Allow switching back to Home, Search, or Profile.
 */

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

/**
 * SellActivity allows users to list their textbooks for sale in the marketplace.
 */
class SellActivity : AppCompatActivity() {

    private lateinit var bookImage: ImageView
    private lateinit var bookTitle: EditText
    private lateinit var bookAuthor: EditText
    private lateinit var bookEdition: EditText
    private lateinit var bookPrice: EditText
    private var selectedImageUri: String? = null
    private var currentUserEmail: String? = null

    // PSEUDO: Launcher for image selection from the phone's gallery
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            bookImage.setImageURI(it)
            selectedImageUri = it.toString()
        }
    }

    /**
     * Sets up the UI for inputting book data and handles image selection.
     */
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

        // PSEUDO: Trigger gallery intent
        btnUploadPicture.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        // PSEUDO: Collect inputs and save the new book listing
        btnPost.setOnClickListener {
            val title = bookTitle.text.toString().trim()
            val author = bookAuthor.text.toString().trim()
            val edition = bookEdition.text.toString().trim()
            val price = bookPrice.text.toString().trim()

            // PSEUDO: Ensure no empty fields
            if (title.isEmpty() || author.isEmpty() || edition.isEmpty() || price.isEmpty() || currentUserEmail == null) {
                Toast.makeText(this, "Please fill in all book details", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    // PSEUDO: Fetch user's faculty to categorize the book
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
                    // PSEUDO: Insert into database and finish
                    db.bookDao().insertBook(newBook)
                    Toast.makeText(this@SellActivity, "Book Posted Successfully!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // PSEUDO: Standard navigation logic
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
