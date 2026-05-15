package com.example.sharedtextview

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.Book
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var filterSpinner: Spinner
    private lateinit var resultsContainer: LinearLayout
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_search)

        currentUserEmail = intent.getStringExtra("USER_EMAIL")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.search_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        searchEditText = findViewById(R.id.searchEditText)
        searchIcon = findViewById(R.id.searchIcon)
        filterSpinner = findViewById(R.id.filterSpinner)
        resultsContainer = findViewById(R.id.searchResultsContainer)

        val db = AppDatabase.getDatabase(this)

        searchEditText.addTextChangedListener { text ->
            performSearch(db, text.toString())
        }

        searchIcon.setOnClickListener {
            performSearch(db, searchEditText.text.toString())
        }

        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                performSearch(db, searchEditText.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupBottomNavigation()
    }

    private fun performSearch(db: AppDatabase, query: String) {
        val filter = filterSpinner.selectedItem.toString()
        lifecycleScope.launch {
            val books = when (filter) {
                "Title" -> db.bookDao().searchBooksByTitle("%$query%")
                "Author" -> db.bookDao().searchBooksByAuthor("%$query%")
                "Edition" -> db.bookDao().searchBooksByEdition("%$query%")
                "Faculty" -> db.bookDao().searchBooksByFaculty("%$query%")
                else -> db.bookDao().searchBooks("%$query%")
            }
            displayResults(books)
        }
    }

    private fun displayResults(books: List<Book>) {
        resultsContainer.removeAllViews()
        if (books.isEmpty()) {
            val noResults = TextView(this).apply {
                text = "No books found matching your search."
                textSize = 18f
                setPadding(0, 50, 0, 0)
                gravity = android.view.Gravity.CENTER
            }
            resultsContainer.addView(noResults)
            return
        }

        for (book in books) {
            val bookView = layoutInflater.inflate(R.layout.item_book_search, resultsContainer, false)
            bookView.findViewById<TextView>(R.id.bookTitle).text = book.title
            bookView.findViewById<TextView>(R.id.bookAuthor).text = "Author: ${book.author}"
            bookView.findViewById<TextView>(R.id.bookEdition).text = "Edition: ${book.edition}"
            bookView.findViewById<TextView>(R.id.bookPrice).text = "Price: ${book.price}"
            
            bookView.setOnClickListener {
                val intent = Intent(this, BookDetailsActivity::class.java)
                intent.putExtra("BOOK_ID", book.id)
                intent.putExtra("USER_EMAIL", currentUserEmail)
                startActivity(intent)
            }
            resultsContainer.addView(bookView)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.navigation_search
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.navigation_search -> true
                R.id.navigation_sell -> {
                    val intent = Intent(this, SellActivity::class.java)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                    finish()
                    true
                }
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
    
    // Helper extension for Float to SP
    private val Int.sp: Float get() = this * resources.displayMetrics.scaledDensity
}
