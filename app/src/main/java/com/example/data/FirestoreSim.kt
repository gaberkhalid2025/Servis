package com.example.data

import android.content.Context
import android.os.Environment
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

/**
 * An advanced, high-fidelity simulated Firestore Real-Time Database.
 * This class emulates addSnapshotListener, offline persistence, and automatic syncing.
 * It is preloaded with beautiful Arabic service data in Yemen districts to ensure immediate visual polish.
 */
object FirestoreSim {
    private const val PREFS_NAME = "firestore_simulation_prefs"

    // Configuration states
    var isPersistenceEnabled = true

    // Real-Time snapshot listeners dictionary
    private val listeners = mutableMapOf<String, MutableList<() -> Unit>>()

    // In-memory data states
    private val _mainCategories = MutableStateFlow<List<MainCategory>>(emptyList())
    val mainCategories: StateFlow<List<MainCategory>> = _mainCategories.asStateFlow()

    private val _subCategories = MutableStateFlow<List<SubCategory>>(emptyList())
    val subCategories: StateFlow<List<SubCategory>> = _subCategories.asStateFlow()

    private val _serviceProviders = MutableStateFlow<List<ServiceProvider>>(emptyList())
    val serviceProviders: StateFlow<List<ServiceProvider>> = _serviceProviders.asStateFlow()

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProviders: StateFlow<List<PendingProvider>> = _pendingProviders.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    private val _reports = MutableStateFlow<List<Report>>(emptyList())
    val reports: StateFlow<List<Report>> = _reports.asStateFlow()

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners.asStateFlow()

    private val _promotedAds = MutableStateFlow<List<PromotedAd>>(emptyList())
    val promotedAds: StateFlow<List<PromotedAd>> = _promotedAds.asStateFlow()

    private val _cities = MutableStateFlow<List<City>>(emptyList())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    private val _fcmChannels = MutableStateFlow<List<FcmChannelState>>(emptyList())
    val fcmChannels: StateFlow<List<FcmChannelState>> = _fcmChannels.asStateFlow()

    private val _appColors = MutableStateFlow(AppColors())
    val appColors: StateFlow<AppColors> = _appColors.asStateFlow()

    private val _appConfigs = MutableStateFlow(AppConfigs())
    val appConfigs: StateFlow<AppConfigs> = _appConfigs.asStateFlow()

    private val _userLoyaltyPoints = MutableStateFlow(0)
    val userLoyaltyPoints: StateFlow<Int> = _userLoyaltyPoints.asStateFlow()

    private val _moderators = MutableStateFlow<List<Moderator>>(emptyList())
    val moderators: StateFlow<List<Moderator>> = _moderators.asStateFlow()

    private val _favorites = MutableStateFlow<Set<String>>(emptySet())
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    private val _contactHistory = MutableStateFlow<List<ContactLog>>(emptyList())
    val contactHistory: StateFlow<List<ContactLog>> = _contactHistory.asStateFlow()

