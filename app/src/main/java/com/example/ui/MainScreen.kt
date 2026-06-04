package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    // Observe DB States
    val configs by FirestoreSim.configs.collectAsState()
    val categories by FirestoreSim.categories.collectAsState()
    val providers by FirestoreSim.providers.collectAsState()
    val banners by FirestoreSim.banners.collectAsState()
    val reviews by FirestoreSim.reviews.collectAsState()
    val userPoints by FirestoreSim.userPoints.collectAsState()

    val isAr = FirestoreSim.currentLang.collectAsState().value == "ar"

    // Search and filters State
    var searchKeyword by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<String?>("all") }
    var selectedCityFilter by remember { mutableStateOf("الكل") }
    var selectedRadiusFilter by remember { mutableStateOf(15f) } // Default maximum radius in km

    // Voice search simulator state
    var voiceSearchActive by remember { mutableStateOf(false) }
    var voiceSearchQueryInput by remember { mutableStateOf("") }

    // Navigation and screen controllers
    var currentScreenState by remember { mutableStateOf("home") } // home, login_panel, join_panel, chat_list
    var activeChatRoomId by remember { mutableStateOf<String?>(null) }

    // Backdoor 5-tap counter
    var backdoorTapsCount by remember { mutableStateOf(0) }

    // Dynamic banner auto-rotation
    var activeBannerIndex by remember { mutableStateOf(0) }
    LaunchedEffect(banners) {
        if (banners.isNotEmpty()) {
            while (true) {
                delay(8000)
                activeBannerIndex = (activeBannerIndex + 1) % banners.size
            }
        }
    }

    // Detail dialog states
    var selectedProviderForDetail by remember { mutableStateOf<Provider?>(null) }
    var showReviewPostDialog by remember { mutableStateOf(false) }
    var showReportPostDialog by remember { mutableStateOf(false) }

    // Theme Selector Color Constants based on App settings
    val primaryThemeColor = when (configs.appThemeMode) {
        "Charcoal Gold" -> Color(0xFFD4AF37)
        "Royal Emerald" -> Color(0xFF10B981)
        else -> Color(0xFFFFD700) // Cosmic Slate Gold
    }

    val scaffoldBgColor = when (configs.appThemeMode) {
        "Royal Emerald" -> Color(0xFF022C22)
        "Charcoal Gold" -> Color(0xFF1C1917)
        else -> Color(0xFF030712) // Cosmic Slate
    }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = primaryThemeColor,
            background = scaffoldBgColor,
            surface = Color(0xFF111827)
        )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                // Customized Upper Bar - ordered dynamically from Firestore configuration
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF111827))
                        .padding(vertical = 10.dp, horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand Logo Name - Tapping 5 times activates backdoor trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    backdoorTapsCount++
                                    if (backdoorTapsCount >= 5) {
                                        backdoorTapsCount = 0
                                        currentScreenState = "login_panel"
                                        Toast.makeText(context, "تم تنشيط تشغيل البوابة الخلفية السرية لمالك التطبيق! 🔒", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Logo icon",
                                tint = primaryThemeColor,
                                modifier = Modifier.size(28.dp).padding(end = 4.dp)
                            )
                            Text(
                                text = configs.appName,
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }

                        // Parse the Configurable Comma-Separated Navigation Items from Firestore
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            configs.topAppBarOrder.split(",").forEach { actionKey ->
                                when (actionKey.trim().lowercase()) {
                                    "home" -> {
                                        IconButton(
                                            onClick = { 
                                                currentScreenState = "home" 
                                                backdoorTapsCount = 0
                                            },
                                            modifier = Modifier.testTag("appbar_home_btn")
                                        ) {
                                            Icon(Icons.Default.Home, contentDescription = "Home", tint = if (currentScreenState == "home") primaryThemeColor else Color.White)
                                        }
                                    }
                                    "login" -> {
                                        IconButton(
                                            onClick = { currentScreenState = "login_panel" },
                                            modifier = Modifier.testTag("appbar_login_btn")
                                        ) {
                                            Icon(Icons.Default.Lock, contentDescription = "Admin Lobby", tint = if (currentScreenState == "login_panel") primaryThemeColor else Color.White)
                                        }
                                    }
                                    "profile" -> {
                                        IconButton(
                                            onClick = { currentScreenState = "join_panel" },
                                            modifier = Modifier.testTag("appbar_profile_btn")
                                        ) {
                                            Icon(Icons.Default.PersonAdd, contentDescription = "Register Provider", tint = if (currentScreenState == "join_panel") primaryThemeColor else Color.White)
                                        }
                                    }
                                    "globe" -> {
                                        IconButton(onClick = {
                                            val nextLang = if (isAr) "en" else "ar"
                                            FirestoreSim.currentLang.value = nextLang
                                            Toast.makeText(context, if (nextLang == "ar") "تم تفعيل اللغة العربية" else "English Interface Enabled", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Language, contentDescription = "Switch Language", tint = Color.White)
                                        }
                                    }
                                    "refresh" -> {
                                        IconButton(onClick = {
                                            Toast.makeText(context, "جاري المزامنة الفورية وتحميل أحدث السجلات... 🔄", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Sync", tint = Color.White)
                                        }
                                    }
                                }
                            }

                            // Extra chats list entry point
                            IconButton(onClick = { currentScreenState = "chat_list" }) {
                                BadgedBox(badge = { Badge { Text("3") } }) {
                                    Icon(Icons.Default.Forum, contentDescription = "Chats", tint = if (currentScreenState == "chat_list") primaryThemeColor else Color.White)
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(scaffoldBgColor)
                    .padding(paddingValues)
            ) {
                // Maintenance Mode Blocker
                if (configs.maintenanceModeEnabled && !FirestoreSim.isBackdoorOwnerLoggedIn) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handyman,
                            contentDescription = "Maintenance icon",
                            tint = primaryThemeColor,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (isAr) "المنصة في وضع صيانة وتحديث مبرمجة" else "Platform is under Scheduled Maintenance",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isAr) "عذراً يا غالي، نقوم بأعمال تحسين وصيانة لتقديم مظهر أفضل وأوقات تواصل أسرع. سنعود للعمل قريباً جداً!" 
                                   else "We are upgrading our databases to improve speed and coordinate connections better. See you soon!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Standard Screen Selector Router
                    when (currentScreenState) {
                        "login_panel" -> {
                            OwnerPanel(onExitPanel = { currentScreenState = "home" })
                        }
                        "join_panel" -> {
                            JoinScreen(onSuccessDismiss = { currentScreenState = "home" })
                        }
                        "chat_list" -> {
                            ChatRoomsScreen(
                                onRoomSelected = { roomId ->
                                    activeChatRoomId = roomId
                                    currentScreenState = "chat_active"
                                },
                                onExit = { currentScreenState = "home" }
                            )
                        }
                        "chat_active" -> {
                            ActiveChatScreen(
                                roomId = activeChatRoomId ?: "",
                                onBack = { currentScreenState = "chat_list" }
                            )
                        }
                        else -> {
                            // MAIN HOME EXPERIENCE
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Loyalty program points & direct shares banner
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0x33FFD700)),
                                    border = BorderStroke(1.dp, primaryThemeColor.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Stars,
                                                contentDescription = "Stars points logo",
                                                tint = primaryThemeColor,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = if (isAr) "رصيدك: $userPoints نقطة ولاء 🌟" else "Balance: $userPoints loyalty points 🌟",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = if (isAr) "اكتب مراجعة (+15) أو شارك التطبيق (+25) لكسب الامتيازات" else "Review providers to secure additional points!",
                                                    fontSize = 10.sp,
                                                    color = Color.LightGray
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                FirestoreSim.awardLoyaltyPoints(25)
                                                val intent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_TEXT, "حمل الآن تطبيق دليلك للكوادر والخدمات وتواصل مع أمهر الفنيين في بلادنا اليمن! الرابط: https://maher736.com/services")
                                                }
                                                context.startActivity(Intent.createChooser(intent, "مشاركة التطبيق"))
                                                Toast.makeText(context, "شكراً لدعمك ومشاركتك التطبيق! تم منحك 25 نقطة ولاء إضافية! 🎉", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryThemeColor),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text(if (isAr) "شارك واكسب 🔗" else "Share 🔗", fontSize = 10.sp, color = Color.Black)
                                        }
                                    }
                                }

                                // Interactive Paid Rolling Banners (Promotions)
                                if (banners.isNotEmpty()) {
                                    val currentBanner = banners.getOrNull(activeBannerIndex)
                                    if (currentBanner != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color(0xFF1E293B))
                                                .border(1.dp, Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Campaign,
                                                    contentDescription = "Promotion Campaign",
                                                    tint = primaryThemeColor,
                                                    modifier = Modifier.size(36.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = currentBanner.title,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.weight(1f)
                                                )
                                            }
                                        }
                                    }
                                }

                                // Filter and search utilities layout
                                Column(modifier = Modifier.padding(12.dp)) {
                                    // Main search bar input with simulated Voice Search Integration!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = if (voiceSearchActive) voiceSearchQueryInput else searchKeyword,
                                            onValueChange = { 
                                                searchKeyword = it 
                                                voiceSearchActive = false
                                            },
                                            placeholder = { Text(if (isAr) "ابحث باسم المهني، هاتفه أو منطقته..." else "Search by name, specialty, or phone...") },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("home_search_input"),
                                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = primaryThemeColor) },
                                            trailingIcon = {
                                                IconButton(onClick = {
                                                    voiceSearchActive = true
                                                    voiceSearchQueryInput = "منظومات شمسية"
                                                    searchKeyword = "منظومات شمسية"
                                                    Toast.makeText(context, "شريط محاكاة البحث الصوتي: جاري تحليل اللكنة اليمنية... تم كنس كتابة 'منظومات شمسية'! 🎙️", Toast.LENGTH_LONG).show()
                                                }) {
                                                    Icon(Icons.Default.Mic, contentDescription = "Voice search", tint = if (voiceSearchActive) Color.Green else Color.White)
                                                }
                                            },
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White,
                                                focusedBorderColor = primaryThemeColor
                                            )
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Advanced Radius & Governorate Filtering Widget
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // City selector
                                        Box(modifier = Modifier.weight(1f)) {
                                            var expandedCity by remember { mutableStateOf(false) }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                                    .clickable { expandedCity = true }
                                                    .padding(12.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(text = "المحافظة: $selectedCityFilter", color = Color.White, fontSize = 12.sp)
                                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.LightGray)
                                            }
                                            DropdownMenu(
                                                expanded = expandedCity,
                                                onDismissRequest = { expandedCity = false },
                                                modifier = Modifier.background(Color(0xFF1E293B))
                                            ) {
                                                listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "حضرموت").forEach { city ->
                                                    DropdownMenuItem(
                                                        text = { Text(city, color = Color.White) },
                                                        onClick = {
                                                            selectedCityFilter = city
                                                            expandedCity = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Radius search distance slider
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "المسافة الجغرافية: ${selectedRadiusFilter.toInt()} كم 📍",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = primaryThemeColor,
                                                fontSize = 11.sp
                                            )
                                            Slider(
                                                value = selectedRadiusFilter,
                                                onValueChange = { selectedRadiusFilter = it },
                                                valueRange = 1f..50f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = primaryThemeColor,
                                                    activeTrackColor = primaryThemeColor
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Horizontal categories row filter widgets
                                    Box {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            // 'All' card
                                            Card(
                                                modifier = Modifier.clickable { selectedCategoryFilter = "all" },
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (selectedCategoryFilter == "all") primaryThemeColor else Color(0xFF1E293B)
                                                )
                                            ) {
                                                Text(
                                                    text = if (isAr) "الكل 🌍" else "All Categories",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                    color = if (selectedCategoryFilter == "all") Color.Black else Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            categories.forEach { cat ->
                                                Card(
                                                    modifier = Modifier.clickable { selectedCategoryFilter = cat.id },
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (selectedCategoryFilter == cat.id) primaryThemeColor else Color(0xFF1E293B)
                                                    )
                                                ) {
                                                    Text(
                                                        text = "${cat.icon} " + (if (isAr) cat.name else cat.nameEn),
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        color = if (selectedCategoryFilter == cat.id) Color.Black else Color.White,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Interactive GPS pin maps coordinates widget
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                                    border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                                ) {
                                    Box(modifier = Modifier.fillMaxSize()) {
                                        // Fake high-contrast digital coordinates map canvas
                                        Canvas(modifier = Modifier.fillMaxSize()) {
                                            // Screen layout circles representing radius
                                            drawCircle(
                                                color = Color.LightGray.copy(alpha = 0.08f),
                                                radius = 160f,
                                                center = center,
                                                style = Stroke(width = 2f)
                                            )
                                            drawCircle(
                                                color = primaryThemeColor.copy(alpha = 0.15f),
                                                radius = 280f,
                                                center = center,
                                                style = Stroke(width = 2f)
                                            )

                                            // Draw center point representing Client user
                                            drawCircle(color = Color(0xFF3B82F6), radius = 10f, center = center)
                                        }

                                        // Map UI text labels overlay
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Map, contentDescription = null, tint = primaryThemeColor, modifier = Modifier.size(14.dp))
                                                Text("خريطة المواقع الجغرافية (Pins Map Simulator)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                                            }
                                            Text("تظهر نقط زرقاء تمثل أصحاب المهن الأقرب لعنوان ومحاط طوقك الحالي بالأقمار الصناعية.", color = Color.Gray, fontSize = 9.sp)
                                        }

                                        // Dynamically draw provider locator pins
                                        providers.forEach { prov ->
                                            if (!prov.isBlocked) {
                                                Box(
                                                    modifier = Modifier
                                                        .offset(
                                                            x = (40 + (prov.lng - 44.20) * 1200).dp,
                                                            y = (50 + (prov.lat - 15.34) * 1200).dp
                                                        )
                                                        .clickable {
                                                            selectedProviderForDetail = prov
                                                        }
                                                        .clip(CircleShape)
                                                        .background(primaryThemeColor)
                                                        .padding(4.dp)
                                                ) {
                                                    Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color.Black, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                // RECONSTRUCTING PROVIDERS LIST FOR HOME PAGE
                                val filteredProviders = providers.filter { prov ->
                                    val matchBlock = !prov.isBlocked
                                    val matchKey = if (searchKeyword.isBlank()) true else {
                                        prov.name.contains(searchKeyword, ignoreCase = true) ||
                                        prov.title.contains(searchKeyword, ignoreCase = true) ||
                                        prov.phone.contains(searchKeyword, ignoreCase = true) ||
                                        prov.region.contains(searchKeyword, ignoreCase = true)
                                    }
                                    val matchCat = if (selectedCategoryFilter == "all") true else {
                                        prov.mainCategoryId == selectedCategoryFilter
                                    }
                                    val matchCity = if (selectedCityFilter == "الكل") true else {
                                        prov.city == selectedCityFilter
                                    }

                                    // Simple pseudo radius filtering checking diff (representing coordinate distance verification)
                                    val pseudoDistance = Math.sqrt(
                                        Math.pow((prov.lat - 15.348) * 111, 2.0) +
                                        Math.pow((prov.lng - 44.204) * 111, 2.0)
                                    )
                                    val matchRadius = pseudoDistance <= selectedRadiusFilter

                                    matchBlock && matchKey && matchCat && matchCity && matchRadius
                                }

                                // Sort pinned options to standard top flow!
                                val finalSortedProviders = filteredProviders.sortedWith(
                                    compareByDescending<Provider> { it.isPinned }
                                        .thenByDescending { it.hasVipBadge }
                                        .thenByDescending { it.averageRating }
                                )

                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                        .padding(horizontal = 12.dp)
                                ) {
                                    item {
                                        Text(
                                            text = if (isAr) "مقدمي الخدمات الموصى بهم وعروض النخبة ⭐" else "Recommended list elite",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = primaryThemeColor,
                                            modifier = Modifier.padding(vertical = 6.dp)
                                        )

                                        // Horizontal scroll of recommended elite providers
                                        val recProviders = providers.filter { it.isRecommended && !it.isBlocked }
                                        if (recProviders.isEmpty()) {
                                            Text("سيتم إدراج مقدمي نخبة موصى بهم من قبل المالك قريباً 🌟", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 12.dp))
                                        } else {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .horizontalScroll(rememberScrollState())
                                                    .padding(bottom = 12.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                recProviders.forEach { rp ->
                                                    Card(
                                                        modifier = Modifier
                                                            .width(180.dp)
                                                            .clickable { selectedProviderForDetail = rp },
                                                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                                        border = BorderStroke(1.dp, primaryThemeColor.copy(alpha = 0.5f))
                                                    ) {
                                                        Column(modifier = Modifier.padding(10.dp)) {
                                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                                Text(text = rp.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                                                Icon(Icons.Default.Stars, contentDescription = null, tint = primaryThemeColor, modifier = Modifier.size(14.dp))
                                                            }
                                                            Text(text = rp.subCategoryId, fontSize = 10.sp, color = Color.LightGray)
                                                            Text(text = "⭐ ${rp.averageRating}", fontSize = 10.sp, color = Color.Yellow)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    item {
                                        Text(
                                            text = if (isAr) "الدليل العام للكوادر والمهنيين (${finalSortedProviders.size} مطابق)" else "General catalogs",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }

                                    if (finalSortedProviders.isEmpty()) {
                                        item {
                                            Text(
                                                text = "لم يتم العثور على أي نتائج مطابقة لبحثك في الأطواق القريبة، يرجى توسيع شريط المسافة الجغرافية أو تبديل السجل! 🗺️",
                                                textAlign = TextAlign.Center,
                                                color = Color.Gray,
                                                fontSize = 12.sp,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(24.dp)
                                            )
                                        }
                                    } else {
                                        items(finalSortedProviders) { prov ->
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 6.dp)
                                                    .clickable { selectedProviderForDetail = prov }
                                                    .testTag("provider_item_card"),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = if (prov.isPinned) Color(0x33FFD700) else Color(0xFF1E293B)
                                                ),
                                                border = BorderStroke(
                                                    width = 1.dp,
                                                    color = if (prov.isPinned) primaryThemeColor else Color.Gray.copy(alpha = 0.1f)
                                                )
                                            ) {
                                                Column(modifier = Modifier.padding(14.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = prov.name,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 15.sp,
                                                                color = Color.White
                                                            )
                                                            if (prov.isVerified) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Icon(
                                                                    imageVector = Icons.Default.Verified,
                                                                    contentDescription = "Verified Icon",
                                                                    tint = Color(0xFF3B82F6),
                                                                    modifier = Modifier.size(16.dp)
                                                                )
                                                            }
                                                            if (prov.hasVipBadge) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text("VIP", color = Color.Red, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.background(primaryThemeColor).padding(horizontal = 3.dp))
                                                            }
                                                        }

                                                        // Pinned ribbon tag
                                                        if (prov.isPinned) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(Icons.Default.PushPin, contentDescription = null, tint = primaryThemeColor, modifier = Modifier.size(14.dp))
                                                                Text("مُثبت", color = primaryThemeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }

                                                    Text(text = prov.title, fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(vertical = 2.dp))
                                                    Text(text = "📍 " + prov.city + " - " + prov.region, fontSize = 11.sp, color = Color.Gray)

                                                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.Gray.copy(alpha = 0.2f))

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(16.dp))
                                                            Text(
                                                                text = " ${prov.averageRating} (${prov.reviewCount} مراجعة)",
                                                                fontSize = 12.sp,
                                                                color = Color.White
                                                            )
                                                        }

                                                        Text(
                                                            text = prov.phone,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 13.sp,
                                                            color = primaryThemeColor
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // PROVIDER DETAIL MODAL POPUP
    if (selectedProviderForDetail != null) {
        val sp = selectedProviderForDetail!!
        
        // Filter reviews for this provider
        val provReviews = reviews.filter { it.providerId == sp.id }

        AlertDialog(
            onDismissRequest = { selectedProviderForDetail = null },
            containerColor = Color(0xFF1E293B),
            modifier = Modifier.fillMaxWidth(0.95f),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = sp.name, color = primaryThemeColor, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        if (sp.isVerified) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp).padding(start = 4.dp))
                        }
                    }
                    IconButton(onClick = { selectedProviderForDetail = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "المهنة واللقب: " + sp.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(text = "رقم الهاتف الفعال: " + sp.phone, color = primaryThemeColor, fontWeight = FontWeight.Bold)
                    Text(text = "العنوان: " + sp.city + " / " + sp.region, color = Color.LightGray)
                    
                    Text(text = "السيرة المهنية ودليل الأعمال:", style = MaterialTheme.typography.bodySmall, color = primaryThemeColor)
                    Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A)).padding(10.dp).clip(RoundedCornerShape(8.dp))) {
                        Text(text = sp.bio, color = Color.White, fontSize = 12.sp)
                    }

                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))

                    // Reviews / Stars Average score logic
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "مراجعات الفكاهة والتقييمات العام (⭐ ${sp.averageRating})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Button(
                            onClick = { showReviewPostDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryThemeColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("+ تقييم", fontSize = 10.sp, color = Color.Black)
                        }
                    }

                    if (provReviews.isEmpty()) {
                        Text("لا توجد تعليقات نصية مكتوبة لحساب هذا المهني بعد. كن الأول وقيمه!", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        provReviews.forEach { rev ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A))
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(6.dp))
                            ) {
                                Column {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = "• " + rev.authorName, fontWeight = FontWeight.Bold, color = primaryThemeColor, fontSize = 11.sp)
                                        Text(text = "⭐ " + rev.rating, color = Color.Yellow, fontSize = 11.sp)
                                    }
                                    Text(text = rev.content, fontSize = 11.sp, color = Color.White, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Reports utility triggers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Report Code button
                        IconButton(onClick = { showReportPostDialog = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Report, contentDescription = "Report", tint = Color.Red)
                                Text("إبلاغ عن محتوى 🚨", color = Color.Red, fontSize = 10.sp, modifier = Modifier.padding(start = 2.dp))
                            }
                        }

                        // Message direct chat trigger
                        Button(
                            onClick = {
                                val rid = FirestoreSim.openOrCreateChatRoom(sp.id, sp.name)
                                activeChatRoomId = rid
                                selectedProviderForDetail = null
                                currentScreenState = "chat_active"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تواصل دردشة متاح 💬", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Review Post dialog constructor
    if (showReviewPostDialog && selectedProviderForDetail != null) {
        val sp = selectedProviderForDetail!!
        var revAuthor by remember { mutableStateOf("") }
        var revStars by remember { mutableStateOf(5) }
        var revContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReviewPostDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("إرسال تقييم وخمس نجوم للمهني ⭐", color = primaryThemeColor) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = revAuthor, onValueChange = { revAuthor = it }, label = { Text("اسمك الكامل") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    
                    Text("أعطه نجوم تقييم (1-5):", fontSize = 12.sp, color = Color.White)
                    Row {
                        (1..5).forEach { ns ->
                            IconButton(onClick = { revStars = ns }) {
                                Icon(
                                    imageVector = if (ns <= revStars) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (ns <= revStars) Color.Yellow else Color.Gray
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = revContent,
                        onValueChange = { revContent = it },
                        label = { Text("اكتب مراجعة تفصيلية للأعمال") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (revAuthor.isNotBlank() && revContent.isNotBlank()) {
                        FirestoreSim.addReview(sp.id, revAuthor, revStars, revContent)
                        showReviewPostDialog = false
                        Toast.makeText(context, "تم تسليم تقييمك ومصداقيتك! كسبت 15 نقطة ولاء إضافية! 🎉", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("تسليم المراجعة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewPostDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Report Post dialog constructor
    if (showReportPostDialog && selectedProviderForDetail != null) {
        val sp = selectedProviderForDetail!!
        var repReporter by remember { mutableStateOf("") }
        var repReason by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportPostDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("الإبلاغ عن مخالفة ورفع للمالك 🚨", color = Color.Red) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = repReporter, onValueChange = { repReporter = it }, label = { Text("اسمك الكامل للتحقق") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = repReason, onValueChange = { repReason = it }, label = { Text("سبب البلاغ المفصل") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (repReporter.isNotBlank() && repReason.isNotBlank()) {
                            FirestoreSim.submitReport(sp.id, sp.name, repReporter, repReason)
                            showReportPostDialog = false
                            Toast.makeText(context, "تم رفع شكواك لمالك التطبيق آلياً بكل سرية وأمان! 🛡️", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("تأكيد البلاغ", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportPostDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}

// CHAT ROOMS LIST SCREEN
@Composable
fun ChatRoomsScreen(onRoomSelected: (String) -> Unit, onExit: () -> Unit) {
    val rooms by FirestoreSim.chatRooms.collectAsState()
    val isAr = FirestoreSim.currentLang.collectAsState().value == "ar"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (isAr) "صندوق الدردشات النشطة 💬" else "Active Chats Inbox 💬", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            IconButton(onClick = { onExit() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }

        if (rooms.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("لا توجد محادثات جارية حالياً. تواصل مع الكوادر اليمنية للاستفسار والطلب!", color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn {
                items(rooms) { r ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onRoomSelected(r.id) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = r.providerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text(text = r.lastMessage, color = Color.LightGray, fontSize = 11.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ACTIVE CHAT SCREEN
@Composable
fun ActiveChatScreen(roomId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val rooms by FirestoreSim.chatRooms.collectAsState()
    val allMessages by FirestoreSim.messages.collectAsState()
    val room = rooms.find { it.id == roomId }

    val isAr = FirestoreSim.currentLang.collectAsState().value == "ar"

    var inputMsgField by remember { mutableStateOf("") }
    val roomMessages = allMessages.filter { it.chatRoomId == roomId }

    if (room == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("المحادثة المطلوبة غير متوفرة أو معطلة.", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(text = room.providerName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        Text(text = if (isAr) "مسؤول الدعم متصل بالخادم" else "Responsive advisor online", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }
            }

            // Message logs
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(roomMessages) { m ->
                    val isOwn = m.senderId == FirestoreSim.currentUserId
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isOwn) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isOwn) Color(0xFF3B82F6) else Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = m.content, color = Color.White, fontSize = 12.sp)
                                if (m.photoUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("[صورة مرفقة - معاينة دقة الغطاء 📷]", fontSize = 10.sp, color = Color.LightGray)
                                }
                                if (m.audioUrl.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("[مذكرة صوتية - تشغيل بانتظام 🎤]", fontSize = 10.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }
            }

            // Chat input utilities (Supports Simulated Image capture & Audio records attachments)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Audio Attachment Button
                IconButton(onClick = {
                    FirestoreSim.sendMessage(roomId, "أرسل مذكرة صوتية مسجلة 🎤", context, audioUrl = "sim_voice.mp3")
                    Toast.makeText(context, "تم إرسال تسجيل مذكرتك الصوتية للطرف الآخر بنجاح! 🎤", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice note", tint = Color.White)
                }

                // Camera File Attachment Button
                IconButton(onClick = {
                    FirestoreSim.sendMessage(roomId, "أرسل صورة قياسات وأبعاد 📷", context, photoUrl = "sim_image.jpg")
                    Toast.makeText(context, "تم تصوير الأبعاد وتحميل الصورة الآمنة للدردشة! 📸", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Standard Chat Text Field Custom Input
                OutlinedTextField(
                    value = inputMsgField,
                    onValueChange = { inputMsgField = it },
                    placeholder = { Text(if (isAr) "اكتب رسالة استفسار..." else "Write enquiry...") },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.width(6.dp))

                // Submit Send Button
                IconButton(
                    onClick = {
                        if (inputMsgField.isNotBlank()) {
                            FirestoreSim.sendMessage(roomId, inputMsgField, context)
                            inputMsgField = ""
                        }
                    },
                    modifier = Modifier.background(Color(0xFF3B82F6), CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send check", tint = Color.White)
                }
            }
        }
    }
}
