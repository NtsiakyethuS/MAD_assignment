/* 
 * PSEUDO-CODE LOGIC:
 * 1. Setup the Home Screen and retrieve the current user's email.
 * 2. Ensure initial data exists in the database.
 * 3. Load all books from the database and display them in a list.
 * 4. Setup Search:
 *    - As the user types, query the database for matching titles/authors.
 *    - Update the display list with search results.
 * 5. Display Books:
 *    - For each book, create a visual item (Title, Author, Price, Cover).
 *    - Handle clicks on book items to show detailed information.
 * 6. Bottom Navigation: Handle switching between Home, Search, Sell, and Profile screens.
 */

package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.Book
import com.example.sharedtextview.R.id.home_root
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * HomeActivity displays the main dashboard with a list of available books.
 */
class HomeActivity : AppCompatActivity() {
    
    private var currentUserEmail: String? = null

    /**
     * Initializes the activity, sets up the search bar and navigation.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        currentUserEmail = intent.getStringExtra("USER_EMAIL")

        // PSEUDO: Adjust layout padding for status bars
        setOnApplyWindowInsetsListener(findViewById(home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchIcon = findViewById<ImageView>(R.id.searchIcon)

        val db = AppDatabase.getDatabase(this)

        // PSEUDO: Load initial book data in the background
        lifecycleScope.launch {
            com.example.sharedtextview.database.DataPopulator.populateBooks(this@HomeActivity)
            loadBooks(db)
        }

        // PSEUDO: Real-time search update as user types
        searchEditText.addTextChangedListener { text ->
            performSearch(db, text.toString())
        }

        // PSEUDO: Search button click handler
        searchIcon.setOnClickListener {
            performSearch(db, searchEditText.text.toString())
        }

        // PSEUDO: Setup Bottom Navigation Bar interactions
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_home
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_search -> {
                    val intent = Intent(this, SearchActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    true
                }
                R.id.navigation_sell -> {
                    val intent = Intent(this, SellActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    true
                }
                R.id.navigation_profile -> {
                    val intent = Intent(this, ProfileActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    /**
     * Retrieves all books from the database and updates the UI.
     */
    private fun loadBooks(db: AppDatabase) {
        lifecycleScope.launch {
            val books = db.bookDao().getAllBooks()
            displayBooks(books)
        }
    }

    /**
     * Queries the database for books matching the search string.
     */
    private fun performSearch(db: AppDatabase, query: String) {
        lifecycleScope.launch {
            val books = if (query.isEmpty()) {
                db.bookDao().getAllBooks()
            } else {
                db.bookDao().searchBooks("%$query%")
            }
            displayBooks(books)
        }
    }

    /**
     * Dynamically populates the linear layout with book items.
     */
    private fun displayBooks(books: List<Book>) {
        val booksContainer = findViewById<LinearLayout>(R.id.booksContainer)
        booksContainer.removeAllViews()

        for (book in books) {
            // PSEUDO: Inflate the book item layout and populate it with data
            val bookView = layoutInflater.inflate(R.layout.item_book_home, booksContainer, false)
            
            val ivCover = bookView.findViewById<ImageView>(R.id.ivBookCover)
            val tvTitle = bookView.findViewById<TextView>(R.id.tvBookTitle)
            val tvAuthor = bookView.findViewById<TextView>(R.id.tvBookAuthor)
            val tvEdition = bookView.findViewById<TextView>(R.id.tvBookEdition)
            val tvPrice = bookView.findViewById<TextView>(R.id.tvBookPrice)

            tvTitle.text = book.title
            tvAuthor.text = book.author
            tvEdition.text = book.edition
            tvPrice.text = book.price

            // PSEUDO: Set the book image, fallback to placeholder if null
            if (book.imageUri != null) {
                try {
                    ivCover.setImageURI(android.net.Uri.parse(book.imageUri))
                } catch (e: Exception) {
                    ivCover.setImageResource(R.drawable.picture1)
                }
            } else {
                ivCover.setImageResource(R.drawable.picture1)
            }

            // PSEUDO: Clicking a book opens its details page
            bookView.setOnClickListener {
                val intent = Intent(this, BookDetailsActivity::class.java)
                intent.putExtra("BOOK_ID", book.id)
                intent.putExtra("USER_EMAIL", currentUserEmail)
                startActivity(intent)
            }

            booksContainer.addView(bookView)
        }

        if (books.isNotEmpty()) {
            Toast.makeText(this, "${books.size} books found", Toast.LENGTH_SHORT).show()
        }
    }
}
