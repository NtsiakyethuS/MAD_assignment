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

class AdminActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var statsLayout: LinearLayout
    private lateinit var db: AppDatabase

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

        findViewById<Button>(R.id.btnViewUsers).setOnClickListener { showUsers() }
        findViewById<Button>(R.id.btnViewBooks).setOnClickListener { showBooks() }
        findViewById<Button>(R.id.btnViewComplaints).setOnClickListener { showComplaints() }
        findViewById<Button>(R.id.btnViewChats).setOnClickListener { showChats() }
        findViewById<Button>(R.id.btnViewStats).setOnClickListener { showStats() }

        showUsers() // Default view
    }

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
                    
                    holder.itemView.setOnLongClickListener {
                        AlertDialog.Builder(this@AdminActivity)
                            .setTitle("Manage User")
                            .setMessage("Do you want to delete this account?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch {
                                    db.userDao().deleteUser(user.email)
                                    showUsers()
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

                    holder.itemView.setOnLongClickListener {
                        AlertDialog.Builder(this@AdminActivity)
                            .setTitle("Manage Book")
                            .setMessage("Do you want to delete this book?")
                            .setPositiveButton("Delete") { _, _ ->
                                lifecycleScope.launch {
                                    db.bookDao().deleteBook(book.id)
                                    showBooks()
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

                    holder.itemView.setOnClickListener {
                        val intent = Intent(this@AdminActivity, ChatActivity::class.java)
                        intent.putExtra("CHAT_ID", chat.id)
                        intent.putExtra("USER_EMAIL", "ADMIN") // Signal it's an admin view
                        intent.putExtra("IS_ADMIN", true)
                        startActivity(intent)
                    }
                }
                override fun getItemCount() = chats.size
            }
        }
    }

    private fun showStats() {
        recyclerView.visibility = View.GONE
        statsLayout.visibility = View.VISIBLE
        lifecycleScope.launch {
            val usersCount = db.userDao().getAllUsers().size
            val booksCount = db.bookDao().getAllBooks().size
            val avgRating = db.adminDao().getAverageRating() ?: 0f

            findViewById<TextView>(R.id.tvTotalUsers).text = "Total Users: $usersCount"
            findViewById<TextView>(R.id.tvTotalBooks).text = "Total Books: $booksCount"
            findViewById<TextView>(R.id.tvAvgRating).text = "Avg Satisfaction: ${String.format("%.1f", avgRating)} / 5.0"
        }
    }
}
