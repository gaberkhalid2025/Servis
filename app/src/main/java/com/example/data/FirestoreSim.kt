package com.example.data

import android.util.Base64
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

object FirestoreSim {
    private val gson = Gson()

    // -------------------------------------------------------------
    // Core Database Simulation States
    // -------------------------------------------------------------
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _providers = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val providers: StateFlow<List<ServiceProvider>> = _providers

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProviders: StateFlow<List<PendingProvider>> = _pendingProviders

    private val _supervisors = MutableStateFlow<List<Supervisor>>(emptyList())
    val supervisors: StateFlow<List<Supervisor>> = _supervisors

    private val _backupHistory = MutableStateFlow<List<BackupHistory>>(emptyList())
    val backupHistory: StateFlow<List<BackupHistory>> = _backupHistory

    private val _configs = MutableStateFlow(AppConfigs())
    val configs: StateFlow<AppConfigs> = _configs

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    // Auto-scheduler status warning tracker
    private val _schedulerFailedAlert = MutableStateFlow<String?>(null)
    val schedulerFailedAlert: StateFlow<String?> = _schedulerFailedAlert

    init {
        prepopulateData()
    }

    private fun prepopulateData() {
        // Prepopulate Categories
        val cats = listOf(
            Category("cat_plumb", "سباكة", "🚰", isPinned = true),
            Category("cat_elec", "كهرباء", "⚡", isPinned = true),
            Category("cat_carp", "نجارة", "🪚", isPinned = false),
            Category("cat_weld", "لحامة حديد", "👨‍🏭", isPinned = false),
            Category("cat_paint", "طلاء ودهانات", "🎨", isPinned = false),
            Category("cat_ac", "تكييف وتبريد", "❄️", isPinned = false),
            
            // Sub-categories
            Category("sub_plumb_leak", "إصلاح التسريب", "💧", parentId = "cat_plumb"),
            Category("sub_plumb_install", "تركيب مغاسل وخلاطات", "🚿", parentId = "cat_plumb"),
            Category("sub_elec_house", "توصيلات منزلية", "🔌", parentId = "cat_elec"),
            Category("sub_elec_generator", "صيانة مولدات", "⚙️", parentId = "cat_elec")
        )
        _categories.value = cats

        // Prepopulate Providers
        val provs = listOf(
            ServiceProvider(
                id = "prov_1",
                name = "المهندس فؤاد السباك",
                phone = "771234567",
                categoryId = "cat_plumb",
                address = "شارع حده، خلف مركز سيتي ستير",
                neighborhood = "حدة",
                city = "صنعاء",
                profilePhoto = "",
                isPinned = true,
                isPremium = true,
                subscriptionStatus = "active",
                rating = 4.9f,
                latitude = 15.3242,
                longitude = 44.1884
            ),
            ServiceProvider(
                id = "prov_2",
                name = "صالح الكهربائي",
                phone = "772345678",
                categoryId = "cat_elec",
                address = "شارع الجزائر، أمام مستشفى العلوم",
                neighborhood = "الجزائر",
                city = "صنعاء",
                profilePhoto = "",
                isPinned = false,
                isPremium = true,
                subscriptionStatus = "active",
                rating = 4.7f,
                latitude = 15.3415,
                longitude = 44.2012
            ),
            ServiceProvider(
                id = "prov_3",
                name = "أبو يمن للنجارة الحديثة",
                phone = "733456789",
                categoryId = "cat_carp",
                address = "المنصورة، تقاطع بئر فضل",
                neighborhood = "المنصورة",
                city = "عدن",
                profilePhoto = "",
                isPinned = false,
                isPremium = false,
                subscriptionStatus = "active",
                rating = 4.2f,
                latitude = 12.8331,
                longitude = 44.9922
            ),
            ServiceProvider(
                id = "prov_4",
                name = "عقيل فني تكييف وسخانات",
                phone = "711556677",
                categoryId = "cat_ac",
                address = "شارع تعز، جوار جولة النصر",
                neighborhood = "شعوب",
                city = "صنعاء",
                profilePhoto = "",
                isPinned = false,
                isPremium = false,
                subscriptionStatus = "suspended", // Suspended
                rating = 3.8f,
                latitude = 15.3615,
                longitude = 44.2255
            )
        )
        _providers.value = provs

        // Prepopulate Supervisors
        val sups = listOf(
            Supervisor("sup1", "الأدمن العام", "maher--736462", canEditCategories = true, canDeleteProviders = true, canViewBackup = true, canModifyConfigs = true),
            Supervisor("sup2", "المشرف عادل", "2222", canEditCategories = true, canDeleteProviders = false, canViewBackup = false, canModifyConfigs = false)
        )
        _supervisors.value = sups

        // Prepopulate Pending join requests
        val pends = listOf(
            PendingProvider(
                id = "pend_1",
                name = "بسام مهندس سخانات شمسية",
                phone = "775432100",
                categoryId = "cat_elec",
                address = "الحصبة، جوار مبنى التلفزيون",
                neighborhood = "الحصبة",
                city = "صنعاء",
                profilePhoto = ""
            )
        )
        _pendingProviders.value = pends

        // Prepopulate backup logs
        _backupHistory.value = listOf(
            BackupHistory(
                id = "back_1",
                timestamp = System.currentTimeMillis() - 7200000,
                status = "success",
                chosenPath = "Google Drive",
                description = "تم النسخ الاحتياطي التلقائي المجدول بنجاح."
            ),
            BackupHistory(
                id = "back_2",
                timestamp = System.currentTimeMillis() - 3600000,
                status = "failed",
                chosenPath = "SD Card",
                description = "النسخ الاحتياطي المجدول واجه عطل فني في الوصول للملفات.",
                errorMessage = "IOException: Outer memory permission denied or SD Card disconnected."
            )
        )

        // Set simulated auto-scheduler failure warning to alert supervisor
        _schedulerFailedAlert.value = "فشل النسخ الاحتياطي المجدول فجر اليوم على ذاكرة SD Card بسبب عدم توفر مساحة كافية."
    }

