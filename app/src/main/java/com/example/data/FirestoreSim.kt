package com.example.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File

object FirestoreSim {
    // Shared state variables mimicking authentic Firestore synchronization flows.
    private var firestore: FirebaseFirestore? = null

    // Collections
    private var categoriesListener: ListenerRegistration? = null
    private var providersListener: ListenerRegistration? = null
    private var pendingListener: ListenerRegistration? = null
    private var reviewsListener: ListenerRegistration? = null
    private var reportsListener: ListenerRegistration? = null
    private var configsListener: ListenerRegistration? = null
    private var bannersListener: ListenerRegistration? = null

    // Fallback reactive collections
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers: StateFlow<List<Provider>> = _providers

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProviders: StateFlow<List<PendingProvider>> = _pendingProviders

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews: StateFlow<List<Review>> = _reviews

    private val _reports = MutableStateFlow<List<ReportNotification>>(emptyList())
    val reports: StateFlow<List<ReportNotification>> = _reports

    private val _configs = MutableStateFlow(AppConfigs())
    val configs: StateFlow<AppConfigs> = _configs

    private val _banners = MutableStateFlow<List<Banner>>(emptyList())
    val banners: StateFlow<List<Banner>> = _banners

    private val _whitelistedDevices = MutableStateFlow<List<String>>(listOf("Device-Yem-Emulator", "Device-Maher-736"))
    val whitelistedDevices: StateFlow<List<String>> = _whitelistedDevices

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // Loyalty points state
    private val _userPoints = MutableStateFlow(125)
    val userPoints: StateFlow<Int> = _userPoints

    // Current Session Identity
    var currentUserId = "guest"
    var currentUserName = "زائر كريم"
    var currentUserPhone = ""
    var isBackdoorOwnerLoggedIn = false

    // Multi-Language Tracker
    val currentLang = MutableStateFlow("ar") // "ar" or "en"

    // Initializer
    fun initialize(context: Context) {
        // 1. Setup Firebase with Offline Persistence if initialized
        try {
            firestore = FirebaseFirestore.getInstance()
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
            firestore?.firestoreSettings = settings
        } catch (e: Exception) {
            firestore = null
        }

        // 2. Prepopulate standard defaults in the state flows so the app is instantly rich
        loadDefaultMockData()

        // 3. Connect real snapshot listeners
        startFirestoreSync()
    }

