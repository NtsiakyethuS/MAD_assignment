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

class HomeActivity : AppCompatActivity() {
    
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        
        currentUserEmail = intent.getStringExtra("USER_EMAIL")

        // Apply system bar insets to the root layout
        setOnApplyWindowInsetsListener(findViewById(home_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val searchEditText = findViewById<EditText>(R.id.searchEditText)
        val searchIcon = findViewById<ImageView>(R.id.searchIcon)

        val db = AppDatabase.getDatabase(this)

        // Prepopulate data
        lifecycleScope.launch {
            com.example.sharedtextview.database.DataPopulator.populateBooks(this@HomeActivity)
            // Load books from database
            loadBooks(db)
        }

        // Search when text changes
        searchEditText.addTextChangedListener { text ->
            performSearch(db, text.toString())
        }

        // Search when icon is clicked
        searchIcon.setOnClickListener {
            performSearch(db, searchEditText.text.toString())
        }

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

    private fun loadBooks(db: AppDatabase) {
        lifecycleScope.launch {
            val books = db.bookDao().getAllBooks()
            displayBooks(books)
        }
    }

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

    private fun displayBooks(books: List<Book>) {
        val booksContainer = findViewById<LinearLayout>(R.id.booksContainer)
        booksContainer.removeAllViews()

        for (book in books) {
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

            if (book.imageUri != null) {
                try {
                    ivCover.setImageURI(android.net.Uri.parse(book.imageUri))
                } catch (e: Exception) {
                    ivCover.setImageResource(R.drawable.picture1)
                }
            } else {
                ivCover.setImageResource(R.drawable.picture1)
            }

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
