package com.example.data

import androidx.compose.runtime.Immutable

@Immutable
data class MainCategory(
    val id: String = "",
    val name: String = "",
    val iconCode: String = "", // e.g. "home", "medical", "school", "car"
    val order: Int = 0
)

@Immutable
data class SubCategory(
    val id: String = "",
    val parentId: String = "",
    val name: String = "",
    val order: Int = 0
)

@Immutable
data class ServiceProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val mainCategoryId: String = "",
    val subCategoryId: String = "",
    val address: String = "",
    val district: String = "",
    val gpsCoordinates: String? = null,
    val avatarUrl: String = "", // Simulated picture URI or placeholder index
    val idCardUrl: String? = null,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isSubscribed: Boolean = false, // True means they have a Premium Badge
    val ratingSum: Float = 0f,
    val ratingCount: Int = 0
) {
    val averageRating: Float
        get() = if (ratingCount > 0) ratingSum / ratingCount else 0f
}

@Immutable
data class PendingProvider(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val mainCategoryId: String = "",
    val subCategoryId: String = "",
    val address: String = "",
    val district: String = "",
    val gpsCoordinates: String? = null,
    val avatarUrl: String = "",
    val idCardUrl: String? = null,
    val status: String = "pending", // "pending", "approved", "rejected"
    val rejectionReason: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class AppColors(
    val themeName: String = "Cosmic Slate", // "Cosmic Slate", "Charcoal Gold", "Royal Emerald"
    val textColorName: String = "Bright White" // "Bright White", "Light Gold", "Vibrant Silver"
)

@Immutable
data class AppConfigs(
    val appName: String = "دليلك لكل الخدمات",
    val supportEmail: String = "support@services-guide.example",
    val supportPhone: String = "777644670",
    val supportWhatsApp: String = "777644670",
    val footerPromoText: String = "MAW 777644670",
    val welcomeMessage: String = "مرحباً بك في دليلك لكل الخدمات الفوري!",
    val adminPassword: String = "maher736462",
    val isMaintenanceActive: Boolean = false,
    val smartAssistantSize: Float = 56f,
    val showPromoFooter: Boolean = true,
    val showDashboardFavorites: Boolean = true,
    val showDashboardCallHistory: Boolean = true,
    val dashboardFavoritesFirst: Boolean = true,
    val dashboardCustomMessage: String = "مرحباً بك في لوحة تحكمك المفضلة وسجل التواصل!",
    val categoryIconSize: Float = 32f
)

@Immutable
data class Review(
    val id: String = "",
    val providerId: String = "",
    val userName: String = "",
    val rating: Float = 5f,
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class Report(
    val id: String = "",
    val providerId: String = "",
    val userName: String = "",
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class Banner(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val durationDays: Int = 7,
    val redirectUrl: String = "",
    val bannerSize: String = "Medium", // "Small", "Medium", "Large"
    val bannerType: String = "Service", // "Service", "External"
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class PromotedAd(
    val id: String = "",
    val providerId: String? = null,
    val type: String = "Text", // "Text", "Image", "Video"
    val title: String = "",
    val content: String = "",
    val durationDays: Int = 5,
    val budget: Double = 100.0,
    val timestamp: Long = System.currentTimeMillis()
)

@Immutable
data class City(
    val id: String = "",
    val name: String = "",
    val districts: List<String> = emptyList()
)

@Immutable
data class FcmChannelState(
    val id: String = "",
    val name: String = "",
    val isEnabled: Boolean = true
)

@Immutable
data class Moderator(
    val id: String = "",
    val name: String = "",
    val username: String = "",
    val password: String = ""
)

@Immutable
data class ContactLog(
    val id: String = "",
    val providerId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val mode: String = "Call" // "Call" or "WhatsApp"
)


