/* 
 * PSEUDO-CODE LOGIC (DAOs):
 * These interfaces define how the application interacts with the SQLite database via Room.
 * 
 * 1. UserDao:
 *    - Create/Update user profiles.
 *    - Retrieve specific users by email (Login/Profile).
 *    - Identify admin accounts.
 *    - Remove users (Admin management).
 * 
 * 2. BookDao:
 *    - Post new books for sale.
 *    - Fetch all available listings.
 *    - Search books using keywords (SQL LIKE) across multiple fields (Title, Author, etc.).
 *    - Filter books by specific categories.
 * 
 * 3. AdminDao:
 *    - Store and retrieve user complaints.
 *    - Handle system feedback and calculate average user satisfaction scores.
 * 
 * 4. ChatDao:
 *    - Manage message threads between buyers and sellers.
 *    - Store individual chat messages with timestamps.
 *    - Retrieve conversation history for specific users.
 */

package com.example.sharedtextview.database

import androidx.room.*

/**
 * Data Access Object for User related database operations.
 */
@Dao
interface UserDao {
    // PSEUDO: Insert a user, replace if they already exist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // PSEUDO: Find a single user by their email address
    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    // PSEUDO: Get all users who have administrative privileges
    @Query("SELECT * FROM users WHERE isAdmin = 1")
    suspend fun getAdmins(): List<User>

    // PSEUDO: List every user in the system
    @Query("SELECT * FROM users")
    suspend fun getAllUsers(): List<User>

    @Update
    suspend fun updateUser(user: User)

    // PSEUDO: Permanent account removal
    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)
}

/**
 * Data Access Object for Book related database operations.
 */
@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: Book)

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

    // PSEUDO: Search across all book text fields using partial matches
    @Query("SELECT * FROM books WHERE title LIKE :searchQuery OR author LIKE :searchQuery OR edition LIKE :searchQuery OR faculty LIKE :searchQuery")
    suspend fun searchBooks(searchQuery: String): List<Book>

    @Query("SELECT * FROM books WHERE title LIKE :searchQuery")
    suspend fun searchBooksByTitle(searchQuery: String): List<Book>

    @Query("SELECT * FROM books WHERE author LIKE :searchQuery")
    suspend fun searchBooksByAuthor(searchQuery: String): List<Book>

    @Query("SELECT * FROM books WHERE edition LIKE :searchQuery")
    suspend fun searchBooksByEdition(searchQuery: String): List<Book>

    @Query("SELECT * FROM books WHERE faculty LIKE :searchQuery")
    suspend fun searchBooksByFaculty(searchQuery: String): List<Book>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getBookById(id: Int): Book?

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteBook(id: Int)
}

/**
 * Data Access Object for Admin/Support related operations.
 */
@Dao
interface AdminDao {
    @Insert
    suspend fun insertComplaint(complaint: Complaint)

    @Query("SELECT * FROM complaints ORDER BY timestamp DESC")
    suspend fun getAllComplaints(): List<Complaint>

    @Update
    suspend fun updateComplaint(complaint: Complaint)

    @Insert
    suspend fun insertFeedback(feedback: Feedback)

    @Query("SELECT * FROM feedback ORDER BY timestamp DESC")
    suspend fun getAllFeedback(): List<Feedback>

    // PSEUDO: Calculate the average rating from all feedback entries
    @Query("SELECT AVG(rating) FROM feedback")
    suspend fun getAverageRating(): Float?
}

/**
 * Data Access Object for the Messaging system.
 */
@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat): Long

    // PSEUDO: Find a chat session between two specific users for a specific book
    @Query("SELECT * FROM chats WHERE (buyerEmail = :email1 AND sellerEmail = :email2 AND bookId = :bookId) OR (buyerEmail = :email2 AND sellerEmail = :email1 AND bookId = :bookId)")
    suspend fun getChatBetweenUsers(email1: String, email2: String, bookId: Int): Chat?

    // PSEUDO: Get all conversations for a specific user, newest first
    @Query("SELECT * FROM chats WHERE buyerEmail = :email OR sellerEmail = :email ORDER BY timestamp DESC")
    suspend fun getChatsForUser(email: String): List<Chat>

    @Query("SELECT * FROM chats ORDER BY timestamp DESC")
    suspend fun getAllChats(): List<Chat>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Int): Chat?

    @Update
    suspend fun updateChat(chat: Chat)

    @Insert
    suspend fun insertMessage(message: Message)

    // PSEUDO: Get conversation history for a specific chat thread
    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(chatId: Int): List<Message>
}