    // Sync state visual notifications
    private val _syncStatus = MutableStateFlow("متزامن بالكامل مع السحابة المركزية")
    val syncStatus: StateFlow<String> = _syncStatus.asStateFlow()

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Checks if populated before, otherwise load beautiful seed data
        val hasData = prefs.getBoolean("has_initialized_data", false)
        if (!hasData) {
            seedInitialData()
            saveToDisk(context)
            prefs.edit().putBoolean("has_initialized_data", true).apply()
        } else {
            loadFromDisk(context)
        }
    }

    private fun seedInitialData() {
        // 1. Initial Main Categories
        _mainCategories.value = listOf(
            MainCategory("cat_1", "صيانة منزلية", "home", 1),
            MainCategory("cat_2", "صحة ورعاية", "medical", 2),
            MainCategory("cat_3", "تعليم وتدريب", "school", 3),
            MainCategory("cat_4", "نقل وخدمات", "car", 4)
        )

        // 2. Initial Sub-categories
        _subCategories.value = listOf(
            SubCategory("sub_1_1", "cat_1", "كهربائي", 1),
            SubCategory("sub_1_2", "cat_1", "سباك", 2),
            SubCategory("sub_1_3", "cat_1", "فني تكييف", 3),
            SubCategory("sub_1_4", "cat_1", "نجار غرف وصالونات", 4),

            SubCategory("sub_2_1", "cat_2", "طبيب منزلي", 1),
            SubCategory("sub_2_2", "cat_2", "ممرض طوارئ", 2),
            SubCategory("sub_2_3", "cat_2", "معالج طبيعي ومساج", 3),

            SubCategory("sub_3_1", "cat_3", "مدرس رياضيات وفيزياء", 1),
            SubCategory("sub_3_2", "cat_3", "مدرس خصوصي أطفال", 2),
            SubCategory("sub_3_3", "cat_3", "مدرب لغات أجنبية", 3),

            SubCategory("sub_4_1", "cat_4", "سائق تكسي وتوصيل مشاوير", 1),
            SubCategory("sub_4_2", "cat_4", "دينا نقل عفش وأثاث", 2),
            SubCategory("sub_4_3", "cat_4", "مهندس ميكانيك متنقل", 3)
        )

        // 3. Preloaded Service Providers
        _serviceProviders.value = listOf(
            ServiceProvider("p_1", "ماهر محمد طاهر ", "777644670", "cat_1", "sub_1_1", "شارع حدة - صنعاء", "مديرية السبعين", "15.3184, 44.1950", "avatar_male_1", null, isPinned = true, isRecommended = true, isSubscribed = true, ratingSum = 25f, ratingCount = 5),
            ServiceProvider("p_2", "أحمد علي الريمي", "771122334", "cat_1", "sub_1_2", "جولة الرويشان - صنعاء", "مديرية السبعين", "15.3214, 44.2014", "avatar_male_2", null, isPinned = false, isRecommended = true, isSubscribed = false, ratingSum = 18f, ratingCount = 4),
            ServiceProvider("p_3", "د. سارة فضل عبدالله", "733445566", "cat_2", "sub_2_1", "شارع التحرير - تعز", "مديرية المظفر", "13.5794, 44.0150", "avatar_female_1", null, isPinned = true, isRecommended = true, isSubscribed = true, ratingSum = 30f, ratingCount = 6),
            ServiceProvider("p_4", "صالح علوي الكلدي", "711556677", "cat_4", "sub_4_2", "منطقة المنصورة - عدن", "حي ريمي", "12.8256, 44.9912", "avatar_male_3", null, isPinned = true, isRecommended = false, isSubscribed = false, ratingSum = 15f, ratingCount = 3),
            ServiceProvider("p_5", "م. وليد كمال الخياط", "770777111", "cat_3", "sub_3_1", "حي الديس - المكلا", "شعبة المهاجرين", "14.5422, 49.1244", "avatar_male_4", null, isPinned = false, isRecommended = false, isSubscribed = true, ratingSum = 10f, ratingCount = 2)
        )

        // 4. Preloaded Pending Providers (Join Requests)
        _pendingProviders.value = listOf(
            PendingProvider("pending_1", "حسين مبارك الحضرمي", "735889900", "cat_1", "sub_1_3", "خور مكسر - عدن", "حي السلام", "12.8122, 45.0211", "avatar_male_5", null),
            PendingProvider("pending_2", "منى سعيد الأشول", "777881122", "cat_3", "sub_3_2", "منطقة الأصبحي - صنعاء", "مديرية السبعين", "15.2894, 44.2150", "avatar_female_2", null)
        )

        // 5. Preloaded Reviews
        _reviews.value = listOf(
            Review("rev_1", "p_1", "محمد يحيى", 5f, "ممتاز جداً وأمين وصيانته سريعة ودقيقة انصح به!"),
            Review("rev_2", "p_1", "ياسر القدسي", 5f, "شغل راقي جدا وملتزم بالمواعيد ورخيص"),
            Review("rev_3", "p_3", "أميرة هائل", 5f, "طبيبة ممتازة جدا تهتم بالمرضى في منازلهم وتعامله راقي"),
            Review("rev_4", "p_5", "وليد الجبلي", 5f, "شرح سلس جدا ومبسط واستفدنا جزيلا منه")
        )

        // 6. Preloaded reports
        _reports.value = listOf(
            Report("rep_1", "p_2", "خالد الوصابي", "تأخر عن الموعد المحدد لأكثر من ساعتين دون إشعار", System.currentTimeMillis())
        )

        // 7. Cities
        _cities.value = listOf(
            City("c_1", "صنعاء", listOf("مديرية السبعين", "مديرية التحرير", "مديرية الصافية", "مديرية شعوب")),
            City("c_2", "عدن", listOf("حي المنصورة", "خور مكسر", "حي المعلا", "كريتر")),
            City("c_3", "تعز", listOf("مديرية المظفر", "مديرية القاهرة", "صالة")),
            City("c_4", "حضرموت", listOf("المكلا - الديس", "الشحر", "سيئون"))
        )

        // 8. Banners
        _banners.value = listOf(
            Banner("b_1", "دليلك الشامل لجميع المهن", "ابحث عن أفضل الفنيين المعتمدين في منطقتك بضغطة زر واحدة", "banner_illustration_1", 30, "", "Medium", "Service"),
            Banner("b_2", "عروض الصيف وتخفيضات المكيفات", "خصومات حتى 25% مع مزودي خدمة صيانة التكييف المميزين", "banner_illustration_2", 15, "", "Large", "External")
        )

        // 9. Promoted ads
        _promotedAds.value = listOf(
            PromotedAd("ad_1", "p_1", "Text", "الكهربائي المعتمد ماهر طاهر", "توصيل وصيانة وتأسيس شبكات المنازل والفلل بأسعار منافسة وجودة عالية وضمانة!", 10, 250.0)
        )

        // 10. FCM Channels
        _fcmChannels.value = listOf(
            FcmChannelState("fcm_1", "طلبات انضمام المهن", true),
            FcmChannelState("fcm_2", "التقارير والبلاغات الجديدة", true),
            FcmChannelState("fcm_3", "التعليقات والتقييمات", true)
        )

        _appColors.value = AppColors("Cosmic Slate", "Bright White")
        _appConfigs.value = AppConfigs()

        // 11. Initial Moderators Seeding
        _moderators.value = listOf(
            Moderator("mod_1", "صالح المشرف الأول", "salih2026", "pass123"),
            Moderator("mod_2", "سارة مشرفة السبعين", "sara2026", "sara777")
        )

        // 12. Initial Favorites Seeding
        _favorites.value = setOf("p_1", "p_3")

        // 13. Initial Contact History Seeding
        _contactHistory.value = listOf(
            ContactLog("cl_1", "p_1", System.currentTimeMillis() - 3600000 * 2, "Call"),
            ContactLog("cl_2", "p_3", System.currentTimeMillis() - 3600000 * 24, "WhatsApp")
        )
    }

    // Snapshot Listening implementation
    fun addSnapshotListener(collection: String, onUpdate: () -> Unit): Subscription {
        val list = listeners.getOrPut(collection) { mutableListOf() }
        list.add(onUpdate)
        return Subscription(collection, onUpdate)
    }

    class Subscription(val collection: String, val callback: () -> Unit) {
        fun remove() {
            listeners[collection]?.remove(callback)
        }
    }

    private fun triggerUpdate(collection: String) {
        _syncStatus.value = "جاري الحفظ والتزامن الفوري..."
        listeners[collection]?.forEach { it() }
        // Simple artificial network simulation delay
        _syncStatus.value = "متزامن بالكامل مع السحابة المركزية (حفظ محلي فوري)"
    }

    // Disk caching / offline simulation
    private fun saveToDisk(context: Context) {
        if (!isPersistenceEnabled) return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()

        try {
            // Save settings statically
            editor.putString("app_name", _appConfigs.value.appName)
            editor.putString("support_email", _appConfigs.value.supportEmail)
            editor.putString("support_phone", _appConfigs.value.supportPhone)
            editor.putString("support_whatsapp", _appConfigs.value.supportWhatsApp)
            editor.putString("footer_promo", _appConfigs.value.footerPromoText)
            editor.putString("welcome_msg", _appConfigs.value.welcomeMessage)
            editor.putString("admin_password", _appConfigs.value.adminPassword)
            editor.putBoolean("maintenance_active", _appConfigs.value.isMaintenanceActive)
            editor.putFloat("assistant_size", _appConfigs.value.smartAssistantSize)
            editor.putBoolean("show_footer", _appConfigs.value.showPromoFooter)
            editor.putFloat("category_icon_size", _appConfigs.value.categoryIconSize)

            // Save Colors
            editor.putString("theme_name", _appColors.value.themeName)
            editor.putString("text_color_name", _appColors.value.textColorName)

            // Points
            editor.putInt("loyalty_points", _userLoyaltyPoints.value)

            // Save User Dashboard customizations
            editor.putBoolean("show_dashboard_favs", _appConfigs.value.showDashboardFavorites)
            editor.putBoolean("show_dashboard_history", _appConfigs.value.showDashboardCallHistory)
            editor.putBoolean("dashboard_favs_first", _appConfigs.value.dashboardFavoritesFirst)
            editor.putString("dashboard_custom_msg", _appConfigs.value.dashboardCustomMessage)

            // Dynamic serialization for lists (simple CSV-like serialization to ensure zero dependency bugs)
            editor.putString("main_categories_serial", serializeMainCategories(_mainCategories.value))
            editor.putString("sub_categories_serial", serializeSubCategories(_subCategories.value))
            editor.putString("service_providers_serial", serializeServiceProviders(_serviceProviders.value))
            editor.putString("pending_providers_serial", serializePendingProviders(_pendingProviders.value))
            editor.putString("reviews_serial", serializeReviews(_reviews.value))
            editor.putString("reports_serial", serializeReports(_reports.value))
            editor.putString("cities_serial", serializeCities(_cities.value))
            editor.putString("banners_serial", serializeBanners(_banners.value))
            editor.putString("promoted_ads_serial", serializePromotedAds(_promotedAds.value))
            editor.putString("fcm_channels_serial", serializeFcmChannels(_fcmChannels.value))
            editor.putString("favorites_serial", serializeFavorites(_favorites.value))
            editor.putString("contact_history_serial", serializeContactHistory(_contactHistory.value))
            editor.putString("moderators_serial", serializeModerators(_moderators.value))

            editor.apply()
        } catch (e: Exception) {
            Log.e("FirestoreSim", "Error saving data", e)
        }
    }

    private fun loadFromDisk(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        try {
            _appConfigs.value = AppConfigs(
                appName = prefs.getString("app_name", "دليلك لكل الخدمات") ?: "دليلك لكل الخدمات",
                supportEmail = prefs.getString("support_email", "support@services-guide.example") ?: "support@services-guide.example",
                supportPhone = prefs.getString("support_phone", "777644670") ?: "777644670",
                supportWhatsApp = prefs.getString("support_whatsapp", "777644670") ?: "777644670",
                footerPromoText = prefs.getString("footer_promo", "MAW 777644670") ?: "MAW 777644670",
                welcomeMessage = prefs.getString("welcome_msg", "مرحباً بك في دليلك لكل الخدمات الفوري!") ?: "مرحباً بك في دليلك لكل الخدمات الفوري!",
                adminPassword = prefs.getString("admin_password", "maher736462") ?: "maher736462",
                isMaintenanceActive = prefs.getBoolean("maintenance_active", false),
                smartAssistantSize = prefs.getFloat("assistant_size", 56f),
                showPromoFooter = prefs.getBoolean("show_footer", true),
                showDashboardFavorites = prefs.getBoolean("show_dashboard_favs", true),
                showDashboardCallHistory = prefs.getBoolean("show_dashboard_history", true),
                dashboardFavoritesFirst = prefs.getBoolean("dashboard_favs_first", true),
                dashboardCustomMessage = prefs.getString("dashboard_custom_msg", "مرحباً بك في لوحة تحكمك المفضلة وسجل التواصل!") ?: "مرحباً بك في لوحة تحكمك المفضلة وسجل التواصل!",
                categoryIconSize = prefs.getFloat("category_icon_size", 32f)
            )

            _appColors.value = AppColors(
                themeName = prefs.getString("theme_name", "Cosmic Slate") ?: "Cosmic Slate",
                textColorName = prefs.getString("text_color_name", "Bright White") ?: "Bright White"
            )

            _userLoyaltyPoints.value = prefs.getInt("loyalty_points", 0)

            // Load arrays
            prefs.getString("main_categories_serial", null)?.let {
                _mainCategories.value = deserializeMainCategories(it)
            }
            prefs.getString("sub_categories_serial", null)?.let {
                _subCategories.value = deserializeSubCategories(it)
            }
            prefs.getString("service_providers_serial", null)?.let {
                _serviceProviders.value = deserializeServiceProviders(it)
            }
            prefs.getString("pending_providers_serial", null)?.let {
                _pendingProviders.value = deserializePendingProviders(it)
            }
            prefs.getString("reviews_serial", null)?.let {
                _reviews.value = deserializeReviews(it)
            }
            prefs.getString("reports_serial", null)?.let {
                _reports.value = deserializeReports(it)
            }
            prefs.getString("cities_serial", null)?.let {
                _cities.value = deserializeCities(it)
            }
            prefs.getString("banners_serial", null)?.let {
                _banners.value = deserializeBanners(it)
            }
            prefs.getString("promoted_ads_serial", null)?.let {
                _promotedAds.value = deserializePromotedAds(it)
            }
            prefs.getString("fcm_channels_serial", null)?.let {
                _fcmChannels.value = deserializeFcmChannels(it)
            }
            prefs.getString("favorites_serial", null)?.let {
                _favorites.value = deserializeFavorites(it)
            }
            prefs.getString("contact_history_serial", null)?.let {
                _contactHistory.value = deserializeContactHistory(it)
            }
            prefs.getString("moderators_serial", null)?.let {
                _moderators.value = deserializeModerators(it)
            }
        } catch (e: Exception) {
            Log.e("FirestoreSim", "Error loading data from SharedPreferences", e)
            seedInitialData()
        }
    }

    // ==========================================
    // MUTATION FUNCTIONS WITH INSTANT SYNC
    // ==========================================

    fun updateColors(context: Context, theme: String, textColor: String) {
        _appColors.value = AppColors(theme, textColor)
        saveToDisk(context)
        triggerUpdate("admins")
    }

    fun updateConfigs(context: Context, configs: AppConfigs) {
        _appConfigs.value = configs
        saveToDisk(context)
        triggerUpdate("admins")
    }

    fun toggleFavorite(context: Context, providerId: String) {
        val current = _favorites.value.toMutableSet()
        if (current.contains(providerId)) {
            current.remove(providerId)
        } else {
            current.add(providerId)
        }
        _favorites.value = current
        saveToDisk(context)
        triggerUpdate("favorites")
    }

    fun addContactLog(context: Context, providerId: String, mode: String) {
        val current = _contactHistory.value.toMutableList()
        current.add(0, ContactLog("cl_${System.currentTimeMillis()}", providerId, System.currentTimeMillis(), mode))
        _contactHistory.value = current.take(50)
        saveToDisk(context)
        triggerUpdate("contact_history")
    }

    fun clearContactHistory(context: Context) {
        _contactHistory.value = emptyList()
        saveToDisk(context)
        triggerUpdate("contact_history")
    }

    fun saveModerator(context: Context, mod: Moderator) {
        val current = _moderators.value.toMutableList()
        val index = current.indexOfFirst { it.id == mod.id }
        if (index >= 0) {
            current[index] = mod
        } else {
            current.add(mod.copy(id = "mod_${System.currentTimeMillis()}"))
        }
        _moderators.value = current
        saveToDisk(context)
        triggerUpdate("moderators")
    }

    fun deleteModerator(context: Context, modId: String) {
        _moderators.value = _moderators.value.filter { it.id != modId }
        saveToDisk(context)
        triggerUpdate("moderators")
    }

    fun addLoyaltyPoints(context: Context, amt: Int) {
        _userLoyaltyPoints.value += amt
        saveToDisk(context)
    }

    fun toggleFcmChannel(context: Context, id: String, enable: Boolean) {
        _fcmChannels.value = _fcmChannels.value.map {
            if (it.id == id) it.copy(isEnabled = enable) else it
        }
        saveToDisk(context)
        triggerUpdate("admins")
    }

    // Main Categories Mutations
    fun saveMainCategory(context: Context, cat: MainCategory) {
        val current = _mainCategories.value.toMutableList()
        val index = current.indexOfFirst { it.id == cat.id }
        if (index >= 0) {
            current[index] = cat
        } else {
            current.add(cat.copy(id = "cat_${System.currentTimeMillis()}"))
        }
        _mainCategories.value = current
        saveToDisk(context)
        triggerUpdate("categories")
    }

    fun deleteMainCategory(context: Context, catId: String) {
        _mainCategories.value = _mainCategories.value.filter { it.id != catId }
        _subCategories.value = _subCategories.value.filter { it.parentId != catId }
        saveToDisk(context)
        triggerUpdate("categories")
    }

    // Sub-Categories Mutations
    fun saveSubCategory(context: Context, sub: SubCategory) {
        val current = _subCategories.value.toMutableList()
        val index = current.indexOfFirst { it.id == sub.id }
        if (index >= 0) {
            current[index] = sub
        } else {
            current.add(sub.copy(id = "sub_${System.currentTimeMillis()}"))
        }
        _subCategories.value = current
        saveToDisk(context)
        triggerUpdate("categories")
    }

    fun deleteSubCategory(context: Context, subId: String) {
        _subCategories.value = _subCategories.value.filter { it.id != subId }
        saveToDisk(context)
        triggerUpdate("categories")
    }

    // Manage Service Providers
    fun addProviderInstantly(context: Context, p: ServiceProvider) {
        val updated = _serviceProviders.value.toMutableList()
        updated.add(p.copy(id = "p_${System.currentTimeMillis()}"))
        _serviceProviders.value = updated
        saveToDisk(context)
        triggerUpdate("service_providers")
    }

    fun updateProvider(context: Context, p: ServiceProvider) {
        _serviceProviders.value = _serviceProviders.value.map {
            if (it.id == p.id) p else it
        }
        saveToDisk(context)
        triggerUpdate("service_providers")
    }

    fun deleteProvider(context: Context, pId: String) {
        _serviceProviders.value = _serviceProviders.value.filter { it.id != pId }
        saveToDisk(context)
        triggerUpdate("service_providers")
    }

    // Pending Providers (Enrollment Requests)
    fun addPendingProvider(context: Context, req: PendingProvider) {
        val current = _pendingProviders.value.toMutableList()
        current.add(req.copy(id = "pending_${System.currentTimeMillis()}"))
        _pendingProviders.value = current
        saveToDisk(context)
        triggerUpdate("pending_providers")
    }

    fun updatePendingStatus(context: Context, reqId: String, status: String, reason: String? = null) {
        val requests = _pendingProviders.value
        val request = requests.find { it.id == reqId } ?: return

        if (status == "approved") {
            // Transfer to service_providers
            val newProvider = ServiceProvider(
                id = "p_${System.currentTimeMillis()}",
                name = request.name,
                phone = request.phone,
                mainCategoryId = request.mainCategoryId,
                subCategoryId = request.subCategoryId,
                address = request.address,
                district = request.district,
                gpsCoordinates = request.gpsCoordinates,
                avatarUrl = request.avatarUrl,
                idCardUrl = request.idCardUrl
            )
            val updatedProviders = _serviceProviders.value.toMutableList()
            updatedProviders.add(newProvider)
            _serviceProviders.value = updatedProviders
            triggerUpdate("service_providers")

            // Remove from pending
            _pendingProviders.value = requests.filter { it.id != reqId }
        } else if (status == "rejected") {
            // Mark status as rejected
            _pendingProviders.value = requests.map {
                if (it.id == reqId) it.copy(status = "rejected", rejectionReason = reason) else it
            }
        }
        saveToDisk(context)
        triggerUpdate("pending_providers")
    }

    fun deletePendingRequest(context: Context, reqId: String) {
        _pendingProviders.value = _pendingProviders.value.filter { it.id != reqId }
        saveToDisk(context)
        triggerUpdate("pending_providers")
    }

    // Cities Controls
    fun addCity(context: Context, name: String, districts: List<String>) {
        val current = _cities.value.toMutableList()
        current.add(City("c_${System.currentTimeMillis()}", name, districts))
        _cities.value = current
        saveToDisk(context)
        triggerUpdate("admins")
    }

    fun deleteCity(context: Context, cityId: String) {
        _cities.value = _cities.value.filter { it.id != cityId }
        saveToDisk(context)
        triggerUpdate("admins")
    }

    // Reviews Mutation
    fun addReview(context: Context, rev: Review) {
        val current = _reviews.value.toMutableList()
        current.add(rev.copy(id = "rev_${System.currentTimeMillis()}"))
        _reviews.value = current

        // Update provider ranking cache
        val sum = current.filter { it.providerId == rev.providerId }.sumOf { it.rating.toDouble() }.toFloat()
        val count = current.filter { it.providerId == rev.providerId }.size

        _serviceProviders.value = _serviceProviders.value.map {
            if (it.id == rev.providerId) {
                it.copy(ratingSum = sum, ratingCount = count)
            } else it
        }

        saveToDisk(context)
        triggerUpdate("reviews")
        triggerUpdate("service_providers")
    }

    // Reports Mutation
    fun addReport(context: Context, rep: Report) {
        val current = _reports.value.toMutableList()
        current.add(rep.copy(id = "rep_${System.currentTimeMillis()}"))
        _reports.value = current
        saveToDisk(context)
        triggerUpdate("reports")
    }

    fun deleteReport(context: Context, reportId: String) {
        _reports.value = _reports.value.filter { it.id != reportId }
        saveToDisk(context)
        triggerUpdate("reports")
    }

    // Banners Mutations
    fun saveBanner(context: Context, ban: Banner) {
        val current = _banners.value.toMutableList()
        val index = current.indexOfFirst { it.id == ban.id }
        if (index >= 0) {
            current[index] = ban
        } else {
            current.add(ban.copy(id = "b_${System.currentTimeMillis()}"))
        }
        _banners.value = current
        saveToDisk(context)
        triggerUpdate("banners")
    }

    fun deleteBanner(context: Context, bannerId: String) {
        _banners.value = _banners.value.filter { it.id != bannerId }
        saveToDisk(context)
        triggerUpdate("banners")
    }

    // Promoted Ads Mutations
    fun savePromotedAd(context: Context, ad: PromotedAd) {
        val current = _promotedAds.value.toMutableList()
        val index = current.indexOfFirst { it.id == ad.id }
        if (index >= 0) {
            current[index] = ad
        } else {
            current.add(ad.copy(id = "ad_${System.currentTimeMillis()}"))
        }
        _promotedAds.value = current
        saveToDisk(context)
        triggerUpdate("banners")
    }

    fun deletePromotedAd(context: Context, adId: String) {
        _promotedAds.value = _promotedAds.value.filter { it.id != adId }
        saveToDisk(context)
        triggerUpdate("banners")
    }

    // ==========================================
    // BACKUP AND RESTORE UTILITIES
    // ==========================================

    fun generateBackupString(): String {
        val builder = java.lang.StringBuilder()
        builder.append("=== SYSTEM BACKUP - ${_appConfigs.value.appName} ===\n")
        builder.append("THEME|${_appColors.value.themeName}|${_appColors.value.textColorName}\n")
        builder.append("CONFIG|${_appConfigs.value.appName}|${_appConfigs.value.supportEmail}|${_appConfigs.value.supportPhone}|${_appConfigs.value.supportWhatsApp}|${_appConfigs.value.footerPromoText}|${_appConfigs.value.welcomeMessage}|${_appConfigs.value.adminPassword}|${_appConfigs.value.isMaintenanceActive}\n")
        
        _mainCategories.value.forEach {
            builder.append("MAIN_CAT|${it.id}|${it.name}|${it.iconCode}|${it.order}\n")
        }
        _subCategories.value.forEach {
            builder.append("SUB_CAT|${it.id}|${it.parentId}|${it.name}|${it.order}\n")
        }
        _serviceProviders.value.forEach {
            builder.append("PROVIDER|${it.id}|${it.name}|${it.phone}|${it.mainCategoryId}|${it.subCategoryId}|${it.address}|${it.district}|${it.gpsCoordinates ?: ""}|${it.avatarUrl}|${it.isPinned}|${it.isRecommended}|${it.isSubscribed}|${it.ratingSum}|${it.ratingCount}\n")
        }
        _reviews.value.forEach {
            builder.append("REVIEW|${it.id}|${it.providerId}|${it.userName}|${it.rating}|${it.comment}\n")
        }
        _reports.value.forEach {
            builder.append("REPORT|${it.id}|${it.providerId}|${it.userName}|${it.comment}\n")
        }
        _cities.value.forEach {
            builder.append("CITY|${it.id}|${it.name}|${it.districts.joinToString(",")}\n")
        }
        return builder.toString()
    }

    fun restoreFromBackupString(context: Context, backupData: String): Boolean {
        if (!backupData.startsWith("=== SYSTEM BACKUP")) return false
        try {
            val lines = backupData.split("\n")
            val newMainCats = mutableListOf<MainCategory>()
            val newSubCats = mutableListOf<SubCategory>()
            val newProviders = mutableListOf<ServiceProvider>()
            val newReviews = mutableListOf<Review>()
            val newReports = mutableListOf<Report>()
            val newCities = mutableListOf<City>()

            for (line in lines) {
                if (line.isBlank() || line.startsWith("===")) continue
                val parts = line.split("|")
                if (parts.size < 2) continue
                when (parts[0]) {
                    "THEME" -> {
                        _appColors.value = AppColors(parts[1], parts.getOrNull(2) ?: "Bright White")
                    }
                    "CONFIG" -> {
                        if (parts.size >= 9) {
                            _appConfigs.value = AppConfigs(
                                appName = parts[1],
                                supportEmail = parts[2],
                                supportPhone = parts[3],
                                supportWhatsApp = parts[4],
                                footerPromoText = parts[5],
                                welcomeMessage = parts[6],
                                adminPassword = parts[7],
                                isMaintenanceActive = parts[8].toBoolean()
                            )
                        }
                    }
                    "MAIN_CAT" -> {
                        if (parts.size >= 5) {
                            newMainCats.add(MainCategory(parts[1], parts[2], parts[3], parts[4].toIntOrNull() ?: 1))
                        }
                    }
                    "SUB_CAT" -> {
                        if (parts.size >= 5) {
                            newSubCats.add(SubCategory(parts[1], parts[2], parts[3], parts[4].toIntOrNull() ?: 1))
                        }
                    }
                    "PROVIDER" -> {
                        if (parts.size >= 15) {
                            newProviders.add(ServiceProvider(
                                id = parts[1], name = parts[2], phone = parts[3],
                                mainCategoryId = parts[4], subCategoryId = parts[5],
                                address = parts[6], district = parts[7],
                                gpsCoordinates = parts[8].ifBlank { null },
                                avatarUrl = parts[9],
                                isPinned = parts[10].toBoolean(),
                                isRecommended = parts[11].toBoolean(),
                                isSubscribed = parts[12].toBoolean(),
                                ratingSum = parts[13].toFloatOrNull() ?: 0f,
                                ratingCount = parts[14].toIntOrNull() ?: 0
                            ))
                        }
                    }
                    "REVIEW" -> {
                        if (parts.size >= 6) {
                            newReviews.add(Review(parts[1], parts[2], parts[3], parts[4].toFloatOrNull() ?: 5f, parts[5]))
                        }
                    }
                    "REPORT" -> {
                        if (parts.size >= 5) {
                            newReports.add(Report(parts[1], parts[2], parts[3], parts[4]))
                        }
                    }
                    "CITY" -> {
                        if (parts.size >= 4) {
                            val districts = parts[3].split(",").filter { it.isNotBlank() }
                            newCities.add(City(parts[1], parts[2], districts))
                        }
                    }
                }
            }

            if (newMainCats.isNotEmpty()) _mainCategories.value = newMainCats
            if (newSubCats.isNotEmpty()) _subCategories.value = newSubCats
            if (newProviders.isNotEmpty()) _serviceProviders.value = newProviders
            _reviews.value = newReviews
            _reports.value = newReports
            if (newCities.isNotEmpty()) _cities.value = newCities

            saveToDisk(context)
            triggerUpdate("categories")
            triggerUpdate("service_providers")
            triggerUpdate("admins")
            triggerUpdate("reports")
            triggerUpdate("reviews")
            return true
        } catch (e: Exception) {
            Log.e("FirestoreSim", "Backup restoration error", e)
            return false
        }
    }

    fun backupToSDCard(context: Context): String? {
        return try {
            val backupText = generateBackupString()
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            val file = File(downloadsDir, "dalil_services_backup.txt")
            file.writeText(backupText)
            file.absolutePath
        } catch (e: Exception) {
            Log.e("FirestoreSim", "SD Card backup failed", e)
            null
        }
    }

    fun restoreFromSDCard(context: Context): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, "dalil_services_backup.txt")
            if (file.exists()) {
                restoreFromBackupString(context, file.readText())
            } else false
        } catch (e: Exception) {
            Log.e("FirestoreSim", "SD Card restore failed", e)
            false
        }
    }

    // ==========================================
    // MANUAL ARRAY SERIALIZERS
    // ==========================================

    private fun serializeMainCategories(list: List<MainCategory>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.iconCode},${it.order}" }
    }
    private fun deserializeMainCategories(s: String): List<MainCategory> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 4) MainCategory(p[0], p[1], p[2], p[3].toIntOrNull() ?: 1) else null
        }
    }

    private fun serializeSubCategories(list: List<SubCategory>): String {
        return list.joinToString(";") { "${it.id},${it.parentId},${it.name},${it.order}" }
    }
    private fun deserializeSubCategories(s: String): List<SubCategory> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 4) SubCategory(p[0], p[1], p[2], p[3].toIntOrNull() ?: 1) else null
        }
    }

    private fun serializeServiceProviders(list: List<ServiceProvider>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.phone},${it.mainCategoryId},${it.subCategoryId},${it.address},${it.district},${it.gpsCoordinates ?: ""},${it.avatarUrl},${it.idCardUrl ?: ""},${it.isPinned},${it.isRecommended},${it.isSubscribed},${it.ratingSum},${it.ratingCount}" }
    }
    private fun deserializeServiceProviders(s: String): List<ServiceProvider> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 15) {
                ServiceProvider(
                    id = p[0],
                    name = p[1],
                    phone = p[2],
                    mainCategoryId = p[3],
                    subCategoryId = p[4],
                    address = p[5],
                    district = p[6],
                    gpsCoordinates = p[7].ifBlank { null },
                    avatarUrl = p[8],
                    idCardUrl = p[9].ifBlank { null },
                    isPinned = p[10].toBoolean(),
                    isRecommended = p[11].toBoolean(),
                    isSubscribed = p[12].toBoolean(),
                    ratingSum = p[13].toFloatOrNull() ?: 0f,
                    ratingCount = p[14].toIntOrNull() ?: 0
                )
            } else null
        }
    }

    private fun serializePendingProviders(list: List<PendingProvider>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.phone},${it.mainCategoryId},${it.subCategoryId},${it.address},${it.district},${it.gpsCoordinates ?: ""},${it.avatarUrl},${it.idCardUrl ?: ""},${it.status},${it.rejectionReason ?: ""},${it.timestamp}" }
    }
    private fun deserializePendingProviders(s: String): List<PendingProvider> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 13) {
                PendingProvider(
                    id = p[0],
                    name = p[1],
                    phone = p[2],
                    mainCategoryId = p[3],
                    subCategoryId = p[4],
                    address = p[5],
                    district = p[6],
                    gpsCoordinates = p[7].ifBlank { null },
                    avatarUrl = p[8],
                    idCardUrl = p[9].ifBlank { null },
                    status = p[10],
                    rejectionReason = p[11].ifBlank { null },
                    timestamp = p[12].toLongOrNull() ?: System.currentTimeMillis()
                )
            } else null
        }
    }

    private fun serializeReviews(list: List<Review>): String {
        return list.joinToString(";") { "${it.id},${it.providerId},${it.userName},${it.rating},${it.comment},${it.timestamp}" }
    }
    private fun deserializeReviews(s: String): List<Review> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 6) Review(p[0], p[1], p[2], p[3].toFloatOrNull() ?: 5f, p[4], p[5].toLongOrNull() ?: System.currentTimeMillis()) else null
        }
    }

    private fun serializeReports(list: List<Report>): String {
        return list.joinToString(";") { "${it.id},${it.providerId},${it.userName},${it.comment},${it.timestamp}" }
    }
    private fun deserializeReports(s: String): List<Report> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 5) Report(p[0], p[1], p[2], p[3], p[4].toLongOrNull() ?: System.currentTimeMillis()) else null
        }
    }

    private fun serializeCities(list: List<City>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.districts.joinToString("|")}" }
    }
    private fun deserializeCities(s: String): List<City> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 3) City(p[0], p[1], p[2].split("|").filter { d -> d.isNotBlank() }) else null
        }
    }

    private fun serializeBanners(list: List<Banner>): String {
        return list.joinToString(";") { "${it.id},${it.title},${it.description},${it.imageUrl},${it.durationDays},${it.redirectUrl},${it.bannerSize},${it.bannerType},${it.timestamp}" }
    }
    private fun deserializeBanners(s: String): List<Banner> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 9) Banner(p[0], p[1], p[2], p[3], p[4].toIntOrNull() ?: 7, p[5], p[6], p[7], p[8].toLongOrNull() ?: System.currentTimeMillis()) else null
        }
    }

    private fun serializePromotedAds(list: List<PromotedAd>): String {
        return list.joinToString(";") { "${it.id},${it.providerId ?: ""},${it.type},${it.title},${it.content},${it.durationDays},${it.budget},${it.timestamp}" }
    }
    private fun deserializePromotedAds(s: String): List<PromotedAd> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 8) PromotedAd(p[0], p[1].ifBlank { null }, p[2], p[3], p[4], p[5].toIntOrNull() ?: 5, p[6].toDoubleOrNull() ?: 100.0, p[7].toLongOrNull() ?: System.currentTimeMillis()) else null
        }
    }

    private fun serializeFcmChannels(list: List<FcmChannelState>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.isEnabled}" }
    }
    private fun deserializeFcmChannels(s: String): List<FcmChannelState> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 3) FcmChannelState(p[0], p[1], p[2].toBoolean()) else null
        }
    }

    private val _fcmChannelsStateSerialList = mutableListOf<String>() // dummy holder

    private fun serializeModerators(list: List<Moderator>): String {
        return list.joinToString(";") { "${it.id},${it.name},${it.username},${it.password}" }
    }
    private fun deserializeModerators(s: String): List<Moderator> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 4) Moderator(p[0], p[1], p[2], p[3]) else null
        }
    }

    private fun serializeFavorites(set: Set<String>): String {
        return set.joinToString(",")
    }
    private fun deserializeFavorites(s: String): Set<String> {
        if (s.isBlank()) return emptySet()
        return s.split(",").filter { it.isNotBlank() }.toSet()
    }

    private fun serializeContactHistory(list: List<ContactLog>): String {
        return list.joinToString(";") { "${it.id},${it.providerId},${it.timestamp},${it.mode}" }
    }
    private fun deserializeContactHistory(s: String): List<ContactLog> {
        if (s.isBlank()) return emptyList()
        return s.split(";").mapNotNull {
            val p = it.split(",")
            if (p.size >= 4) ContactLog(p[0], p[1], p[2].toLongOrNull() ?: System.currentTimeMillis(), p[3]) else null
        }
    }
}
