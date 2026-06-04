package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object FirestoreSim {
    private const val PREFS_NAME = "firestore_sim_prefs"
    private var prefs: SharedPreferences? = null

    // State flows representing real-time listeners (Snapshot listeners)
    private val _providers = MutableStateFlow<List<Provider>>(emptyList())
    val providers = _providers.asStateFlow()

    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    val reviews = _reviews.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms = _chatRooms.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages = _messages.asStateFlow()

    private val _configs = MutableStateFlow(AppConfigs())
    val configs = _configs.asStateFlow()

    private val _notificationTemplates = MutableStateFlow<List<NotificationTemplate>>(emptyList())
    val notificationTemplates = _notificationTemplates.asStateFlow()

    private val _reportNotifications = MutableStateFlow<List<ReportNotification>>(emptyList())
    val reportNotifications = _reportNotifications.asStateFlow()

    private val _pendingProviders = MutableStateFlow<List<PendingProvider>>(emptyList())
    val pendingProviders = _pendingProviders.asStateFlow()

    // Logged in user info
    // "guest" format is used if logged off
    var currentUserId = "guest"
    var currentUserName = "زائر كريم"
    var currentUserPhone = ""
    var currentUserAvatar = ""

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadAll()

        if (_providers.value.isEmpty()) {
            loadDefaultData()
        }
    }

    private fun loadAll() {
        val pJson = prefs?.getString("providers", "[]") ?: "[]"
        val rJson = prefs?.getString("reviews", "[]") ?: "[]"
        val crJson = prefs?.getString("chatRooms", "[]") ?: "[]"
        val mJson = prefs?.getString("messages", "[]") ?: "[]"
        val cJson = prefs?.getString("configs", "") ?: ""
        val ntJson = prefs?.getString("notificationTemplates", "[]") ?: "[]"
        val repJson = prefs?.getString("reports", "[]") ?: "[]"
        val pendJson = prefs?.getString("pending", "[]") ?: "[]"

        val jsonObj = Json { ignoreUnknownKeys = true; coerceInputValues = true }

        try {
            _providers.value = jsonObj.decodeFromString<List<Provider>>(pJson)
            _reviews.value = jsonObj.decodeFromString<List<Review>>(rJson)
            _chatRooms.value = jsonObj.decodeFromString<List<ChatRoom>>(crJson)
            _messages.value = jsonObj.decodeFromString<List<ChatMessage>>(mJson)
            _configs.value = if (cJson.isNotEmpty()) jsonObj.decodeFromString<AppConfigs>(cJson) else AppConfigs()
            _notificationTemplates.value = jsonObj.decodeFromString<List<NotificationTemplate>>(ntJson)
            _reportNotifications.value = jsonObj.decodeFromString<List<ReportNotification>>(repJson)
            _pendingProviders.value = jsonObj.decodeFromString<List<PendingProvider>>(pendJson)
        } catch (e: Exception) {
            Log.e("FirestoreSim", "Failed to decode firestore simulation data, resetting to defaults", e)
            loadDefaultData()
        }
    }

    private fun saveAll() {
        val json = Json { prettyPrint = false }
        prefs?.edit()?.apply {
            putString("providers", json.encodeToString(_providers.value))
            putString("reviews", json.encodeToString(_reviews.value))
            putString("chatRooms", json.encodeToString(_chatRooms.value))
            putString("messages", json.encodeToString(_messages.value))
            putString("configs", json.encodeToString(_configs.value))
            putString("notificationTemplates", json.encodeToString(_notificationTemplates.value))
            putString("reports", json.encodeToString(_reportNotifications.value))
            putString("pending", json.encodeToString(_pendingProviders.value))
            apply()
        }
    }

    private fun loadDefaultData() {
        // Initial providers from major Yemeni regions
        _providers.value = listOf(
            Provider(
                id = "p1",
                name = "المهندس عادل الوادعي",
                title = "خبير شبكات وتمديدات كهربائية متكاملة ⚡",
                bio = "خبرة أكثر من ١٢ عاماً في تركيب شبكات الطاقة الشمسية وتوصيل كابلات المنازل الحديثة بأحدث المقاييس في صنعاء.",
                phone = "771122334",
                mainCategoryId = "electricity",
                subCategoryId = "solar",
                region = "صنعاء - شارع حدة",
                isVerified = true,
                isPinned = true,
                isRecommended = true,
                averageRating = 4.8,
                reviewCount = 2,
                lat = 15.3121,
                lng = 44.1952
            ),
            Provider(
                id = "p2",
                name = "الفني محمد الصبري",
                title = "سباك صحي وخبير تمديد شبكات مياه 🚰",
                bio = "متخصص في تأسيس وتشطيب فلل وعمارات سكنية، وصيانة الفلاتر والمضخمات في العاصمة صنعاء الكبرى.",
                phone = "775566778",
                mainCategoryId = "plumbing",
                subCategoryId = "pipes",
                region = "صنعاء - باب اليمن",
                isVerified = true,
                isPinned = false,
                isRecommended = true,
                averageRating = 4.5,
                reviewCount = 1,
                lat = 15.3524,
                lng = 44.2078
            ),
            Provider(
                id = "p3",
                name = "المبرمج وضاح العولقي",
                title = "مطور تطبيقات أندرويد وتصميم المتاجر الإلكترونية 📱",
                bio = "مطور برمجيات ذو كفاءة عالية، أقوم بتحويل أفكار المشاريع والخدمات المحلية إلى برامج سلسة تدعم اللغة العربية في عدن.",
                phone = "733445566",
                mainCategoryId = "tech",
                subCategoryId = "android",
                region = "عدن - كريتر",
                isVerified = false,
                isPinned = false,
                isRecommended = true,
                averageRating = 5.0,
                reviewCount = 1,
                lat = 12.7852,
                lng = 45.0354
            ),
            Provider(
                id = "p4",
                name = "المعلم صالح العصيمي",
                title = "خبير نجارة وديكورات خشبية راقية 🪵",
                bio = "تصميم صناعة غرف النوم التركية، أبواب المساجد المزخرفة، والأسقف المعلقة بدقة متناهية تحت طابع تعزي أصيل.",
                phone = "711229988",
                mainCategoryId = "carpentry",
                subCategoryId = "decor",
                region = "تعز - شارع جمال",
                isVerified = false,
                isPinned = true,
                isRecommended = false,
                averageRating = 4.0,
                reviewCount = 1,
                lat = 13.5824,
                lng = 44.0125
            ),
            Provider(
                id = "p5",
                name = "الميكانيكي أمين السويدي",
                title = "خبير هيدروليك وصيانة محركات الديزل والبترول 🚗",
                bio = "تشخيص أعطال كمبيوتر السيارات بالسونار، وتوضيب المحركات وناقل السرعات الأتوماتيكي بمهارة وعناية فائقة في الحديدة.",
                phone = "771234567",
                mainCategoryId = "mechanics",
                subCategoryId = "diesel",
                region = "الحديدة - شارع صنعاء",
                isVerified = false,
                isPinned = false,
                isRecommended = false,
                averageRating = 0.0,
                reviewCount = 0,
                lat = 14.7951,
                lng = 42.9542
            )
        )

        // Prepopulate reviews
        _reviews.value = listOf(
            Review("r1", "p1", "أبو بكر الحمادي", 5, "عمل هندسي متقن جداً، قام بتركيب المنظومة الشمسية بمهنية فائقة ووقت وجيز ونعم الأخلاق.", 1717416000000),
            Review("r2", "p1", "خالد يحيى", 5, "خبير ومستشار مميز في حل مشاكل ماس الكهرباء، شكرا جز يلا.", 1717436000000),
            Review("r3", "p2", "فؤاد الغباري", 4, "شغله نظيف وسريع في صيانة تصريف المياه بالحمامات والمجاري.", 1717456000000),
            Review("r4", "p3", "م. سارة الصنعاني", 5, "طور لنا تطبيق المتجر باحترافية، سريع الاستجابة ومرن بالتعامل.", 1717476000000),
            Review("r5", "p4", "ياسر القدسي", 4, "شغله بالخشب الصولد قوي ومتين صمم لنا دولاب ممتاز يستحق الشكر.", 1717496000000)
        )

        // Prepopulate templates
        _notificationTemplates.value = listOf(
            NotificationTemplate("welcome", "رسالة الترحيب التلقائية", "أهلاً بك في تطبيق دليلك للخدمات 👋", "نشكر انضمامك إلينا! دليلك يوصلك بأفضل الحرفيين والفنيين المعتمدين في اليمن بضغطة زر.", true, 1),
            NotificationTemplate("appointment", "تذكير المواعيد والتنسيق", "تأكيد واستذكار الموعد المقترح 📆", "عزيزنا العميل، هذا تذكير بموعد الخدمة المتفق عليه مع الفني. يرجى المتابعة لضمان سلامة التنفيذ.", true, 60),
            NotificationTemplate("bill", "تنبيه الفواتير والدفع الإلكتروني", "تنبيه سداد الفاتورة 💳", "شريكنا العزيز، صدرت فاتورة الخدمة المنجزة. يمكنك الاطلاع على التفاصيل وتأكيد الدفع بأمان.", false, 10)
        )

        // Prepopulate report notifications
        _reportNotifications.value = listOf(
            ReportNotification("rep1", "محاولة دخول غير مصرحة للوحة التحكم", "تم رصد محاولة وصول من جهاز غير مسجل في القائمة الموثوقة (Whitelist). رقم الـ IP: 192.168.1.105", "UNAUTHORIZED_LOGIN", System.currentTimeMillis() - 600000 * 5),
            ReportNotification("rep2", "بلاغ أمني عن إساءة استخدام", "قام المستخدم فؤاد بالتبليغ عن حساب الموفر 'أمين السويدي' بدعوى طلب أسعار مبالغ فيها وعدم الحضور بالميعاد.", "COMPLAINT", System.currentTimeMillis() - 3600000),
            ReportNotification("rep3", "طلب اشتراك ممول / شهري", "الموفر 'المهندس عادل الوادعي' قدم طلباً لترقية الخدمة الممولة للحصول على شارة التوصية والظهور المثبت في الصدارة.", "SUBSCRIPTION_REQUEST", System.currentTimeMillis() - 1200000)
        )

        _configs.value = AppConfigs()

        saveAll()
    }

    // Helper functions
    fun addReview(providerId: String, reviewerName: String, rating: Int, comment: String) {
        val newReview = Review(
            id = "rev_${System.currentTimeMillis()}",
            providerId = providerId,
            reviewerName = reviewerName,
            rating = rating,
            comment = comment,
            timestamp = System.currentTimeMillis()
        )
        val updatedReviews = _reviews.value + newReview
        _reviews.value = updatedReviews

        // Re-calculate average rating for provider
        val currentProviderReviews = updatedReviews.filter { it.providerId == providerId }
        val avg = currentProviderReviews.map { it.rating }.average()
        val count = currentProviderReviews.size

        _providers.value = _providers.value.map {
            if (it.id == providerId) {
                it.copy(averageRating = (Math.round(avg * 10.0) / 10.0), reviewCount = count)
            } else {
                it
            }
        }

        saveAll()
    }

    fun submitJoinRequest(name: String, title: String, bio: String, phone: String, region: String, selfie: String, idCard: String) {
        val newPending = PendingProvider(
            id = "pending_${System.currentTimeMillis()}",
            name = name,
            title = title,
            bio = bio,
            phone = phone,
            region = region,
            selfieUri = selfie,
            idCardUri = idCard,
            timestamp = System.currentTimeMillis()
        )
        _pendingProviders.value = _pendingProviders.value + newPending

        // Add real-time notification log to admin panel about the registry
        addAdminReport(
            title = "طلب انضمام فني جديد للخدمة 🛠️",
            content = "قدم الفني '$name' طلباً للانضمام إلى دليل مقدمي الخدمات المعتمدين في بوابتك بصورة سيلفي وبطاقة هوية مرفقة قيد المراجعة.",
            type = "SUBSCRIPTION_REQUEST"
        )
        saveAll()
    }

    fun approvePendingProvider(pendingId: String, mainCat: String, subCat: String) {
        val pending = _pendingProviders.value.find { it.id == pendingId } ?: return
        val newProvider = Provider(
            id = "prov_${System.currentTimeMillis()}",
            name = pending.name,
            title = pending.title,
            bio = pending.bio,
            phone = pending.phone,
            mainCategoryId = mainCat,
            subCategoryId = subCat,
            region = pending.region,
            selfieUri = pending.selfieUri,
            idCardUri = pending.idCardUri,
            isVerified = true,
            lat = 15.3 + (Math.random() - 0.5) * 0.2,
            lng = 44.2 + (Math.random() - 0.5) * 0.2
        )
        _providers.value = _providers.value + newProvider
        _pendingProviders.value = _pendingProviders.value.filter { it.id != pendingId }

        saveAll()
    }

    fun rejectPendingProvider(pendingId: String) {
        _pendingProviders.value = _pendingProviders.value.filter { it.id != pendingId }
        saveAll()
    }

    // Toggle admin permissions
    fun toggleProviderVerification(id: String) {
        _providers.value = _providers.value.map {
            if (it.id == id) it.copy(isVerified = !it.isVerified) else it
        }
        saveAll()
    }

    fun toggleProviderPinned(id: String) {
        _providers.value = _providers.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
        saveAll()
    }

    fun toggleProviderRecommended(id: String) {
        _providers.value = _providers.value.map {
            if (it.id == id) it.copy(isRecommended = !it.isRecommended) else it
        }
        saveAll()
    }

    fun setProviderBlocked(id: String, blocked: Boolean) {
        _providers.value = _providers.value.map {
            if (it.id == id) it.copy(isBlocked = blocked) else it
        }
        saveAll()
    }

    // Direct configuration edits
    fun updateAppConfigs(newConfigs: AppConfigs) {
        _configs.value = newConfigs
        saveAll()
    }

    // Add alert logs for admin panel
    fun addAdminReport(title: String, content: String, type: String) {
        val newReport = ReportNotification(
            id = "rep_${System.currentTimeMillis()}",
            title = title,
            content = content,
            type = type,
            timestamp = System.currentTimeMillis()
        )
        _reportNotifications.value = listOf(newReport) + _reportNotifications.value
        saveAll()
    }

    fun markReportFileReviewed(id: String) {
        _reportNotifications.value = _reportNotifications.value.map {
            if (it.id == id) it.copy(isReviewed = true) else it
        }
        saveAll()
    }

    fun deleteAdminReport(id: String) {
        _reportNotifications.value = _reportNotifications.value.filter { it.id != id }
        saveAll()
    }

    // Real-time Chat functions
    fun sendMessage(chatRoomId: String, text: String, context: Context, imageUri: String? = null) {
        val roomIndex = _chatRooms.value.indexOfFirst { it.id == chatRoomId }
        if (roomIndex == -1) return
        val room = _chatRooms.value[roomIndex]

        val senderNameValue = if (currentUserId == "guest") "زائر كريم" else currentUserName

        val message = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            chatRoomId = chatRoomId,
            senderId = currentUserId,
            senderName = senderNameValue,
            content = text,
            timestamp = System.currentTimeMillis(),
            imageAttachedUri = imageUri
        )

        _messages.value = _messages.value + message
        
        // Update room log
        _chatRooms.value = _chatRooms.value.map {
            if (it.id == chatRoomId) {
                it.copy(lastMessage = text, lastTimestamp = System.currentTimeMillis())
            } else {
                it
            }
        }
        saveAll()

        // Admin notification triggers
        addAdminReport(
            title = "رسالة دردشة حية نشطة 💬",
            content = "أرسل العميل '$senderNameValue' رسالة إلى مقدم الخدمة '${room.providerName}': \"$text\"",
            type = "COMPLAINT"
        )

        // Provider automated reply simulation to make it feel extremely alive
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if provider is blocked, if so, no reply
            val provider = _providers.value.find { it.id == room.providerId }
            val globalServiceBlocked = _providers.value.all { it.isBlocked }

            if (provider?.isBlocked == true || globalServiceBlocked) {
                val blockedMsg = ChatMessage(
                    id = "msg_autorep_${System.currentTimeMillis()}",
                    chatRoomId = chatRoomId,
                    senderId = room.providerId,
                    senderName = "دليل الخدمات التلقائي 🤖",
                    content = "عذراً، فني الخدمة هذا أو هذا القسم متوقف حالياً بأمر من الإدارة العامة بالبوابة 🚫",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value + blockedMsg
                saveAll()
                return@postDelayed
            }

            // Generate intelligent replies from providers depending on their scope
            val replyText = when (provider?.mainCategoryId) {
                "electricity" -> "حياك الله يا غالي. أنا متاح الآن وجاهز لمعاينة العطل في ممددات الشبكة. بخصوص أسعار الطاقة الشمسية تختلف بنوع اللوح والبطارية الكوري أو الهندسي. أين موقعك وسأتواصل معك؟ 🔌🛠️"
                "plumbing" -> "السلام عليكم ورحمة الله، تمديد وتصريف وتركيب مضخات المياه جاهز لفحصها فوراً يا طيب. هل هي حالة طارئة من تسرب مائي؟"
                "tech" -> "مرحباً بك! يسعدني جداً العمل على تطبيقك أو متجرك الإلكتروني. نعم نحن نقوم بالدعم وتصميم قواعد البيانات مع ربطها بالخوادم المحلية والـ Firestore. دعنا نحدد المتطلبات للتكلفة الكلية."
                "carpentry" -> "يا هلا بك، غرف وتفصيل وتصليح أبواب ومطابخ حسب رغبتك ومقاس منزلك بكل دقة ممكنة إن شاء الله."
                "mechanics" -> "أهلاً وسهلاً بك يا غالي. صيانة ميكانيك وتوضيب وتشخيص بالكمبيوتر متاح بصبر وعناية في ورشتنا المتكاملة، تفضل بزيارتنا وسنقوم بالفحص."
                else -> "حياك الله أخي وتفضل بكيفية مساعدتك وسأتجاوب معك فوراً بإذن الله 🇾🇪"
            }

            val replyMsg = ChatMessage(
                id = "msg_autorep_${System.currentTimeMillis()}",
                chatRoomId = chatRoomId,
                senderId = room.providerId,
                senderName = room.providerName,
                content = replyText,
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + replyMsg

            _chatRooms.value = _chatRooms.value.map {
                if (it.id == chatRoomId) {
                    it.copy(lastMessage = replyText, lastTimestamp = System.currentTimeMillis())
                } else {
                    it
                }
            }
            saveAll()

            // Trigger simulated push notification (FCM) to client
            if (_configs.value.fcmEnabled) {
                val toastMsg = "🔔 تلقيت رسالة من الموفر ${room.providerName}: $replyText"
                Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
            }
        }, 3000)
    }

    fun openOrCreateChatRoom(providerId: String, providerName: String): String {
        val existing = _chatRooms.value.find {
            it.userId == currentUserId && it.providerId == providerId
        }
        if (existing != null) return existing.id

        val newId = "room_${System.currentTimeMillis()}"
        val newRoom = ChatRoom(
            id = newId,
            userId = currentUserId,
            userName = if (currentUserId == "guest") "زائر كريم" else currentUserName,
            providerId = providerId,
            providerName = providerName,
            lastMessage = "بدء دردشة حية فورية جديدة 💬",
            lastTimestamp = System.currentTimeMillis()
        )
        _chatRooms.value = _chatRooms.value + newRoom
        saveAll()
        return newId
    }

    // Toggle individual templates
    fun toggleTemplateEnabled(id: String) {
        _notificationTemplates.value = _notificationTemplates.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
        saveAll()
    }

    fun updateTemplate(id: String, title: String, body: String, delay: Int) {
        _notificationTemplates.value = _notificationTemplates.value.map {
            if (it.id == id) it.copy(title = title, body = body, sendDelayMinutes = delay) else it
        }
        saveAll()
    }

    // Reset database to initial pristine state
    fun resetDatabase() {
        loadDefaultData()
    }
}
