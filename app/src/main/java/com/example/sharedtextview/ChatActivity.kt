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

class ChatActivity : AppCompatActivity() {

    private lateinit var messagesContainer: LinearLayout
    private lateinit var etMessage: EditText
    private lateinit var scrollView: ScrollView
    private var chatId: Int = -1
    private var currentUserEmail: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        currentUserEmail = intent.getStringExtra("USER_EMAIL")
        chatId = intent.getIntExtra("CHAT_ID", -1)

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

        lifecycleScope.launch {
            val chat = db.chatDao().getChatById(chatId)
            if (chat != null) {
                val partnerEmail = if (chat.buyerEmail == currentUserEmail) chat.sellerEmail else chat.buyerEmail
                val partner = db.userDao().getUserByEmail(partnerEmail)
                tvPartnerName.text = "Chat with ${partner?.firstName ?: partnerEmail}"
                
                val book = db.bookDao().getBookById(chat.bookId)
                tvBookTitle.text = "About: ${book?.title ?: "Unknown Book"}"

                loadMessages(db)
            }
        }

        btnSend.setOnClickListener {
            val content = etMessage.text.toString().trim()
            if (content.isNotEmpty()) {
                sendMessage(db, content)
            }
        }
    }

    private fun loadMessages(db: AppDatabase) {
        lifecycleScope.launch {
            val messages = db.chatDao().getMessagesForChat(chatId)
            displayMessages(messages)
        }
    }

    private fun displayMessages(messages: List<Message>) {
        messagesContainer.removeAllViews()
        for (message in messages) {
            val isSentByMe = message.senderEmail == currentUserEmail
            val layoutId = if (isSentByMe) R.layout.item_message_sent else R.layout.item_message_received
            val view = layoutInflater.inflate(layoutId, messagesContainer, false)
            view.findViewById<TextView>(R.id.tvMessageContent).text = message.content
            view.findViewById<TextView>(R.id.tvSenderName).text = message.senderName
            messagesContainer.addView(view)
        }
        scrollView.post { scrollView.fullScroll(android.view.View.FOCUS_DOWN) }
    }

    private fun sendMessage(db: AppDatabase, content: String) {
        lifecycleScope.launch {
            val chat = db.chatDao().getChatById(chatId)
            val myName = if (chat?.buyerEmail == currentUserEmail) chat?.buyerName else chat?.sellerName
            
            val message = Message(
                chatId = chatId,
                senderEmail = currentUserEmail!!,
                senderName = myName ?: "",
                content = content
            )
            db.chatDao().insertMessage(message)
            
            // Update last message in chat
            chat?.let {
                db.chatDao().updateChat(it.copy(lastMessage = content, timestamp = System.currentTimeMillis()))
            }
            
            etMessage.text.clear()
            loadMessages(db)
        }
    }
}
