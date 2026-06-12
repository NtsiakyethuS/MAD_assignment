/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Book Details Screen and retrieve the Book ID from the intent.
 * 2. Fetch Data:
 *    - Query database for book details (Title, Price, Image, etc.).
 *    - Query database for the seller's profile information.
 * 3. Populate UI:
 *    - Set the book information in the view fields.
 *    - Display the seller's name, university, and contact email.
 * 4. Contact Seller (Email):
 *    - When "Contact" is clicked, open an external email app.
 *    - Pre-fill the seller's email and a subject line about the book.
 * 5. In-App Chat:
 *    - When "Chat" is clicked, check if a conversation already exists between the buyer and seller for this book.
 *    - If not, create a new Chat entry in the database.
 *    - Navigate to the Chat Screen with the Chat ID.
 */

package com.example.sharedtextview

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import kotlinx.coroutines.launch

/**
 * BookDetailsActivity displays complete information about a specific textbook and its seller.
 */
class BookDetailsActivity : AppCompatActivity() {

    /**
     * Initializes UI and loads data based on the provided Book ID.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_book_details)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.book_details_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val bookId = intent.getIntExtra("BOOK_ID", -1)
        if (bookId == -1) {
            Toast.makeText(this, "Error loading book details", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = AppDatabase.getDatabase(this)

        val ivBookImage = findViewById<ImageView>(R.id.detailsBookImage)
        val tvTitle = findViewById<TextView>(R.id.detailsBookTitle)
        val tvAuthor = findViewById<TextView>(R.id.detailsBookAuthor)
        val tvEdition = findViewById<TextView>(R.id.detailsBookEdition)
        val tvPrice = findViewById<TextView>(R.id.detailsBookPrice)

        val tvSellerName = findViewById<TextView>(R.id.sellerName)
        val tvSellerUniv = findViewById<TextView>(R.id.sellerUniversity)
        val tvSellerCampus = findViewById<TextView>(R.id.sellerCampus)
        val tvSellerEmail = findViewById<TextView>(R.id.sellerEmail)
        val btnContact = findViewById<Button>(R.id.btnContactSeller)
        val btnChat = findViewById<Button>(R.id.btnChatWithSeller)

        val currentUserEmail = intent.getStringExtra("USER_EMAIL")

        // PSEUDO: Asynchronously load book and seller details from database
        lifecycleScope.launch {
            val book = db.bookDao().getBookById(bookId)
            if (book != null) {
                tvTitle.text = book.title
                tvAuthor.text = "Author: ${book.author}"
                tvEdition.text = "Edition: ${book.edition}"
                tvPrice.text = book.price

                if (book.imageUri != null) {
                    try {
                        ivBookImage.setImageURI(Uri.parse(book.imageUri))
                    } catch (e: Exception) {
                        ivBookImage.setImageResource(R.drawable.picture1)
                    }
                }

                // PSEUDO: Fetch the profile of the user who listed the book
                val seller = db.userDao().getUserByEmail(book.sellerEmail)
                if (seller != null) {
                    tvSellerName.text = "Seller: ${seller.firstName} ${seller.lastName}"
                    tvSellerUniv.text = "University: ${seller.university}"
                    tvSellerCampus.text = "Campus: ${seller.campus}"
                    tvSellerEmail.text = "Email: ${seller.email}"

                    // PSEUDO: Handle external email contact intent
                    btnContact.setOnClickListener {
                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_EMAIL, arrayOf(seller.email))
                            putExtra(Intent.EXTRA_SUBJECT, "Inquiry about your book: ${book.title}")
                            putExtra(Intent.EXTRA_TEXT, "Hi ${seller.firstName}, I am interested in buying your book '${book.title}'.")
                        }
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send email..."))
                        } catch (ex: android.content.ActivityNotFoundException) {
                            Toast.makeText(this@BookDetailsActivity, "No email clients installed.", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // PSEUDO: Handle in-app chat initiation
                    btnChat.setOnClickListener {
                        if (currentUserEmail == null) {
                            Toast.makeText(this@BookDetailsActivity, "Please log in to chat", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        if (currentUserEmail == seller.email) {
                            Toast.makeText(this@BookDetailsActivity, "You cannot chat with yourself", Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        lifecycleScope.launch {
                            // PSEUDO: Check if a chat session already exists to avoid duplicates
                            var chat = db.chatDao().getChatBetweenUsers(currentUserEmail, seller.email, book.id)
                            if (chat == null) {
                                val buyer = db.userDao().getUserByEmail(currentUserEmail)
                                val newChat = com.example.sharedtextview.database.Chat(
                                    bookId = book.id,
                                    buyerEmail = currentUserEmail,
                                    sellerEmail = seller.email,
                                    buyerName = "${buyer?.firstName} ${buyer?.lastName}".trim(),
                                    sellerName = "${seller.firstName} ${seller.lastName}".trim()
                                )
                                val id = db.chatDao().insertChat(newChat)
                                chat = db.chatDao().getChatById(id.toInt())
                            }
                            
                            // PSEUDO: Transition to the chat interface
                            chat?.let {
                                val intent = Intent(this@BookDetailsActivity, ChatActivity::class.java)
                                intent.putExtra("CHAT_ID", it.id)
                                intent.putExtra("USER_EMAIL", currentUserEmail)
                                startActivity(intent)
                            }
                        }
                    }
                } else {
                    tvSellerName.text = "Seller details not available"
                }
            } else {
                Toast.makeText(this@BookDetailsActivity, "Book not found", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
