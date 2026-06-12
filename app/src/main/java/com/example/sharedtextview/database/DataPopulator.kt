package com.example.sharedtextview.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DataPopulator {
    suspend fun populateBooks(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val bookDao = db.bookDao()
        
        // Check if already populated
        val existingBooks = withContext(Dispatchers.IO) { bookDao.getAllBooks() }
        if (existingBooks.size > 5) return // Already populated with more than just a few books

        val books = mutableListOf<Book>()
        val adminEmail = "admin@textbookconnect.com"

        // Education
        val educationBooks = listOf(
            "Teaching in the 21st Century" to "Dr. Sarah Miller",
            "Educational Psychology" to "Prof. Robert James",
            "Curriculum Development" to "Elena Rodriguez",
            "Classroom Management" to "Michael Thompson",
            "History of Education" to "Dr. David Brooks",
            "Inclusive Education" to "Linda Chen",
            "Language and Literacy" to "Sarah Jenkins",
            "Mathematics Education" to "Prof. Alan Smith",
            "Science Education for Kids" to "Dr. Emily White",
            "Philosophy of Education" to "Dr. Mark Wilson"
        )
        educationBooks.forEachIndexed { i, (title, author) ->
            books.add(Book(title = title, author = author, edition = "${i % 3 + 1}st Edition", price = "R ${300 + i * 10}", sellerEmail = adminEmail, faculty = "Education"))
        }

        // Law
        val lawBooks = listOf(
            "Introduction to Law" to "Prof. Michael Cohen",
            "Constitutional Law" to "Dr. Jessica Lee",
            "Criminal Law" to "Advocate Ben Zulu",
            "Contract Law" to "Samantha Reed",
            "Property Law" to "David Goldberg",
            "Human Rights Law" to "Justice Maria Garcia",
            "Administrative Law" to "Prof. Kevin Hart",
            "Family Law" to "Dr. Chloe Adams",
            "International Law" to "Prof. Peter Pan",
            "Legal Ethics" to "Advocate Tim Burton"
        )
        lawBooks.forEachIndexed { i, (title, author) ->
            books.add(Book(title = title, author = author, edition = "${i % 2 + 2}nd Edition", price = "R ${450 + i * 20}", sellerEmail = adminEmail, faculty = "Law"))
        }

        // Information Technology
        val itBooks = listOf(
            "Introduction to Programming" to "Tony Gaddis",
            "Data Structures and Algorithms" to "Robert Lafore",
            "Database Systems" to "Abraham Silberschatz",
            "Computer Networks" to "Andrew S. Tanenbaum",
            "Software Engineering" to "Ian Sommerville",
            "Operating Systems" to "William Stallings",
            "Cyber Security" to "Dr. Alice Smith",
            "Cloud Computing" to "Prof. John Doe",
            "Artificial Intelligence" to "Stuart Russell",
            "Mobile App Development" to "Dr. Chris Stewart"
        )
        itBooks.forEachIndexed { i, (title, author) ->
            books.add(Book(title = title, author = author, edition = "${i % 4 + 1}th Edition", price = "R ${500 + i * 15}", sellerEmail = adminEmail, faculty = "Information Technology"))
        }

        withContext(Dispatchers.IO) {
            books.forEach { bookDao.insertBook(it) }
            
            val userDao = db.userDao()
            // Ensure the main admin user exists
            if (userDao.getUserByEmail("root") == null) {
                userDao.insertUser(User(
                    email = "root",
                    firstName = "System",
                    lastName = "Administrator",
                    physicalAddress = "Control Center",
                    postalAddress = "Root Folder",
                    university = "SharedtextView System",
                    campus = "Global",
                    faculty = "Management",
                    password = "root.admin",
                    isAdmin = true
                ))
            }

            // Also ensure the old admin user exists for legacy books
            if (userDao.getUserByEmail(adminEmail) == null) {
                userDao.insertUser(User(
                    email = adminEmail,
                    firstName = "Admin",
                    lastName = "User",
                    physicalAddress = "University Campus",
                    postalAddress = "P.O. Box 123",
                    university = "Shared University",
                    campus = "Main Campus",
                    faculty = "Administration",
                    password = "adminpassword",
                    isAdmin = true
                ))
            }
        }
    }
}
