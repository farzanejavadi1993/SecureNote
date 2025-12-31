# Secure Note App (KMP)

A modern, secure, and cross-platform note-taking application built with Kotlin Multiplatform (KMP), supporting Android and Desktop. This app is designed with a focus on security, modern architecture, and a clean user experience.

---

## ✨ Features

A comprehensive list of features demonstrating a robust and user-friendly application.

#### Core Functionality
- **📝 Create, Edit & Delete Notes:** A seamless and intuitive interface for managing notes.
- **💾 Persistent Storage:** Notes are saved locally and are available offline, powered by **Room Multiplatform**.
- **🔄 Real-time UI Updates:** The note list updates instantly in response to changes in the database.

#### Advanced UI & UX
- **🌓 Light & Dark Themes:** Automatically adapts to the system's theme for comfortable viewing day or night.
- **📱 Adaptive UI:** A responsive layout that provides a standard single-panel view on phones and a powerful **Master-Detail (Split-Screen)** view on tablets and desktops.
- **☑️ Multi-Select Mode:** Long-press a note to enter selection mode, allowing for bulk operations.
- **📤 Export Selected Notes:** Export only the specific notes you have selected.
- **🗑️ Delete Multiple Notes:** Quickly delete several notes at once.
- **✅ Confirmation Dialogs:** Safe and user-friendly dialogs confirm critical actions like deleting or exporting notes.

### 🛡️ Security Architecture
- **🔐 App PIN Lock:** Integrated authentication system requiring a 4-digit PIN. 
- **🔒 Manual Lock:** dedicated lock trigger allowing users to secure the session instantly.
- **🛡️ Data-at-Rest Encryption:** All note content is **encrypted** using a custom cipher before being written to the database. Even if the physical database file is compromised, the content remains unreadable.
- **📲 Privacy Screen:** (Android) Prevents the operating system from taking screenshots or displaying app content in the "Recent Apps" switcher.

> **A Note on the PIN Feature Implementation:**
>
> For the purpose of this showcase, the PIN lock is managed directly via the lock icon in the app bar. This allows for quick demonstration of the app's core security features (setting a PIN, locking, and removing it).
>
> In a full production application, this functionality would be moved to a dedicated **Settings screen**. This would provide a clearer and more intuitive user experience, allowing users to explicitly enable or disable the passcode and manage other security preferences, rather than toggling it from the main screen's app bar.

#### Platform-Specific Features
- **Android:**
    - **Scoped Storage:** Securely exports notes to the "Downloads" folder without requiring invasive storage permissions, following modern Android best practices.
- **Desktop:**
    - **Native File Picker:** Uses the system's native file picker dialog for a seamless export experience.

---

## 🛠️ Tech Stack & Architecture

This project is built with a modern, scalable, and testable technology stack, following **Clean Architecture** principles.

- **Framework:** [Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)
- **UI:** [Jetpack Compose for Multiplatform](https://github.com/JetBrains/compose-multiplatform)
- **Architecture:**
    - **Clean Architecture** (Domain, Data, Presentation layers)
    - **MVI** (Model-View-Intent) pattern for state management.
- **Navigation:** [Decompose](https://github.com/arkivanov/Decompose) for managing navigation and component lifecycle in a platform-agnostic way.
- **Dependency Injection:** [Koin](https://insert-koin.io/) for managing dependencies across all modules.
- **Database:** [Room Multiplatform](https://developer.android.com/kotlin/multiplatform/room) for robust, local SQL database storage.
- **Coroutines:** For asynchronous operations and background tasks.
- **Serialization:** `kotlinx.serialization` for handling data models.

---

## 🏗️ Project Structure

The project is organized into distinct layers, ensuring a clean separation of concerns.

## Demo Video
![Demo](Demo.MP4)