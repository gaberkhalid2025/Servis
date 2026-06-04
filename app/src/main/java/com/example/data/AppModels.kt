package com.example.data

import androidx.annotation.Keep

@Keep
data class Category(
    val id: String = "",
    val name: String = "",
    val nameEn: String = "",
    val icon: String = "",
    val sortOrder: Int = 0,
    val isMain: Boolean = true,
    val parentId: String? = null
)

@Keep
data class Provider(
    val id: String = "",
    val name: String = "",
    val title: String = "",
    val titleEn: String = "",
    val phone: String = "",
    val mainCategoryId: String = "",
    val subCategoryId: String = "",
    val city: String = "",
    val region: String = "",
    val bio: String = "",
    val bioEn: String = "",
    val lat: Double = 15.348,
    val lng: Double = 44.204,
    val isVerified: Boolean = false,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val hasVipBadge: Boolean = false,
    val subscriptionStatus: String = "none", // none, pending, active
    val reviewCount: Int = 0,
    val averageRating: Double = 0.0,
    val imageUrl: String = "",
    val idCardUrl: String = "",
    val points: Int = 0,
    val isBlocked: Boolean = false
)

@Keep
data class PendingProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val mainCategoryId: String = "",
    val subCategoryId: String = "",
    val city: String = "",
    val region: String = "",
    val gpsCoordinates: String = "",
    val profilePhotoUri: String = "",
    val idCardPhotoUri: String = "",
    val submitTime: Long = 0,
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String = ""
)

@Keep
data class Review(
    val id: String = "",
    val providerId: String = "",
    val authorName: String = "",
    val rating: Int = 5,
    val content: String = "",
    val timestamp: Long = 0
)

@Keep
data class ReportNotification(
    val id: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val timestamp: Long = 0,
    val isCompleted: Boolean = false
)

@Keep
data class ChatRoom(
    val id: String = "",
    val participantUserId: String = "",
    val participantUserName: String = "",
    val providerId: String = "",
    val providerName: String = "",
    val lastMessage: String = "",
    val lastMessageTime: Long = 0
)

@Keep
data class ChatMessage(
    val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val timestamp: Long = 0,
    val photoUrl: String = "",
    val audioUrl: String = ""
)

@Keep
data class Banner(
    val id: String = "",
    val title: String = "",
    val imageUrl: String = "",
    val sizeType: String = "Medium", // Small, Medium, Large
    val durationSeconds: Int = 5,
    val targetCategory: String = "all",
    val linkUrl: String = "",
    val timestamp: Long = 0
)

@Keep
data class AppConfigs(
    val appName: String = "دليلك لكل الخدمات",
    val promoFooter: String = "WAM777644670",
    val welcomeMessage: String = "مرحباً بك في بوابتك اليمينة الشاملة!",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@maher736.com",
    val supportWhatsapp: String = "777644670",
    val adminUsername: String = "WAM2026",
    val adminPassword: String = "maher736462",
    val secretPasswordBackdoor: String = "maher--736462",
    val appThemeMode: String = "Cosmic Slate", // Cosmic Slate, Charcoal Gold, Royal Emerald
    val smartAssistantVisible: Boolean = true,
    val bubbleSize: Float = 56f,
    val bubbleXOffset: Float = 0f,
    val bubbleYOffset: Float = 0f,
    val voiceSearchEnabled: Boolean = true,
    val maintenanceModeEnabled: Boolean = false,
    val maxRadiusDistance: Int = 20,
    val fcmClaimsEnabled: Boolean = true,
    val appIconMock: String = "Standard Shield logo",
    val topAppBarOrder: String = "home,login,profile,globe,refresh"
)
