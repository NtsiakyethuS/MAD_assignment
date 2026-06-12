/* 
 * PSEUDO-CODE LOGIC:
 * 1. Initialize Database Seeding Process.
 * 2. Check if the database already contains books.
 *    - If there are already 5 or more books, skip population to avoid duplicates.
 * 3. Define Lists of Initial Books for different faculties:
 *    - Education (Titles, Authors, Editions, Prices).
 *    - Law (Titles, Authors, Editions, Prices).
 *    - Information Technology (Titles, Authors, Editions, Prices).
 * 4. Create and Insert Book Objects:
 *    - Loop through each list and construct Book objects.
 *    - Save these to the Book table in the database.
 * 5. Ensure Administrative Accounts:
 *    - Check if a 'root' admin user exists. If not, create one.
 *    - Check if the default support admin exists. If not, create one.
 */

package com.example.sharedtextview.database

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * DataPopulator is responsible for seeding the database with initial data on first run.
 */
object DataPopulator {

    /**
     * Fills the database with a selection of books and default admin accounts.
     */
    suspend fun populateBooks(context: Context) {
        val db = AppDatabase.getDatabase(context)
        val bookDao = db.bookDao()
        
        // PSEUDO: Avoid re-populating if data already exists
        val existingBooks = withContext(Dispatchers.IO) { bookDao.getAllBooks() }
        if (existingBooks.size > 5) return 

        val books = mutableListOf<Book>()
        val adminEmail = "admin@textbookconnect.com"

        // PSEUDO: Define sample books for Education faculty
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

        // PSEUDO: Define sample books for Law faculty
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

        // PSEUDO: Define sample books for IT faculty
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

        // PSEUDO: Perform database insertions on a background thread
        withContext(Dispatchers.IO) {
            books.forEach { bookDao.insertBook(it) }
            
            val userDao = db.userDao()
            
            // PSEUDO: Create the system root administrator if missing
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

            // PSEUDO: Create the default support administrator if missing
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
