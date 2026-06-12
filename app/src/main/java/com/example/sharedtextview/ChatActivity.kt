/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize the Chat Screen and retrieve the Chat ID and User Email.
 * 2. Admin Check:
 *    - If the user is an admin, hide the message input (read-only mode).
 * 3. Load Chat Details:
 *    - Fetch the chat metadata from the database.
 *    - Identify the chat partner (Buyer or Seller).
 *    - Fetch book details or mark as "System Support" if it's an admin chat.
 * 4. Load & Display Messages:
 *    - Retrieve all messages for this Chat ID from the database.
 *    - For each message, determine if it was sent by the current user.
 *    - Inflate the appropriate message bubble (Sent or Received).
 *    - Auto-scroll to the bottom of the conversation.
 * 5. Send Message:
 *    - Collect text from input.
 *    - Save message to database with a timestamp.
 *    - Update the Chat metadata with the "Last Message" for the summary list.
 *    - Refresh the message display.
 */

package com.example.sharedtextview

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.Message
import kotlinx.coroutines.launch

/**
 * ChatActivity handles real-time messaging between users or between a user and admin.
 */
class ChatActivity : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var scrollView: ScrollView
    private var chatId: Int = -1
    private var currentUserEmail: String? = null
    private var isAdmin: Boolean = false

    /**
     * Sets up the chat UI and loads existing messages.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        currentUserEmail = intent.getStringExtra("USER_EMAIL")
        chatId = intent.getIntExtra("CHAT_ID", -1)
        isAdmin = intent.getBooleanExtra("IS_ADMIN", false)

        // PSEUDO: Validate session data
        if (currentUserEmail == null || chatId == -1) {
            Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        messagesContainer = findViewById(R.id.messagesContainer)
        etMessage = findViewById(R.id.etMessage)
        scrollView = findViewById(R.id.chatScrollView)
        val btnSend = findViewById<ImageButton>(R.id.btnSendMessage)
        val tvPartnerName = findViewById<TextView>(R.id.chatPartnerName)
        val tvBookTitle = findViewById<TextView>(R.id.chatBookTitle)

        val db = AppDatabase.getDatabase(this)

        // PSEUDO: If viewing as admin, prevent sending messages
        if (isAdmin) {
            findViewById<LinearLayout>(R.id.messageInputLayout).visibility = android.view.View.GONE
        }

        lifecycleScope.launch {
            // PSEUDO: Load chat info from database
            val chat = db.chatDao().getChatById(chatId)
            if (chat != null) {
                // PSEUDO: Determine partner name based on who is viewing
                if (isAdmin) {
                    tvPartnerName.text = "Chat: ${chat.buyerName} vs ${chat.sellerName}"
                } else {
                    val partnerEmail = if (chat.buyerEmail == currentUserEmail) chat.sellerEmail else chat.buyerEmail
                    val partner = db.userDao().getUserByEmail(partnerEmail)
                    tvPartnerName.text = "Chat with ${partner?.firstName ?: partnerEmail}"
                }
                
                // PSEUDO: Handle system support chats (no specific book)
                if (chat.bookId == -1) {
                    tvBookTitle.text = "System Support"
                } else {
                    val book = db.bookDao().getBookById(chat.bookId)
                    tvBookTitle.text = "About: ${book?.title ?: "Unknown Book"}"
                }

                loadMessages(db)
            }
        }

        // PSEUDO: Send button handler
        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(db, content)
            }
        }
    }

    /**
     * Fetches messages from database and triggers UI update.
     */
    private fun loadMessages(db: AppDatabase) {
        lifecycleScope.launch {
            val messages = db.chatDao().getMessagesForChat(chatId)
            displayMessages(messages)
        }
    }

    /**
     * Renders message bubbles in the container.
     */
    private fun displayMessages(messages: List<Message>) {
        messagesContainer.removeAllViews()
        for (message in messages) {
            // PSEUDO: Determine if the message should appear on the left or right
            val isSentByMe = !isAdmin && message.senderEmail == currentUserEmail
            val layoutId = if (isSentByMe) R.layout.item_message_sent else R.layout.item_message_received
            val view = layoutInflater.inflate(layoutId, messagesContainer, false)
            
            view.findViewById<TextView>(R.id.tvMessageContent).text = message.content
            view.findViewById<TextView>(R.id.tvSenderName).text = if (isAdmin) "${message.senderName}:" else message.senderName
            messagesContainer.addView(view)
        }
        // PSEUDO: Scroll to the latest message
        scrollView.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    /**
     * Saves a new message to the database and refreshes the view.
     */
    private fun sendMessage(db: AppDatabase, content: String) {
        lifecycleScope.launch {
            val chat = db.chatDao().getChatById(chatId)
            val myName = if (chat?.buyerEmail == currentUserEmail) chat?.buyerName else chat?.sellerName
            
            // PSEUDO: Construct message object
            val message = Message(
                chatId = chatId,
                senderEmail = currentUserEmail!!,
                senderName = myName ?: "",
                content = content
            )
            db.chatDao().insertMessage(message)
            
            // PSEUDO: Update chat's last message for the inbox preview
            chat?.let {
                db.chatDao().updateChat(it.copy(lastMessage = content, timestamp = System.currentTimeMillis()))
            }
            
            etMessage.text.clear()
            loadMessages(db)
        }
    }
}
