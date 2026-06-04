package com.example.data

import kotlinx.serialization.Serializable

@Serializable
data class Provider(
    val id: String,
    val name: String,
    val title: String,
    val bio: String,
    val phone: String,
    val mainCategoryId: String,
    val subCategoryId: String,
    val region: String,
    val avatar: String = "",
    val isVerified: Boolean = false, // الشارة الزرقاء
    val isPinned: Boolean = false,   // تثبيت في الصدارة
    val isRecommended: Boolean = false, // موصى به
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0,
    val lat: Double, // موقع جغرافي للمخرطة/الخارطة (Pins)
    val lng: Double,
    val isBlocked: Boolean = false, // تعطيل او ايقاف الخدمة
    val selfieUri: String = "",
    val idCardUri: String = ""
)

@Serializable
data class Review(
    val id: String,
    val providerId: String,
    val reviewerName: String,
    val rating: Int, // 1 to 5 stars
    val comment: String,
    val timestamp: Long
)

@Serializable
data class ChatMessage(
    val id: String,
    val chatRoomId: String,
    val senderId: String,
    val senderName: String,
    val content: String,
    val timestamp: Long,
    val imageAttachedUri: String? = null
)

@Serializable
data class ChatRoom(
    val id: String,
    val userId: String,
    val userName: String,
    val providerId: String,
    val providerName: String,
    val lastMessage: String,
    val lastTimestamp: Long,
    val isBlockedByAdmin: Boolean = false
)

@Serializable
data class AppConfigs(
    val primaryHex: String = "#0284C7", // الأزرق الأساسي
    val secondaryHex: String = "#EF4444", // الأحمر الأساسي
    val smartAssistantIconUrl: String = "ic_assistant", // شكل الأيقونة للمساعد المذكور
    val smartAssistantIconSize: Int = 48,
    val smartAssistantVisible: Boolean = true,
    val appInfoIconUrl: String = "ic_info",
    val appInfoIconSize: Int = 42,
    val appInfoVisible: Boolean = true,
    // إحداثيات الأيقونة العائمة عى الشاشة
    val bubbleXOffset: Float = 16f,
    val bubbleYOffset: Float = 16f,
    val bubbleSize: Int = 60,
    val fcmEnabled: Boolean = true, // FCM Notification Toggle
    val activeThemeId: String = "custom"
)

@Serializable
data class NotificationTemplate(
    val id: String, // welcome, appointment, bill
    val nameAr: String,
    val title: String,
    val body: String,
    val isEnabled: Boolean,
    val sendDelayMinutes: Int
)

@Serializable
data class ReportNotification(
    val id: String,
    val title: String,
    val content: String,
    val type: String, // COMPLAINT, UNAUTHORIZED_LOGIN, SUBSCRIPTION_REQUEST
    val timestamp: Long,
    val isReviewed: Boolean = false
)

@Serializable
data class PendingProvider(
    val id: String,
    val name: String,
    val title: String,
    val bio: String,
    val phone: String,
    val region: String,
    val selfieUri: String,
    val idCardUri: String,
    val timestamp: Long
)