    private fun loadDefaultMockData() {
        _categories.value = listOf(
            Category("electricity", "صيانة منزلية وكهرباء ⚡", "Home Electricity ⚡", "⚡", 1),
            Category("plumbing", "سباكة وصحي 🚰", "Plumbing & Sanitary 🚰", "🚰", 2),
            Category("tech", "برمجة وهواتف وحاسوب 📱", "Programming & Phones 📱", "📱", 3),
            Category("carpentry", "نجارة وديكور وأخشاب 🪵", "Carpentry & Decor 🪵", "🪵", 4),
            Category("mechanics", "صيانة ميكانيك وسيارات 🚗", "Car Mechanics 🚗", "🚗", 5)
        )

        _providers.value = listOf(
            Provider(
                id = "p1",
                name = "المهندس فؤاد الزبيدي",
                title = "أخصائي تمديدات ومنظومات طاقة شمسية في صنعاء ⚡",
                titleEn = "Solar System Specialist ⚡",
                phone = "777644670",
                mainCategoryId = "electricity",
                subCategoryId = "صيانة منظومة شمسية",
                city = "صنعاء",
                region = "الأصبحي - حدة",
                bio = "خبرة تفوق الـ 10 سنوات في صيانة وتركيب المنظومات الشمسية والبطاريات وحل مشاكل تمديدات المنازل والمحال التجارية بدقة واحترافية عالية.",
                bioEn = "Solar networks specialist in Sanaa",
                lat = 15.348,
                lng = 44.204,
                isVerified = true,
                isPinned = true,
                isRecommended = true,
                reviewCount = 3,
                averageRating = 4.8,
                points = 250
            ),
            Provider(
                id = "p2",
                name = "الفني محمد الصبري",
                title = "مهندس تصريف وسباكة وشبكات مياه 🚰",
                titleEn = "Plumbing Expert",
                phone = "733444455",
                mainCategoryId = "plumbing",
                subCategoryId = "صيانة حمامات",
                city = "صنعاء",
                region = "باب اليمن - صنعاء القديمة",
                bio = "متخصص في تأسيس خطوط التهوية المانعة للروائح وتركيب وصيانة المضخات والسمن وخطوط الصرف الداخلي والعمومي بأدوات حديثة.",
                lat = 15.352,
                lng = 44.215,
                isVerified = true,
                isPinned = false,
                isRecommended = true,
                reviewCount = 1,
                averageRating = 4.0,
                points = 90
            ),
            Provider(
                id = "p3",
                name = "المبرمج صدام الورقي",
                title = "مطور تطبيقات ذكية وحل مشاكل برمجية 📱",
                titleEn = "Mobile Apps Developer 📱",
                phone = "771100222",
                mainCategoryId = "tech",
                subCategoryId = "تطبيقات الهواتف",
                city = "عدن",
                region = "شباب المنصورة",
                bio = "طورت الكثير من التطبيقات للهواتف الأندرويد والآيفون، وجاهز لحل وتخصيص البوابات والمتاجر باليمن.",
                lat = 12.802,
                lng = 44.988,
                isVerified = false,
                isPinned = false,
                isRecommended = false,
                reviewCount = 1,
                averageRating = 5.0,
                points = 45
            )
        )

        _pendingProviders.value = listOf(
            PendingProvider(
                id = "pp1",
                name = "سامي الحيمي",
                phone = "775556667",
                mainCategoryId = "mechanics",
                city = "تعز",
                region = "شارع جمال",
                gpsCoordinates = "13.579,44.015",
                submitTime = System.currentTimeMillis() - 86400000,
                status = "pending"
            )
        )

        _reviews.value = listOf(
            Review("r1", "p1", "أبو بكر الحمادي", 5, "عمل قمة في الروعة، أنجز المنظومة في ساعات وبسعر مناسب جداً تمنياتي له بالتوفيق.", System.currentTimeMillis() - 5000000),
            Review("r2", "p1", "عارف الريمي", 5, "مهندس أمين وخلوق، يشتغل بذمة وضمير.", System.currentTimeMillis() - 3000000),
            Review("r3", "p2", "خالد يحيى", 4, "صالح لي مواسير المطبخ بسرعة، مشكور جداً.", System.currentTimeMillis() - 10000000)
        )

        _reports.value = listOf(
            ReportNotification("rep1", "p2", "محمد الصبري", "عبدالرحمن الميثالي", "تأخر عن الموعد ساعتين ولم يرد على الاتصال بسرعة.", System.currentTimeMillis() - 72000000)
        )

        _banners.value = listOf(
            Banner("b1", "عروض الصيف الفنية: احصل على خصم 15% على تمديدات الكهرباء 💡!", "", "Medium", 8, "electricity", "https://maher736.com", System.currentTimeMillis()),
            Banner("b2", "دليل الكوادر والمهن اليمنية: شريكك الأول لتوفير الوقت والأمان 🇾🇪", "", "Small", 10, "all", "", System.currentTimeMillis())
        )

        _chatRooms.value = listOf(
            ChatRoom("cr1", "u_test", "عبدالله الصنعاني", "p1", "المهندس فؤاد الزبيدي", "مرحباً بك مهندس فؤاد، متى يمكنك الحضور؟", System.currentTimeMillis())
        )

        _messages.value = listOf(
            ChatMessage("m1", "cr1", "u_test", "عبدالله الصنعاني", "السلام عليكم مهندس، أريد صيانة بطارية طاقة شمسية قريبة.", System.currentTimeMillis() - 60000),
            ChatMessage("m2", "cr1", "p1", "المهندس فؤاد الزبيدي", "مرحباً يا غالي، أهلاً بك. أنا متاح الآن تفضل.", System.currentTimeMillis() - 40000),
            ChatMessage("m3", "cr1", "u_test", "عبدالله الصنعاني", "مرحباً بك مهندس فؤاد، متى يمكنك الحضور؟", System.currentTimeMillis() - 20000)
        )
    }

