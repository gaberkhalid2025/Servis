@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfigs
import com.example.data.ChatMessage
import com.example.data.FirestoreSim
import com.example.data.Provider
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val providers by FirestoreSim.providers.collectAsState()
    val reviews by FirestoreSim.reviews.collectAsState()
    val chatRooms by FirestoreSim.chatRooms.collectAsState()
    val currentMessages by FirestoreSim.messages.collectAsState()
    val configs by FirestoreSim.configs.collectAsState()

    // Screen navigation
    var currentScreen by remember { mutableStateOf("explore") } // explore, join, owner
    var selectedProviderId by remember { mutableStateOf<String?>(null) }
    var activeChatRoomId by remember { mutableStateOf<String?>(null) }

    // User authentication status and Guest Mode tracker
    var isGuestMode by remember { mutableStateOf(true) }
    var showGuestBlockDialog by remember { mutableStateOf(false) }

    // Guest registration states
    var guestRegisterName by remember { mutableStateOf("") }
    var guestRegisterPhone by remember { mutableStateOf("") }

    // Search and filter states
    var searchQuery by remember { mutableStateOf("") }
    var activeCategoryFilter by remember { mutableStateOf("all") }
    var filterClosestOnly by remember { mutableStateOf(false) }

    // Client self GPS coordinates simulation (باب اليمن, صنعاء)
    val clientLat = 15.348
    val clientLng = 44.204

    // Floating assistant controls
    var showAssistantChat by remember { mutableStateOf(false) }
    var assistantMessages by remember { mutableStateOf(listOf(
        ChatMessage("ast1", "ast", "assistant", "المساعد الذكي للبوابة 🤖", "مرحباً بك في دليلك للخدمات باليمن! كيف يمكنني مساعدتك في استخراج أفضل عروض الكهرباء والتمديدات أو مراجعة أعمالك؟ 🇾🇪", System.currentTimeMillis())
    )) }
    var assistantInputText by remember { mutableStateOf("") }

    // Floating icon offset state, which admin can configure OR user can drag dynamically
    var bubbleOffsetX by remember { mutableStateOf(configs.bubbleXOffset) }
    var bubbleOffsetY by remember { mutableStateOf(configs.bubbleYOffset) }

    // Update state when configs change
    LaunchedEffect(configs) {
        bubbleOffsetX = configs.bubbleXOffset
        bubbleOffsetY = configs.bubbleYOffset
    }

    Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
        when (screen) {
            "join" -> JoinScreen(onNavigateBack = { currentScreen = "explore" })
            "owner" -> OwnerPanel(onNavigateBack = { currentScreen = "explore" })
            "explore" -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sticky Header Toolbar
                        ExploreHeader(
                            isGuest = isGuestMode,
                            userName = FirestoreSim.currentUserName,
                            onSwitchToAdmin = { currentScreen = "owner" },
                            onSwitchToJoin = { currentScreen = "join" },
                            onLogOutOrIn = {
                                if (isGuestMode) {
                                    showGuestBlockDialog = true
                                } else {
                                    // Log out
                                    FirestoreSim.currentUserId = "guest"
                                    FirestoreSim.currentUserName = "زائر كريم"
                                    isGuestMode = true
                                    Toast.makeText(context, "تم تسجيل الخروج. تصفح الآن بصفتك ضيفاً 🔎", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )

                        // Main scrolling contents
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Unified Category Pills Row
                            CategorySelectorRow(
                                activeFilter = activeCategoryFilter,
                                onFilterChange = { activeCategoryFilter = it }
                            )

                            // Quick Map / Geographical component showing proximity pins
                            Text(
                                text = "خريطة الموفرين والورش القريبة منك 📍",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                            Text(
                                text = "انقر على العلامة لمعاينة المسافة ومعلومات الفني الفورية.",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                            )

                            // Map Box container
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .padding(horizontal = 16.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF1E293B))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            ) {
                                YemeniMapCanvas(
                                    providers = providers,
                                    clientLat = clientLat,
                                    clientLng = clientLng,
                                    activeCategory = activeCategoryFilter,
                                    highlightClosestOnly = filterClosestOnly,
                                    onProviderSelected = { prov ->
                                        selectedProviderId = prov.id
                                    }
                                )

                                // Real-time coordinates toggle button on top right of map
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { filterClosestOnly = !filterClosestOnly },
                                        shape = RoundedCornerShape(30.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (filterClosestOnly) MaterialTheme.colorScheme.primary else Color(0xFF475569)
                                        ),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Color.White)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(if (filterClosestOnly) "عرض الكل" else "أقرب تمديد متاح 📍", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Listings Search input
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { searchQuery = "" }) {
                                            Icon(Icons.Default.Clear, null, tint = Color.Gray)
                                        }
                                    }
                                },
                                placeholder = { Text("ابحث بالمنطقة أو التخصص اليمني (مثال: صنعاء)...") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Provider Listings header
                            Text(
                                text = "مقدمو الخدمات الحرة والورش الفنية المتاحة:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
                            )

                            // Filtering and calculating closest items distance lists
                            val filteredProviders = providers.filter { prov ->
                                val matchCat = activeCategoryFilter == "all" || prov.mainCategoryId == activeCategoryFilter
                                val matchQuery = prov.name.contains(searchQuery, true) ||
                                        prov.title.contains(searchQuery, true) ||
                                        prov.region.contains(searchQuery, true) ||
                                        prov.bio.contains(searchQuery, true)

                                val matchesClosestConstraint = if (filterClosestOnly) {
                                    // Calculate 3 closest providers
                                    val top3Ids = providers
                                        .map { p -> p.id to calculateDistance(clientLat, clientLng, p.lat, p.lng) }
                                        .sortedBy { it.second }
                                        .take(3)
                                        .map { it.first }
                                    top3Ids.contains(prov.id)
                                } else {
                                    true
                                }

                                matchCat && matchQuery && matchesClosestConstraint && !prov.isBlocked
                            }

                            if (filteredProviders.isEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = Color.Gray)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("عذراً، لم نعثر على أي مقدمي خدمات يطابقون خياراتك حالياً.", color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
                                }
                            } else {
                                // Render providers lists, sorted with PINNED (مثبت في الصدارة) first!
                                val sortedComp = filteredProviders.sortedByDescending { it.isPinned }
                                sortedComp.forEach { provider ->
                                    val distance = calculateDistance(clientLat, clientLng, provider.lat, provider.lng)
                                    ProviderListItemCard(
                                        provider = provider,
                                        distance = distance,
                                        onClick = { selectedProviderId = provider.id }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }

                    // Floating Controls Container obeying offsets and sizes specified by standard admin customizers
                    if (configs.smartAssistantVisible) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 16.dp, end = 16.dp),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset {
                                        IntOffset(
                                            bubbleOffsetX.roundToInt(),
                                            bubbleOffsetY.roundToInt()
                                        )
                                    }
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            bubbleOffsetX += dragAmount.x
                                            bubbleOffsetY += dragAmount.y
                                        }
                                    }
                                    .clip(CircleShape)
                                    .size(configs.bubbleSize.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .clickable { showAssistantChat = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LiveHelp,
                                    contentDescription = "Contact Assistant Or Admin",
                                    tint = Color.White,
                                    modifier = Modifier.size((configs.bubbleSize * 0.55f).dp)
                                )
                            }
                        }
                    }

                    // Sliding Floating Assistant Dialogue Box
                    if (showAssistantChat) {
                        FloatingAssistantPanel(
                            messages = assistantMessages,
                            inputText = assistantInputText,
                            onInputChange = { assistantInputText = it },
                            onClose = { showAssistantChat = false },
                            onSendMessage = {
                                if (assistantInputText.trim().isEmpty()) return@FloatingAssistantPanel
                                val userMsg = ChatMessage("ast_u_${System.currentTimeMillis()}", "ast", "user", "أنت", assistantInputText, System.currentTimeMillis())
                                assistantMessages = assistantMessages + userMsg
                                val userText = assistantInputText
                                assistantInputText = ""

                                // Fake automatic replies from smart helper bot
                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                    val botReply = when {
                                        userText.contains("كهرباء", true) || userText.contains("طاقة", true) -> "لقد سجلنا طلبك. المهندس عادل الوادعي هو الأقرب إليك ويملك حالياً شارة التوصية والتوثيق المعتمد بالصدارة ⚡."
                                        userText.contains("سباك", true) || userText.contains("ماء", true) -> "أنصحك بالتواصل مع الفني محمد الصبري، فهو متاح لتمديد ورش الأنابيب داخل صنعاء القديمة."
                                        userText.contains("سعر", true) || userText.contains("بكم", true) -> "أسعار الفنيين والخدمات حرة وتفاوضية وتعتمد على الاتفاق المباشر بالدردشة الحرة فوراً."
                                        else -> "مرحباً بك! يمكنك التحدث معنا وسنقوم بربطك بأبرز الفنيين والآدمن مباشرة لحل طلبك وتأكيد الفواتير."
                                    }
                                    val replyMsg = ChatMessage("ast_b_${System.currentTimeMillis()}", "ast", "assistant", "المساعد الذكي لبوابة اليمن 🤖", botReply, System.currentTimeMillis())
                                    assistantMessages = assistantMessages + replyMsg
                                }, 1200)
                            }
                        )
                    }

                    // Expansive Bottom Sheet / Pop-up for the Selected Provider
                    selectedProviderId?.let { id ->
                        val provider = providers.find { it.id == id }
                        if (provider != null) {
                            val providerReviews = reviews.filter { it.providerId == provider.id }
                            ProviderDetailModal(
                                provider = provider,
                                reviewsList = providerReviews,
                                isGuest = isGuestMode,
                                onDismiss = { selectedProviderId = null },
                                onChatClicked = {
                                    if (isGuestMode) {
                                        showGuestBlockDialog = true
                                    } else {
                                        selectedProviderId = null
                                        activeChatRoomId = FirestoreSim.openOrCreateChatRoom(provider.id, provider.name)
                                    }
                                },
                                onAddReviewSubmitted = { rating, text ->
                                    if (isGuestMode) {
                                        showGuestBlockDialog = true
                                    } else {
                                        FirestoreSim.addReview(provider.id, FirestoreSim.currentUserName, rating, text)
                                        Toast.makeText(context, "نشكرك، تم تدوين وقيد تقييمك ومراجعتك للخدمات بنجاح!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }

                    // Active Chat Room Transcript Overlay Dialog
                    activeChatRoomId?.let { roomId ->
                        val room = chatRooms.find { it.id == roomId }
                        if (room != null) {
                            val roomMessages = currentMessages.filter { it.chatRoomId == room.id }
                            ChatRoomOverlayDialog(
                                roomName = room.providerName,
                                messages = roomMessages,
                                onDismiss = { activeChatRoomId = null },
                                onSendMessage = { text, photoUrl ->
                                    FirestoreSim.sendMessage(room.id, text, context, photoUrl)
                                }
                            )
                        }
                    }

                    // Guest Registration dialogue when trying to perform user constraints actions
                    if (showGuestBlockDialog) {
                        AlertDialog(
                            onDismissRequest = { showGuestBlockDialog = false },
                            title = { Text("تسجيل الدخول / الانضمام السريع 🤝", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                            text = {
                                Column {
                                    Text(
                                        text = "عذراً يا طيب! وضع التصفح كضيف يسمح لك بالاستعراض فقط. للدردشة المباشرة ورفع المرفقات أو تقييم مقدمي الخدمات، يرجى كتابة اسمك وهاتفك للتفعيل الفوري:",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    OutlinedTextField(
                                        value = guestRegisterName,
                                        onValueChange = { guestRegisterName = it },
                                        label = { Text("الاسم الكريم") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                                    )

                                    OutlinedTextField(
                                        value = guestRegisterPhone,
                                        onValueChange = { guestRegisterPhone = it },
                                        label = { Text("رقم هاتفك اليمني للتواصل") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (guestRegisterName.trim().isEmpty() || guestRegisterPhone.trim().isEmpty()) {
                                            Toast.makeText(context, "يرجى ملء الاسم والحقل لتسجيل الحساب الموثق", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        FirestoreSim.currentUserId = "user_${System.currentTimeMillis()}"
                                        FirestoreSim.currentUserName = guestRegisterName
                                        FirestoreSim.currentUserPhone = guestRegisterPhone
                                        isGuestMode = false
                                        showGuestBlockDialog = false
                                        Toast.makeText(context, "مرحباً بك $guestRegisterName! تم تفعيل هويتك بنجاح للدردشة والتقييم 🎉", Toast.LENGTH_LONG).show()
                                    }
                                ) {
                                    Text("تفعيل الحساب والولوج 🚀")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showGuestBlockDialog = false }) {
                                    Text("بقاء كضيف 👁️", color = Color.LightGray)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreHeader(
    isGuest: Boolean,
    userName: String,
    onSwitchToAdmin: () -> Unit,
    onSwitchToJoin: () -> Unit,
    onLogOutOrIn: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "دليلك لكل الخدمات 🇾🇪",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(
                        text = if (isGuest) "وضع التصفح كضيف الحالي 👁️" else "مرحباً بك: $userName ✅",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Admin Gateway Button
                    IconButton(
                        onClick = onSwitchToAdmin,
                        modifier = Modifier.background(Color(0xFF374151), CircleShape)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Developer Options", tint = Color.White)
                    }

                    // Quick status Log switch
                    Button(
                        onClick = onLogOutOrIn,
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isGuest) MaterialTheme.colorScheme.primary else Color(0xFFD1D5DB)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (isGuest) "تسجيل الدخول" else "خروج",
                            color = if (isGuest) Color.White else Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub header quick links
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ابحث وتواصل وقيم في لحظات مع أمهر المهن المعتمدين.",
                    fontSize = 10.sp,
                    color = Color.LightGray,
                    modifier = Modifier.weight(1f)
                )

                TextButton(
                    onClick = onSwitchToJoin,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(Icons.Default.GroupAdd, null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("انضم كفني متاح 🛠️+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategorySelectorRow(
    activeFilter: String,
    onFilterChange: (String) -> Unit
) {
    val categories = listOf(
        "all" to "الكل 📋",
        "electricity" to "كهرباء ⚡",
        "plumbing" to "سباكة 🚰",
        "tech" to "برمجة وحاسوب 📱",
        "carpentry" to "نجارة ديكور 🪵",
        "mechanics" to "ميكانيك سيارات 🚗"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { (catId, catLabel) ->
            val isSelected = activeFilter == catId
            val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            Text(
                text = catLabel,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(bg)
                    .clickable { onFilterChange(catId) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
fun ProviderListItemCard(
    provider: Provider,
    distance: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Name + Badges
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = provider.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    if (provider.isVerified) {
                        Icon(
                            Icons.Default.Verified,
                            contentDescription = "Verified Provider",
                            tint = Color(0xFF00B2FF),
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    if (provider.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF7C2D12))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("مثبت صدارة 📍", color = Color(0xFFF97316), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (provider.isRecommended) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF065F46))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("موصى به ⭐", color = Color(0xFF34D399), fontSize = 7.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Proximity text distance calculated in real-time
                Text(
                    text = String.format("تبعد بـ %.1f كم 📍", distance),
                    color = Color.LightGray,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(provider.title, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(provider.bio, fontSize = 11.sp, color = Color.LightGray, maxLines = 2, overflow = TextOverflow.Ellipsis)

            Spacer(modifier = Modifier.height(10.dp))

            // Rating + Proximity summary footer block
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(provider.region, color = Color.Gray, fontSize = 11.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = RatingGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (provider.reviewCount == 0) "لا تقييم" else "${provider.averageRating} (${provider.reviewCount})",
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun YemeniMapCanvas(
    providers: List<Provider>,
    clientLat: Double,
    clientLng: Double,
    activeCategory: String,
    highlightClosestOnly: Boolean,
    onProviderSelected: (Provider) -> Unit
) {
    val distanceList = providers.map { prov ->
        prov to calculateDistance(clientLat, clientLng, prov.lat, prov.lng)
    }

    val top3Ids = if (highlightClosestOnly) {
        distanceList.sortedBy { it.second }.take(3).map { it.first.id }
    } else {
        emptyList()
    }

    // Capture Canvas Draw Scope to draw yemeni grid
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                // Click simulated on a random provider pin in active section
                val candidates = providers.filter {
                    val catMatches = activeCategory == "all" || it.mainCategoryId == activeCategory
                    val closestMatches = !highlightClosestOnly || top3Ids.contains(it.id)
                    catMatches && closestMatches
                }
                if (candidates.isNotEmpty()) {
                    onProviderSelected(candidates.random())
                }
            }
    ) {
        val w = size.width
        val h = size.height

        // Background terrain grids
        drawRect(color = Color(0xFF0F172A))

        // Draw simulated Yemen streets
        for (i in 1..8) {
            val offsetProgress = i * (w / 9f)
            // Longitude roads
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.4f),
                start = Offset(offsetProgress, 0f),
                end = Offset(offsetProgress + 30f, h),
                strokeWidth = 3f
            )
            // Latitude roads
            val offsetProgressY = i * (h / 9f)
            drawLine(
                color = Color(0xFF334155).copy(alpha = 0.4f),
                start = Offset(0f, offsetProgressY),
                end = Offset(w, offsetProgressY + 15f),
                strokeWidth = 3f
            )
        }

        // Draw central residential sector "باب اليمن الكبير" with light circle
        drawCircle(
            color = Color(0xFF3B82F6).copy(alpha = 0.08f),
            center = Offset(w * 0.5f, h * 0.5f),
            radius = w * 0.28f
        )

        // Draw Client center (Green pulsing radar circle)
        val clientX = w * 0.5f
        val clientY = h * 0.5f
        drawCircle(color = Color(0xFF10B981).copy(alpha = 0.4f), center = Offset(clientX, clientY), radius = 16f)
        drawCircle(color = Color.White, center = Offset(clientX, clientY), radius = 6f)

        // Plot pins for providers dynamically
        providers.forEachIndexed { idx, prov ->
            // Skip blocked providers
            if (prov.isBlocked) return@forEachIndexed

            // Calculate matching constraints
            val catMatches = activeCategory == "all" || prov.mainCategoryId == activeCategory
            val closestMatches = !highlightClosestOnly || top3Ids.contains(prov.id)

            if (catMatches && closestMatches) {
                // Place pins relative offset based on coordinate differences
                val diffX = (prov.lng - clientLng) * 2800f
                val diffY = (prov.lat - clientLat) * 2800f

                val pinX = (w * 0.5f) + diffX.toFloat()
                val pinY = (h * 0.5f) - diffY.toFloat()

                // Constraint safe viewport bounds
                val drawX = pinX.coerceIn(40f, w - 40f)
                val drawY = pinY.coerceIn(40f, h - 40f)

                // Assign Pin colors according to primary classifications
                val pinColor = when (prov.mainCategoryId) {
                    "electricity" -> Color(0xFFEAB308) // electricity
                    "plumbing" -> Color(0xFF10B981)    // plumbing
                    "tech" -> Color(0xFF06B6D4)        // computer
                    "carpentry" -> Color(0xFFF97316)   // carpentry
                    else -> Color(0xFFD946EF)
                }

                // Draw highlighted ring if provider is Pinned
                if (prov.isPinned) {
                    drawCircle(color = Color.Red.copy(alpha = 0.3f), center = Offset(drawX, drawY), radius = 14f)
                }

                // Draw solid pin
                drawCircle(color = pinColor, center = Offset(drawX, drawY), radius = 9f)
                drawCircle(color = Color.White, center = Offset(drawX, drawY), radius = 3.5f)
            }
        }
    }
}

@Composable
fun FloatingAssistantPanel(
    messages: List<ChatMessage>,
    inputText: String,
    onInputChange: (String) -> Unit,
    onClose: () -> Unit,
    onSendMessage: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Panel Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LiveHelp, null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("المساعد الحواري والتقني السريع 🇾🇪", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            // Scroll of responses
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.size) { index ->
                    val msg = messages[index]
                    val isBot = msg.senderId == "assistant"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isBot) Alignment.Start else Alignment.End
                    ) {
                        Text(msg.senderName, fontSize = 9.sp, color = Color.Gray)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isBot) Color(0xFF374151) else MaterialTheme.colorScheme.primary
                                )
                                .padding(10.dp)
                        ) {
                            Text(msg.content, color = Color.White, fontSize = 11.sp, textAlign = TextAlign.Start)
                        }
                    }
                }
            }

            // Form inputs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text("اكتب سؤالك هنا للفائدة الفورية...", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = onSendMessage,
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun ProviderDetailModal(
    provider: Provider,
    reviewsList: List<com.example.data.Review>,
    isGuest: Boolean,
    onDismiss: () -> Unit,
    onChatClicked: () -> Unit,
    onAddReviewSubmitted: (Int, String) -> Unit
) {
    var ratingStars by remember { mutableStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .clickable(enabled = false) {} // block dismiss on click inside
                .padding(top = 16.dp),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Drag handle bar
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.Gray)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Detail Top bar containing verification and dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = provider.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            if (provider.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, null, tint = Color(0xFF1E90FF), modifier = Modifier.size(20.dp))
                            }
                        }
                        Text(provider.title, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(provider.region, fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.LightGray)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.DarkGray)

                // Detailed Bio Box
                Text("نبذة مهنية معتمدة للعمل", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Text(provider.bio, fontSize = 12.sp, color = Color.White, lineHeight = 18.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contact Actions bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onChatClicked,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Chat, null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("الدردشة الفورية 💬", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            // Direct call trigger simulator
                            Toast.makeText(onChatClicked as? Context ?: onDismiss as? Context ?: onDismiss() as? Context, "جاري فتح خط الاتصال الهاتفي بالفني: ${provider.phone}", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.height(46.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Phone, null, tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Five Star reviews feed
                Text("آراء وتقييمات العملاء السابقين ⭐", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text("متوسط التقييم العام: ⭐ ${String.format("%.1f", provider.averageRating)}", fontWeight = FontWeight.Bold, color = RatingGold, fontSize = 13.sp)

                Spacer(modifier = Modifier.height(8.dp))

                if (reviewsList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد مراجعات نصية لهذا الموفر حتى الآن، كن أول المقيّمين!", color = Color.Gray, fontSize = 11.sp)
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        reviewsList.forEach { rev ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.reviewerName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.White)
                                        Row {
                                            for (i in 1..5) {
                                                Icon(
                                                    Icons.Default.Star,
                                                    null,
                                                    tint = if (rev.rating >= i) RatingGold else Color.DarkGray,
                                                    modifier = Modifier.size(10.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(rev.comment, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Interactive 5 stars input module
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("أضف مراجعة وتقييم للموفر الخبير:", fontSize = 11.sp, color = RatingGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Star selection layout
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            for (currentStar in 1..5) {
                                IconButton(
                                    modifier = Modifier.size(36.dp),
                                    onClick = { ratingStars = currentStar }
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = "Star $currentStar",
                                        tint = if (ratingStars >= currentStar) RatingGold else Color.DarkGray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = { Text("اكتب رأيك الصريح عن جودة المواعيد ونظافة التمديد هنا...", fontSize = 11.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                if (isGuest) {
                                    onAddReviewSubmitted(ratingStars, reviewText)
                                } else {
                                    if (reviewText.trim().isEmpty()) {
                                        Toast.makeText(onChatClicked as? Context ?: onDismiss as? Context, "الرجاء كتابة تعليق مناسب لوصف الفني أولاً", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    onAddReviewSubmitted(ratingStars, reviewText)
                                    reviewText = ""
                                }
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(containerColor = RatingGold),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إرسال التقييم ✍️", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomOverlayDialog(
    roomName: String,
    messages: List<com.example.data.ChatMessage>,
    onDismiss: () -> Unit,
    onSendMessage: (String, String?) -> Unit
) {
    val context = LocalContext.current
    var textMessage by remember { mutableStateOf("") }
    var simulatedAttachedPhoto by remember { mutableStateOf<String?>(null) }
    var isAttachingPhoto by remember { mutableStateOf(false) }

    // Media permissions and Picker simulators
    val galleryPickerSim: () -> Unit = {
        simulatedAttachedPhoto = "field_defect_measurement_${(1000..9999).random()}.png"
        Toast.makeText(context, "تم إرفاق صورة قياسات العطل من المعرض الموثق! 🏞️", Toast.LENGTH_LONG).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("غرفة دردشة: $roomName 💬", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, tint = Color.LightGray)
                }
            }
        },
        text = {
            Column(modifier = Modifier.width(320.dp)) {
                // Messages log scroll
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages.size) { mIdx ->
                        val mObj = messages[mIdx]
                        val isSelf = mObj.senderId == FirestoreSim.currentUserId
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isSelf) Alignment.End else Alignment.Start
                        ) {
                            Text(mObj.senderName, fontSize = 9.sp, color = Color.Gray)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelf) MaterialTheme.colorScheme.primary else Color(0xFF1E293B)
                                    )
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(mObj.content, color = Color.White, fontSize = 11.sp)
                                    if (mObj.imageAttachedUri != null) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Star, null, tint = RatingGold, modifier = Modifier.size(10.dp))
                                            Text("مرفق كاميرا: ${mObj.imageAttachedUri}", color = Color.Green, fontSize = 9.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Camera/Media Upload Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = galleryPickerSim,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.Attachment, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("رفع لقطة عطل 📷", fontSize = 10.sp)
                    }

                    Button(
                        onClick = {
                            // Direct camera capture simulator
                            isAttachingPhoto = true
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isAttachingPhoto = false
                                simulatedAttachedPhoto = "field_instant_snap_${(10..99).random()}.jpg"
                                Toast.makeText(context, "تم التقاط وإرفاق صورة فورية للعطل بنجاح ✅", Toast.LENGTH_SHORT).show()
                            }, 1000)
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("لقطة فورية", fontSize = 10.sp)
                    }
                }

                // Active attachment label
                simulatedAttachedPhoto?.let { photoName ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .background(Color(0xFF0F172A), RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("صورة سيلفي/عطل مرفقة: $photoName", fontSize = 9.sp, color = Color.Green)
                        IconButton(onClick = { simulatedAttachedPhoto = null }, modifier = Modifier.size(16.dp)) {
                            Icon(Icons.Default.Cancel, null, tint = Color.Red, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                if (isAttachingPhoto) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = { Text("اكتب رسالة فنية...", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (textMessage.trim().isEmpty() && simulatedAttachedPhoto == null) return@IconButton
                        onSendMessage(textMessage, simulatedAttachedPhoto)
                        textMessage = ""
                        simulatedAttachedPhoto = null
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                ) {
                    Icon(Icons.Default.Send, null, tint = Color.White)
                }
            }
        }
    )
}

// Distance helper between global coords points
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val dLat = lat2 - lat1
    val dLon = lon2 - lon1
    return sqrt(dLat * dLat + dLon * dLon) * 111.0 // 111km approx per degree
}
