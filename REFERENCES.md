# References for Textbook Connect Assignment

This document lists the technical resources, libraries, and official documentation used in the development of the **Textbook Connect** Android application.

## 1. Official Documentation
*   **Android Developers Documentation**: Used as the primary resource for implementing Activity lifecycles, Intent navigation, and UI components.
    *   URL: [https://developer.android.com/docs](https://developer.android.com/docs)
*   **Kotlin Language Documentation**: Referenced for syntax, coroutines (lifecycleScope), and data classes.
    *   URL: [https://kotlinlang.org/docs/home.html](https://kotlinlang.org/docs/home.html)
*   **Material Design 3 (M3)**: Guidelines used for the application's visual style and component implementation (Buttons, BottomNavigationView, etc.).
    *   URL: [https://m3.material.io/](https://m3.material.io/)

## 2. Libraries & Frameworks
*   **Jetpack Room Persistence Library**: Used for local database management and ORM (Object-Relational Mapping).
    *   Documentation: [https://developer.android.com/training/data-storage/room](https://developer.android.com/training/data-storage/room)
*   **View Binding**: Utilized in `MainActivity` and other components for safe interaction with XML layouts.
    *   Documentation: [https://developer.android.com/topic/libraries/view-binding](https://developer.android.com/topic/libraries/view-binding)
*   **AndroidX AppCompat & Core KTX**: Essential libraries for backward compatibility and Kotlin extensions.
    *   Documentation: [https://developer.android.com/jetpack/androidx](https://developer.android.com/jetpack/androidx)
*   **ConstraintLayout**: The primary layout manager used for creating responsive and centralized UI designs.
    *   Documentation: [https://developer.android.com/training/constraintlayout](https://developer.android.com/training/constraintlayout)

## 3. APIs & Patterns
*   **Activity Result API**: Specifically `ActivityResultContracts.GetContent()` for gallery image selection in the Profile and Sell screens.
*   **Repository Pattern / DAO**: Implementation of Data Access Objects for database interaction.
*   **Coroutines**: Used for asynchronous database operations to ensure a smooth UI experience.

## 4. Development Tools
*   **Android Studio**: The Integrated Development Environment (IDE) used for building, debugging, and testing the application.
*   **Gradle**: Build automation system for dependency management and APK/AAB generation.