    private fun startFirestoreSync() {
        val f = firestore ?: return

        // 1. Categories sync
        categoriesListener = f.collection("categories")
            .orderBy("sortOrder")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toCategory() }
                    if (list.isNotEmpty()) _categories.value = list
                }
            }

        // 2. Providers sync
        providersListener = f.collection("service_providers")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toProvider() }
                    if (list.isNotEmpty()) _providers.value = list
                }
            }

        // 3. Pending Providers sync
        pendingListener = f.collection("pending_providers")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toPendingProvider() }
                    if (list.isNotEmpty()) _pendingProviders.value = list
                }
            }

        // 4. Reviews sync
        reviewsListener = f.collection("reviews")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toReview() }
                    if (list.isNotEmpty()) _reviews.value = list
                }
            }

        // 5. Reports sync
        reportsListener = f.collection("reports")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toReportNotification() }
                    if (list.isNotEmpty()) _reports.value = list
                }
            }

        // 6. Configs sync
        configsListener = f.collection("app_configs")
            .document("default_config")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null && snapshot.exists()) {
                    val conf = snapshot.toAppConfigs()
                    if (conf != null) _configs.value = conf
                }
            }

        // 7. Banners sync
        bannersListener = f.collection("banners")
            .addSnapshotListener { snapshot, error ->
                if (error == null && snapshot != null) {
                    val list = snapshot.documents.mapNotNull { it.toBanner() }
                    if (list.isNotEmpty()) _banners.value = list
                }
            }
    }

    // Helper Extporters to map firebase Documents
    private fun DocumentSnapshot.toCategory(): Category? = try {
        Category(
            id = getString("id") ?: id,
            name = getString("name") ?: "",
            nameEn = getString("nameEn") ?: "",
            icon = getString("icon") ?: "",
            sortOrder = getLong("sortOrder")?.toInt() ?: 0,
            isMain = getBoolean("isMain") ?: true,
            parentId = getString("parentId")
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toProvider(): Provider? = try {
        Provider(
            id = getString("id") ?: id,
            name = getString("name") ?: "",
            title = getString("title") ?: "",
            titleEn = getString("titleEn") ?: "",
            phone = getString("phone") ?: "",
            mainCategoryId = getString("mainCategoryId") ?: "",
            subCategoryId = getString("subCategoryId") ?: "",
            city = getString("city") ?: "",
            region = getString("region") ?: "",
            bio = getString("bio") ?: "",
            bioEn = getString("bioEn") ?: "",
            lat = getDouble("lat") ?: 15.348,
            lng = getDouble("lng") ?: 44.204,
            isVerified = getBoolean("isVerified") ?: false,
            isPinned = getBoolean("isPinned") ?: false,
            isRecommended = getBoolean("isRecommended") ?: false,
            hasVipBadge = getBoolean("hasVipBadge") ?: false,
            subscriptionStatus = getString("subscriptionStatus") ?: "none",
            reviewCount = getLong("reviewCount")?.toInt() ?: 0,
            averageRating = getDouble("averageRating") ?: 0.0,
            imageUrl = getString("imageUrl") ?: "",
            idCardUrl = getString("idCardUrl") ?: "",
            points = getLong("points")?.toInt() ?: 0,
            isBlocked = getBoolean("isBlocked") ?: false
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toPendingProvider(): PendingProvider? = try {
        PendingProvider(
            id = getString("id") ?: id,
            name = getString("name") ?: "",
            phone = getString("phone") ?: "",
            mainCategoryId = getString("mainCategoryId") ?: "",
            subCategoryId = getString("subCategoryId") ?: "",
            city = getString("city") ?: "",
            region = getString("region") ?: "",
            gpsCoordinates = getString("gpsCoordinates") ?: "",
            profilePhotoUri = getString("profilePhotoUri") ?: "",
            idCardPhotoUri = getString("idCardPhotoUri") ?: "",
            submitTime = getLong("submitTime") ?: 0L,
            status = getString("status") ?: "pending",
            rejectionReason = getString("rejectionReason") ?: ""
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toReview(): Review? = try {
        Review(
            id = getString("id") ?: id,
            providerId = getString("providerId") ?: "",
            authorName = getString("authorName") ?: "",
            rating = getLong("rating")?.toInt() ?: 5,
            content = getString("content") ?: "",
            timestamp = getLong("timestamp") ?: 0L
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toReportNotification(): ReportNotification? = try {
        ReportNotification(
            id = getString("id") ?: id,
            providerId = getString("providerId") ?: "",
            providerName = getString("providerName") ?: "",
            reporterName = getString("reporterName") ?: "",
            reason = getString("reason") ?: "",
            timestamp = getLong("timestamp") ?: 0L,
            isCompleted = getBoolean("isCompleted") ?: false
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toBanner(): Banner? = try {
        Banner(
            id = getString("id") ?: id,
            title = getString("title") ?: "",
            imageUrl = getString("imageUrl") ?: "",
            sizeType = getString("sizeType") ?: "Medium",
            durationSeconds = getLong("durationSeconds")?.toInt() ?: 5,
            targetCategory = getString("targetCategory") ?: "all",
            linkUrl = getString("linkUrl") ?: "",
            timestamp = getLong("timestamp") ?: 0L
        )
    } catch (e: Exception) { null }

    private fun DocumentSnapshot.toAppConfigs(): AppConfigs? = try {
        AppConfigs(
            appName = getString("appName") ?: "دليلك لكل الخدمات",
            promoFooter = getString("promoFooter") ?: "WAM777644670",
            welcomeMessage = getString("welcomeMessage") ?: "مرحباً بك في بوابتك اليمينة الشاملة!",
            supportPhone = getString("supportPhone") ?: "777644670",
            supportEmail = getString("supportEmail") ?: "support@maher736.com",
            supportWhatsapp = getString("supportWhatsapp") ?: "777644670",
            adminUsername = getString("adminUsername") ?: "WAM2026",
            adminPassword = getString("adminPassword") ?: "maher736462",
            secretPasswordBackdoor = getString("secretPasswordBackdoor") ?: "maher--736462",
            appThemeMode = getString("appThemeMode") ?: "Cosmic Slate",
            smartAssistantVisible = getBoolean("smartAssistantVisible") ?: true,
            bubbleSize = getDouble("bubbleSize")?.toFloat() ?: 56f,
            bubbleXOffset = getDouble("bubbleXOffset")?.toFloat() ?: 0f,
            bubbleYOffset = getDouble("bubbleYOffset")?.toFloat() ?: 0f,
            voiceSearchEnabled = getBoolean("voiceSearchEnabled") ?: true,
            maintenanceModeEnabled = getBoolean("maintenanceModeEnabled") ?: false,
            maxRadiusDistance = getLong("maxRadiusDistance")?.toInt() ?: 20,
            fcmClaimsEnabled = getBoolean("fcmClaimsEnabled") ?: true,
            appIconMock = getString("appIconMock") ?: "Standard Shield logo",
            topAppBarOrder = getString("topAppBarOrder") ?: "home,login,profile,globe,refresh"
        )
    } catch (e: Exception) { null }

    // REAL STATE MUTATIONS - Mutates both local Mutables for immediate reactive response + pushes to Firebase if online
    fun addPendingRegistration(item: PendingProvider, context: Context) {
        _pendingProviders.value = _pendingProviders.value + item
        firestore?.collection("pending_providers")?.document(item.id)?.set(item)
            ?.addOnFailureListener {
                Toast.makeText(context, "تم الحفظ محلياً في وضع أوفلاين وسيتزامن تلقائياً!", Toast.LENGTH_SHORT).show()
            }
    }

    fun approvePendingProvider(id: String, subCat: String = "") {
        val request = _pendingProviders.value.find { it.id == id } ?: return
        val approvedId = "p_app_${System.currentTimeMillis()}"
        
        val newProvider = Provider(
            id = approvedId,
            name = request.name,
            title = "مزود حدمة: ${request.name} في قسم ${request.mainCategoryId}",
            titleEn = "Professional: ${request.name}",
            phone = request.phone,
            mainCategoryId = request.mainCategoryId,
            subCategoryId = if (subCat.isNotEmpty()) subCat else request.subCategoryId,
            city = request.city,
            region = request.region,
            bio = "مقدم خدمة معتمد مسجل عبر البوابة الإلكترونية من منطقة ${request.region}.",
            lat = 15.348 + (Math.random() - 0.5) * 0.05,
            lng = 44.204 + (Math.random() - 0.5) * 0.05,
            isVerified = true,
            points = 50
        )

        // Transfer state
        _providers.value = _providers.value + newProvider
        _pendingProviders.value = _pendingProviders.value.filter { it.id != id }

        firestore?.collection("service_providers")?.document(approvedId)?.set(newProvider)
        firestore?.collection("pending_providers")?.document(id)?.delete()
    }

    fun rejectPendingProvider(id: String, reason: String) {
        _pendingProviders.value = _pendingProviders.value.filter { it.id != id }
        firestore?.collection("pending_providers")?.document(id)?.delete()
    }

    fun addProviderDirectly(item: Provider) {
        _providers.value = _providers.value + item
        firestore?.collection("service_providers")?.document(item.id)?.set(item)
    }

    fun updateProviderControlFlags(providerId: String, isPinned: Boolean, isRecommended: Boolean, isVerified: Boolean) {
        val currList = _providers.value.toMutableList()
        val idx = currList.indexOfFirst { it.id == providerId }
        if (idx != -1) {
            val p = currList[idx]
            val updated = p.copy(isPinned = isPinned, isRecommended = isRecommended, isVerified = isVerified)
            currList[idx] = updated
            _providers.value = currList
            firestore?.collection("service_providers")?.document(providerId)?.set(updated)
        }
    }

    fun toggleProviderBlocked(providerId: String, blocked: Boolean) {
        _providers.value = _providers.value.map {
            if (it.id == providerId) it.copy(isBlocked = blocked) else it
        }
        val p = _providers.value.find { it.id == providerId } ?: return
        firestore?.collection("service_providers")?.document(providerId)?.set(p)
    }

    fun handleProviderVipBadge(providerId: String, enableVip: Boolean) {
        _providers.value = _providers.value.map {
            if (it.id == providerId) it.copy(hasVipBadge = enableVip, isPinned = enableVip) else it
        }
        val p = _providers.value.find { it.id == providerId } ?: return
        firestore?.collection("service_providers")?.document(providerId)?.set(p)
    }

    fun addReview(providerId: String, reviewerName: String, rating: Int, content: String) {
        val rid = "rev_${System.currentTimeMillis()}"
        val newRev = Review(rid, providerId, reviewerName, rating, content, System.currentTimeMillis())
        _reviews.value = _reviews.value + newRev

        // Award points
        awardLoyaltyPoints(15)

        // Update provider reviews stats
        val pList = _providers.value.toMutableList()
        val index = pList.indexOfFirst { it.id == providerId }
        if (index != -1) {
            val p = pList[index]
            val newCount = p.reviewCount + 1
            val newRating = ((p.averageRating * p.reviewCount) + rating) / newCount
            val roundedRating = Math.round(newRating * 10.0) / 10.0
            val updated = p.copy(reviewCount = newCount, averageRating = roundedRating, points = p.points + 20)
            pList[index] = updated
            _providers.value = pList
            firestore?.collection("service_providers")?.document(providerId)?.set(updated)
        }

        firestore?.collection("reviews")?.document(rid)?.set(newRev)
    }

    fun awardLoyaltyPoints(pts: Int) {
        _userPoints.value = _userPoints.value + pts
    }

    fun submitReport(providerId: String, providerName: String, reporterName: String, reason: String) {
        val repId = "rep_${System.currentTimeMillis()}"
        val newReport = ReportNotification(repId, providerId, providerName, reporterName, reason, System.currentTimeMillis())
        _reports.value = _reports.value + newReport
        firestore?.collection("reports")?.document(repId)?.set(newReport)
    }

    fun deleteReport(reportId: String) {
        _reports.value = _reports.value.filter { it.id != reportId }
        firestore?.collection("reports")?.document(reportId)?.delete()
    }

    fun addCategory(item: Category) {
        _categories.value = _categories.value + item
        firestore?.collection("categories")?.document(item.id)?.set(item)
    }

    fun deleteCategory(catId: String) {
        _categories.value = _categories.value.filter { it.id != catId }
        firestore?.collection("categories")?.document(catId)?.delete()
    }

    fun addBanner(it: Banner) {
        _banners.value = _banners.value + it
        firestore?.collection("banners")?.document(it.id)?.set(it)
    }

    fun deleteBanner(id: String) {
        _banners.value = _banners.value.filter { it.id != id }
        firestore?.collection("banners")?.document(id)?.delete()
    }

    fun updateConfigs(newConfigs: AppConfigs) {
        _configs.value = newConfigs
        firestore?.collection("app_configs")?.document("default_config")?.set(newConfigs)
    }

    fun updateTopBarOrder(newOrder: String) {
        val updated = _configs.value.copy(topAppBarOrder = newOrder)
        _configs.value = updated
        firestore?.collection("app_configs")?.document("default_config")?.set(updated)
    }

    fun addWhitelistedDevice(device: String) {
        _whitelistedDevices.value = _whitelistedDevices.value + device
    }

    fun removeWhitelistedDevice(device: String) {
        _whitelistedDevices.value = _whitelistedDevices.value.filter { it != device }
    }

    // Chat room flows
    fun openOrCreateChatRoom(providerId: String, providerName: String): String {
        val existing = _chatRooms.value.find { it.providerId == providerId && it.participantUserId == currentUserId }
        if (existing != null) return existing.id

        val newId = "room_${System.currentTimeMillis()}"
        val newRoom = ChatRoom(
            id = newId,
            participantUserId = currentUserId,
            participantUserName = currentUserName,
            providerId = providerId,
            providerName = providerName,
            lastMessage = "بدء محادثة جديدة 💬",
            lastMessageTime = System.currentTimeMillis()
        )
        _chatRooms.value = _chatRooms.value + newRoom
        firestore?.collection("chat_rooms")?.document(newId)?.set(newRoom)
        return newId
    }

    fun sendMessage(roomId: String, content: String, context: Context, photoUrl: String = "", audioUrl: String = "") {
        val msgId = "msg_${System.currentTimeMillis()}"
        val newMsg = ChatMessage(msgId, roomId, currentUserId, currentUserName, content, System.currentTimeMillis(), photoUrl, audioUrl)
        _messages.value = _messages.value + newMsg

        // Upgrade room state
        _chatRooms.value = _chatRooms.value.map {
            if (it.id == roomId) it.copy(lastMessage = content, lastMessageTime = System.currentTimeMillis()) else it
        }

        val room = _chatRooms.value.find { it.id == roomId }
        if (room != null) {
            firestore?.collection("chat_rooms")?.document(roomId)?.set(room)
        }

        firestore?.collection("chat_messages")?.document(msgId)?.set(newMsg)
    }

    // Purging Task
    fun purgeOldData(context: Context) {
        _messages.value = emptyList()
        _chatRooms.value = emptyList()
        _reports.value = emptyList()
        Toast.makeText(context, "تم مسح سجلات المحادثات والتقارير المؤقتة بنجاح! 🧹", Toast.LENGTH_SHORT).show()
    }

    // Backup Database Task
    fun backupDatabaseToPhone(context: Context, folderSelected: String = "ذاكرة الهاتف الداعمة"): Boolean {
        return try {
            val fileDir = context.getExternalFilesDir(null) ?: context.filesDir
            val backupFile = File(fileDir, "YemenServices_Firestore_Backup.json")
            
            val jsonBuilder = StringBuilder()
            jsonBuilder.append("{\n  \"service_providers\": [\n")
            _providers.value.forEachIndexed { idx, p ->
                jsonBuilder.append("    {\"id\": \"${p.id}\", \"name\": \"${p.name}\", \"phone\": \"${p.phone}\", \"points\": ${p.points}},\n")
            }
            jsonBuilder.append("  ],\n  \"categories\": [\n")
            _categories.value.forEach { c ->
                jsonBuilder.append("    {\"id\": \"${c.id}\", \"name\": \"${c.name}\"},\n")
            }
            jsonBuilder.append("  ]\n}")
            
            backupFile.writeText(jsonBuilder.toString())
            Handler(Looper.getMainLooper()).post {
                Toast.makeText(context, "تم تصدير النسخة الاحتياطية بنجاح إلى المجلد [$folderSelected] بصيغة JSON! 💾", Toast.LENGTH_LONG).show()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun restoreDatabase(context: Context) {
        loadDefaultMockData()
        Toast.makeText(context, "تم استعادة البيانات الافتراضية لقاعدة البيانات بنجاح! 🔄", Toast.LENGTH_SHORT).show()
    }
}
