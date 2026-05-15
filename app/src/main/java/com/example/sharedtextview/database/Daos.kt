package com.example.sharedtextview.database

import androidx.room.*

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE email = :email")
    suspend fun deleteUser(email: String)
}

@Dao
interface BookDao {
    @Insert
    suspend fun insertBook(book: Book)

    @Query("SELECT * FROM books")
    suspend fun getAllBooks(): List<Book>

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
}

@Dao
interface ChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: Chat): Long

    @Query("SELECT * FROM chats WHERE (buyerEmail = :email1 AND sellerEmail = :email2 AND bookId = :bookId) OR (buyerEmail = :email2 AND sellerEmail = :email1 AND bookId = :bookId)")
    suspend fun getChatBetweenUsers(email1: String, email2: String, bookId: Int): Chat?

    @Query("SELECT * FROM chats WHERE buyerEmail = :email OR sellerEmail = :email ORDER BY timestamp DESC")
    suspend fun getChatsForUser(email: String): List<Chat>

    @Query("SELECT * FROM chats WHERE id = :chatId")
    suspend fun getChatById(chatId: Int): Chat?

    @Update
    suspend fun updateChat(chat: Chat)

    @Insert
    suspend fun insertMessage(message: Message)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getMessagesForChat(chatId: Int): List<Message>
}
