/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Admin Dashboard.
 * 2. Navigation:
 *    - Buttons to toggle between Users, Books, Complaints, Chats, and Stats.
 * 3. Manage Users:
 *    - Fetch all users from database.
 *    - Long-press a user to trigger a deletion dialog.
 * 4. Manage Books:
 *    - Fetch all book listings.
 *    - Long-press a book to delete it from the marketplace.
 * 5. View Complaints:
 *    - List all support tickets/complaints sent by users.
 * 6. Monitor Chats:
 *    - List all active conversations between users.
 *    - Click a chat to view the message history in read-only mode.
 * 7. System Stats:
 *    - Calculate total users, total books, and average feedback ratings.
 */

package com.example.sharedtextview

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sharedtextview.database.AppDatabase
import com.example.sharedtextview.database.User
import com.example.sharedtextview.database.Book
import com.example.sharedtextview.database.Complaint
import com.example.sharedtextview.database.Chat
import android.content.Intent
import kotlinx.coroutines.launch

/**
 * AdminActivity provides the interface for system administrators to manage data.
 */
class AdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statsLayout: LinearLayout
    private lateinit var db: AppDatabase

    /**
     * Sets up the dashboard and default view.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin)

        db = AppDatabase.getDatabase(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        recyclerView = findViewById(R.id.adminRecyclerView)
        statsLayout = findViewById(R.id.statsLayout)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // PSEUDO: Set listeners for each management category
        findViewById<Button>(R.id.btnViewUsers).setOnClickListener { showUsers() }
        findViewById<Button>(R.id.btnViewBooks).setOnClickListener { showBooks() }
        findViewById<Button>(R.id.btnViewComplaints).setOnClickListener { showComplaints() }
        findViewById<Button>(R.id.btnViewChats).setOnClickListener { showChats() }
        findViewById<Button>(R.id.btnViewStats).setOnClickListener { showStats() }

        showUsers() // PSEUDO: Show users by default
    }

    /**
     * Displays a list of all registered users with management options.
     */
    private fun showUsers() {
        recyclerView.visibility = View.VISIBLE
        statsLayout.visibility = View.GONE
        lifecycleScope.launch {
            val users = db.userDao().getAllUsers()
            recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val user = users[position]
                    val text1 = holder.itemView.findViewById<TextView>(android.R.id.text1)
                    val text2 = holder.itemView.findViewById<TextView>(android.R.id.text2)
                    text1.text = "${user.firstName} ${user.lastName} (${user.email})"
                    text2.text = "Password: ${user.password} | Admin: ${user.isAdmin}"
                    
                    // PSEUDO: Enable user deletion on long click
                    holder.itemView.setOnLongClickListener {
                        AlertDialog.Builder(this@AdminActivity)
                            .setTitle("Manage User")
                            .setMessage("Do you want to delete this account?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch {
                                    db.userDao().deleteUser(user.email)
                                    showUsers() // Refresh list
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                }
                override fun getItemCount() = users.size
            }
        }
    }

    /**
     * Displays a list of all listed books with deletion capability.
     */
    private fun showBooks() {
        recyclerView.visibility = View.VISIBLE
        statsLayout.visibility = View.GONE
        lifecycleScope.launch {
            val books = db.bookDao().getAllBooks()
            recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val book = books[position]
                    val text1 = holder.itemView.findViewById<TextView>(android.R.id.text1)
                    val text2 = holder.itemView.findViewById<TextView>(android.R.id.text2)
                    text1.text = "${book.title} - ${book.author}"
                    text2.text = "Price: ${book.price} | Seller: ${book.sellerEmail}"

                    // PSEUDO: Enable book deletion on long click
                    holder.itemView.setOnLongClickListener {
                        AlertDialog.Builder(this@AdminActivity)
                            .setTitle("Manage Book")
                            .setMessage("Do you want to delete this book?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch {
                                    db.bookDao().deleteBook(book.id)
                                    showBooks() // Refresh list
                                }
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                        true
                    }
                }
                override fun getItemCount() = books.size
            }
        }
    }

    /**
     * Lists all complaints submitted via the support system.
     */
    private fun showComplaints() {
        recyclerView.visibility = View.VISIBLE
        statsLayout.visibility = View.GONE
        lifecycleScope.launch {
            val complaints = db.adminDao().getAllComplaints()
            recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val comp = complaints[position]
                    val text1 = holder.itemView.findViewById<TextView>(android.R.id.text1)
                    val text2 = holder.itemView.findViewById<TextView>(android.R.id.text2)
                    text1.text = "From: ${comp.userEmail} | ${comp.subject}"
                    text2.text = comp.message
                }
                override fun getItemCount() = complaints.size
            }
        }
    }

    /**
     * Lists all user-to-user and user-to-admin chats.
     */
    private fun showChats() {
        recyclerView.visibility = View.VISIBLE
        statsLayout.visibility = View.GONE
        lifecycleScope.launch {
            val chats = db.chatDao().getAllChats()
            recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                    val view = layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
                    return object : RecyclerView.ViewHolder(view) {}
                }
                override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                    val chat = chats[position]
                    val text1 = holder.itemView.findViewById<TextView>(android.R.id.text1)
                    val text2 = holder.itemView.findViewById<TextView>(android.R.id.text2)
                    text1.text = "${chat.buyerName} & ${chat.sellerName}"
                    text2.text = "Last: ${chat.lastMessage}"

                    // PSEUDO: Allow admin to view the conversation
                    holder.itemView.setOnClickListener {
                        val intent = Intent(this@AdminActivity, ChatActivity::class.java)
                        intent.putExtra("CHAT_ID", chat.id)
                        intent.putExtra("USER_EMAIL", "ADMIN")
                        intent.putExtra("IS_ADMIN", true) // Open in read-only mode
                        startActivity(intent)
                    }
                }
                override fun getItemCount() = chats.size
            }
        }
    }

    /**
     * Aggregates and displays system metrics.
     */
    private fun showStats() {
        recyclerView.visibility = View.GONE
        statsLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            // PSEUDO: Fetch counts and averages from various DAOs
            val usersCount = db.userDao().getAllUsers().size
            val booksCount = db.bookDao().getAllBooks().size
            val avgRating = db.adminDao().getAverageRating() ?: 0f

            findViewById<TextView>(R.id.tvTotalUsers).text = "Total Users: $usersCount"
            findViewById<TextView>(R.id.tvTotalBooks).text = "Total Books: $booksCount"
            findViewById<TextView>(R.id.tvAvgRating).text = "Avg Satisfaction: ${String.format("%.1f", avgRating)} / 5.0"
        }
    }
}