    // -------------------------------------------------------------
    // Categorization Operations (CRUD & Pinnings)
    // -------------------------------------------------------------
    fun addCategory(name: String, icon: String, parentId: String? = null) {
        val newCat = Category(
            id = "cat_" + UUID.randomUUID().toString().take(6),
            name = name,
            icon = icon,
            parentId = parentId,
            isPinned = false
        )
        _categories.value = _categories.value + newCat
    }

    fun updateCategory(id: String, name: String, icon: String, parentId: String?) {
        _categories.value = _categories.value.map {
            if (it.id == id) it.copy(name = name, icon = icon, parentId = parentId) else it
        }
    }

    fun deleteCategory(id: String) {
        _categories.value = _categories.value.filter { it.id != id && it.parentId != id }
    }

    fun toggleCategoryPin(id: String) {
        _categories.value = _categories.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    // -------------------------------------------------------------
    // Service Provider Status & Subscription controls
    // -------------------------------------------------------------
    fun updateProviderSubscription(id: String, status: String) {
        // Status values: "active", "suspended", "canceled"
        _providers.value = _providers.value.map {
            if (it.id == id) {
                it.copy(subscriptionStatus = status)
            } else it
        }
    }

    fun toggleProviderPremium(id: String) {
        _providers.value = _providers.value.map {
            if (it.id == id) {
                // If becomes premium, make sure its subscription is active as well
                val currentPremium = it.isPremium
                it.copy(
                    isPremium = !currentPremium,
                    subscriptionStatus = if (!currentPremium) "active" else it.subscriptionStatus
                )
            } else it
        }
    }

    fun toggleProviderPin(id: String) {
        _providers.value = _providers.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun deleteProvider(id: String) {
        _providers.value = _providers.value.filter { it.id != id }
    }

    // -------------------------------------------------------------
    // Supervisors / Admin Credentials Manager
    // -------------------------------------------------------------
    fun addSupervisor(name: String, pinCode: String, editCats: Boolean, delProvs: Boolean, back: Boolean, conf: Boolean) {
        val newSup = Supervisor(
            id = "sup_" + UUID.randomUUID().toString().take(6),
            name = name,
            pinCode = pinCode,
            canEditCategories = editCats,
            canDeleteProviders = delProvs,
            canViewBackup = back,
            canModifyConfigs = conf
        )
        _supervisors.value = _supervisors.value + newSup
    }

    fun updateSupervisor(id: String, name: String, pinCode: String, editCats: Boolean, delProvs: Boolean, back: Boolean, conf: Boolean) {
        _supervisors.value = _supervisors.value.map {
            if (it.id == id) {
                it.copy(
                    name = name,
                    pinCode = pinCode,
                    canEditCategories = editCats,
                    canDeleteProviders = delProvs,
                    canViewBackup = back,
                    canModifyConfigs = conf
                )
            } else it
        }
    }

    fun deleteSupervisor(id: String) {
        _supervisors.value = _supervisors.value.filter { it.id != id }
    }

    // -------------------------------------------------------------
    // Config Updates (Instant Global updates)
    // -------------------------------------------------------------
    fun updateAppConfigs(newConfigs: AppConfigs) {
        _configs.value = newConfigs
    }

    fun toggleGoogleMaps(enabled: Boolean) {
        _configs.value = _configs.value.copy(googleMapsEnabled = enabled)
    }

    fun updateSmartAssistantStyle(scale: Float, alpha: Float, hidden: Boolean, customBase64: String?) {
        _configs.value = _configs.value.copy(
            assistantScale = scale,
            assistantTransparency = alpha,
            assistantHidden = hidden,
            customAssistantIconBase64 = customBase64 ?: _configs.value.customAssistantIconBase64
        )
    }

    fun updateAboutAppStyle(scale: Float, alpha: Float, hidden: Boolean, customBase64: String?) {
        _configs.value = _configs.value.copy(
            aboutAppScale = scale,
            aboutAppTransparency = alpha,
            aboutAppHidden = hidden,
            aboutAppIconBase64 = customBase64 ?: _configs.value.aboutAppIconBase64
        )
    }

    fun updateWelcomeBanner(text: String, size: Float, position: String, imageBase64: String?, show: Boolean) {
        _configs.value = _configs.value.copy(
            welcomeText = text,
            welcomeTextSize = size,
            welcomePosition = position,
            welcomeImageBase64 = imageBase64,
            showWelcomeBanner = show
        )
    }

    fun triggerDataSync() {
        _configs.value = _configs.value.copy()
    }

    // -------------------------------------------------------------
    // Backup & Restoration Utilities
    // -------------------------------------------------------------
    fun triggerBackup(chosenPath: String): Boolean {
        return try {
            val appState = AppStateBackup(
                timestamp = System.currentTimeMillis(),
                categories = _categories.value,
                providers = _providers.value,
                pendingProviders = _pendingProviders.value,
                supervisors = _supervisors.value,
                backups = _backupHistory.value,
                configs = _configs.value
            )
            // Save inside logs
            val isSuccess = Math.random() > 0.15 // 85% success simulation
            val desc = if (isSuccess) {
                "تم النسخ الاحتياطي اليدوي بنجاح وحفظه في مسار [$chosenPath]."
            } else {
                "فشل النسخ الاحتياطي اليدوي في مسار [$chosenPath] لخلل في المصادقة والتحقق من حساب التخزين."
            }

            val newHistory = BackupHistory(
                id = "back_manual_" + UUID.randomUUID().toString().take(6),
                timestamp = System.currentTimeMillis(),
                status = if (isSuccess) "success" else "failed",
                chosenPath = chosenPath,
                description = desc,
                errorMessage = if (isSuccess) null else "AuthorizationException: Account verification failed in $chosenPath."
            )
            
            _backupHistory.value = listOf(newHistory) + _backupHistory.value
            isSuccess
        } catch (e: Exception) {
            false
        }
    }

    fun restoreBackupFromBase64(backupBase64: String): Boolean {
        return try {
            val decodedBytes = Base64.decode(backupBase64, Base64.DEFAULT)
            val jsonText = String(decodedBytes, Charsets.UTF_8)
            val appState = gson.fromJson(jsonText, AppStateBackup::class.java)
            
            _categories.value = appState.categories
            _providers.value = appState.providers
            _pendingProviders.value = appState.pendingProviders
            _supervisors.value = appState.supervisors
            _backupHistory.value = appState.backups
            _configs.value = appState.configs
            true
        } catch (e: Exception) {
            false
        }
    }

    fun generateBackupBase64String(): String {
        return try {
            val appState = AppStateBackup(
                timestamp = System.currentTimeMillis(),
                categories = _categories.value,
                providers = _providers.value,
                pendingProviders = _pendingProviders.value,
                supervisors = _supervisors.value,
                backups = _backupHistory.value,
                configs = _configs.value
            )
            val jsonText = gson.toJson(appState)
            Base64.encodeToString(jsonText.toByteArray(Charsets.UTF_8), Base64.DEFAULT)
        } catch (e: Exception) {
            ""
        }
    }

    fun clearSchedulerAlert() {
        _schedulerFailedAlert.value = null
    }

    fun simulateSchedulerFailure(path: String) {
        _schedulerFailedAlert.value = "فشل النسخ الاحتياطي المبرمج المجدول على مسار [$path] الساعة 04:00 ص."
        _backupHistory.value = listOf(
            BackupHistory(
                id = "back_fail_" + UUID.randomUUID().toString().take(6),
                timestamp = System.currentTimeMillis(),
                status = "failed",
                chosenPath = path,
                description = "فشل النسخ المجدول تلقائياً.",
                errorMessage = "SchedulerException: Device went offline or path is unreachable."
            )
        ) + _backupHistory.value
    }

    // -------------------------------------------------------------
    // Join Requests Approve / Reject
    // -------------------------------------------------------------
    fun approveJoinRequest(requestId: String) {
        val request = _pendingProviders.value.find { it.id == requestId } ?: return
        val newProvider = ServiceProvider(
            id = "prov_" + UUID.randomUUID().toString().take(6),
            name = request.name,
            phone = request.phone,
            categoryId = request.categoryId,
            address = request.address,
            neighborhood = request.neighborhood,
            city = request.city,
            profilePhoto = request.profilePhoto,
            isPinned = false,
            isPremium = false,
            subscriptionStatus = "active"
        )
        _providers.value = _providers.value + newProvider
        _pendingProviders.value = _pendingProviders.value.filter { it.id != requestId }
    }

    fun rejectJoinRequest(requestId: String, reason: String) {
        _pendingProviders.value = _pendingProviders.value.filter { it.id != requestId }
    }

    // -------------------------------------------------------------
    // Ratings & Reviews
    // -------------------------------------------------------------
    fun addReview(providerId: String, author: String, rating: Int, comment: String) {
        val newReview = Review(
            id = "rev_" + UUID.randomUUID().toString().take(6),
            providerId = providerId,
            authorName = author,
            rating = rating,
            comment = comment
        )
        _reviews.value = _reviews.value + newReview
        
        // Recalculate rating
        val provReviews = _reviews.value.filter { it.providerId == providerId }
        val avgRating = provReviews.map { it.rating }.average().toFloat()
        _providers.value = _providers.value.map {
            if (it.id == providerId) it.copy(rating = if (avgRating.isNaN()) 5.0f else avgRating) else it
        }
    }

    // -------------------------------------------------------------
    // Chat Simulation
    // -------------------------------------------------------------
    fun sendChatMessage(senderId: String, senderName: String, recipientId: String, text: String) {
        val msg = ChatMessage(
            id = "chat_" + UUID.randomUUID().toString().take(6),
            senderId = senderId,
            senderName = senderName,
            recipientId = recipientId,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        _chatMessages.value = _chatMessages.value + msg
    }
}
