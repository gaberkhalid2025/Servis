package com.example.data

data class Category(
    val id: String,
    val name: String,
    val icon: String, // Emoji representation
    val parentId: String? = null, // Supports sub-categories
    val isPinned: Boolean = false
)

data class ServiceProvider(
    val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val address: String,
    val neighborhood: String,
    val city: String,
    val profilePhoto: String, // Base64 data or URI
    val identityPhoto: String? = null,
    val isPinned: Boolean = false, // Pinned at top
    val isPremium: Boolean = false, // "الشارة المميزة" (Featured status)
    val subscriptionStatus: String = "active", // active, suspended, canceled
    val isVerified: Boolean = false,
    val rating: Float = 5.0f,
    val latitude: Double = 15.3694, // Simulating coordinates in Sana'a
    val longitude: Double = 44.1910
)

data class PendingProvider(
    val id: String,
    val name: String,
    val phone: String,
    val categoryId: String,
    val address: String,
    val neighborhood: String,
    val city: String,
    val profilePhoto: String, // mandatory selfie/personal photo
    val identityPhoto: String? = null, // optional ID upload
    val hasLocation: Boolean = false,
    val latitude: Double = 15.3694,
    val longitude: Double = 44.1910,
    val status: String = "pending", // pending, approved, rejected
    val rejectReason: String? = null
)

data class Supervisor(
    val id: String,
    val name: String,
    var pinCode: String, // Passcode / password
    var canEditCategories: Boolean = true,
    var canDeleteProviders: Boolean = true,
    var canViewBackup: Boolean = true,
    var canModifyConfigs: Boolean = true
)

data class BackupHistory(
    val id: String,
    val timestamp: Long,
    val status: String, // "success" or "failed"
    val chosenPath: String, // Phone Storage, SD Card, Google Drive
    val description: String,
    val errorMessage: String? = null
)

data class AppConfigs(
    var appName: String = "دليل الخدمات اليمني",
    var supportPhone: String = "777644670",
    var supportEmail: String = "support@guide.ye",
    var supportWhatsapp: String = "777644670",
    var footerText: String = "MAW 777644670",
    var footerScale: Float = 0.5f, // 50% smaller by default
    var footerTransparency: Float = 0.8f,
    
    // Smart assistant customizable options
    var assistantScale: Float = 0.6f, // Scale factor: 0.1 to 1.5
    var assistantTransparency: Float = 1.0f, // Alpha: 0.0 to 1.0
    var assistantHidden: Boolean = false,
    var customAssistantIconBase64: String? = null, // Custom PNG/SVG base64 data
    
    // About App widget options
    var aboutAppIconBase64: String? = null,
    var aboutAppScale: Float = 0.6f,
    var aboutAppTransparency: Float = 1.0f,
    var aboutAppHidden: Boolean = false,

    // Google Maps integration status
    var googleMapsEnabled: Boolean = true,
    
    // Welcome message configurations
    var welcomeText: String = "أهلاً بك في الدليل الرسمي للخدمات والصيانة في اليمن 🇾🇪",
    var welcomeTextSize: Float = 16f, // in sp
    var welcomePosition: String = "top", // "top", "below_search", "below_map"
    var welcomeImageBase64: String? = null, // Custom image from phone memory instead of text
    var showWelcomeBanner: Boolean = true,

    // Default search ranges
    var defaultSearchRangeKm: Int = 10,
    var isMaintenanceMode: Boolean = false,

    // Input text field styling customizable by admin
    var inputBackgroundColor: String = "#1E293B", // hex color string
    var inputTextColor: String = "#FFFFFF" // hex color string
)

data class Review(
    val id: String,
    val providerId: String,
    val authorName: String,
    val rating: Int,
    val comment: String
)

data class ChatMessage(
    val id: String,
    val senderId: String, // "user", "admin", or providerId
    val senderName: String,
    val recipientId: String,
    val text: String,
    val timestamp: Long
)

// Full App State Schema for Backup and Restore Export/Import
data class AppStateBackup(
    val timestamp: Long,
    val categories: List<Category>,
    val providers: List<ServiceProvider>,
    val pendingProviders: List<PendingProvider>,
    val supervisors: List<Supervisor>,
    val backups: List<BackupHistory>,
    val configs: AppConfigs
)
