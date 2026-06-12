/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize the Advanced Search Screen.
 * 2. Setup Search Inputs:
 *    - Text field for keywords.
 *    - Spinner/Dropdown to filter by Title, Author, Edition, or Faculty.
 * 3. Perform Search:
 *    - If the user types "@admin", trigger a special admin contact view.
 *    - Otherwise, query the database based on the selected filter and text.
 *    - Use wildcards (%) for partial matching in the SQL queries.
 * 4. Display Results:
 *    - Clear any previous results.
 *    - If no books found, show a "No results" message.
 *    - For each found book, inflate a list item with its details.
 * 5. Handle Clicks:
 *    - Clicking a book opens its full details.
 *    - Clicking the "@admin" result initiates a support chat.
 */

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
import com.example.sharedtextview.database.Chat
import com.example.sharedtextview.database.User
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

/**
 * SearchActivity provides advanced filtering and search capabilities.
 */
class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var searchIcon: ImageView
    private lateinit var filterSpinner: Spinner
    private lateinit var resultsContainer: LinearLayout
    private var currentUserEmail: String? = null

    /**
     * Sets up UI components and search triggers.
     */
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

        // PSEUDO: Search automatically as text changes
        searchEditText.addTextChangedListener { text ->
            performSearch(db, text.toString())
        }

        // PSEUDO: Search when clicking the magnifying glass icon
        searchIcon.setOnClickListener {
            performSearch(db, searchEditText.text.toString())
        }

        // PSEUDO: Refresh search when the filter category changes
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                performSearch(db, searchEditText.text.toString())
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        setupBottomNavigation()
    }

    /**
     * Core search logic that handles different filters and the @admin shortcut.
     */
    private fun performSearch(db: AppDatabase, query: String) {
        val filter = filterSpinner.selectedItem.toString()
        lifecycleScope.launch {
            // PSEUDO: Check for special admin shortcut
            if (query.trim().lowercase() == "@admin") {
                displayAdminResult(db)
                return@launch
            }

            // PSEUDO: Execute specific database query based on filter
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

    /**
     * Populates the results container with book item views.
     */
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
            // PSEUDO: Create a view for each book and set details
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

    /**
     * Displays a special clickable item to contact support.
     */
    private fun displayAdminResult(db: AppDatabase) {
        resultsContainer.removeAllViews()
        val adminView = layoutInflater.inflate(R.layout.item_book_search, resultsContainer, false)
        adminView.findViewById<TextView>(R.id.bookTitle).text = "Contact Administrator"
        adminView.findViewById<TextView>(R.id.bookAuthor).text = "Support and Assistance"
        adminView.findViewById<TextView>(R.id.bookEdition).text = "Type messages here"
        adminView.findViewById<TextView>(R.id.bookPrice).text = "FREE"

        adminView.setOnClickListener {
            lifecycleScope.launch {
                // PSEUDO: Find an admin and start/open a chat session
                val admins = db.userDao().getAdmins()
                if (admins.isNotEmpty()) {
                    val admin = admins[0]
                    val currentUser = db.userDao().getUserByEmail(currentUserEmail ?: "")
                    
                    var chat = db.chatDao().getChatBetweenUsers(currentUserEmail!!, admin.email, -1)
                    if (chat == null) {
                        chat = Chat(
                            bookId = -1,
                            buyerEmail = currentUserEmail!!,
                            sellerEmail = admin.email,
                            buyerName = "${currentUser?.firstName} ${currentUser?.lastName}",
                            sellerName = "Admin Support"
                        )
                        val chatId = db.chatDao().insertChat(chat)
                        chat = chat.copy(id = chatId.toInt())
                    }
                    
                    val intent = Intent(this@SearchActivity, ChatActivity::class.java)
                    intent.putExtra("CHAT_ID", chat.id)
                    intent.putExtra("USER_EMAIL", currentUserEmail)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@SearchActivity, "No administrator available", Toast.LENGTH_SHORT).show()
                }
            }
        }
        resultsContainer.addView(adminView)
    }

    /**
     * Standard bottom navigation setup.
     */
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
