package com.example.sharedtextview.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val firstName: String,
    val lastName: String,
    val physicalAddress: String,
    val postalAddress: String,
    val university: String,
    val campus: String,
    val faculty: String,
    val profileImageUri: String? = null,
    val password: String,
    val isAdmin: Boolean = false
)

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val author: String,
    val edition: String,
    val price: String,
    val imageUri: String? = null,
    val sellerEmail: String,
    val faculty: String = "General"
)

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: Int,
    val buyerEmail: String,
    val sellerEmail: String,
    val buyerName: String = "",
    val sellerName: String = "",
    val lastMessage: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val chatId: Int,
    val senderEmail: String,
    val senderName: String = "",
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "complaints")
data class Complaint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val subject: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)

@Entity(tableName = "feedback")
data class Feedback(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userEmail: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val timestamp: Long = System.currentTimeMillis()
)
