package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.AlertRed
import com.example.ui.theme.getSelectedTextColor

private fun safeParseColor(hex: String, fallback: Color): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        fallback
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    currentSession: String, // "none", "moderator", "owner"
    onNavigate: (screen: String) -> Unit,
    onBackdoorUnlock: () -> Unit
) {
    val context = LocalContext.current
    val appColors = FirestoreSim.appColors.collectAsState()
    val appConfigs = FirestoreSim.appConfigs.collectAsState()
    val writeTextColor = getSelectedTextColor(appColors.value.textColorName)

    // Database states
    val mainCats = FirestoreSim.mainCategories.collectAsState().value
    val subCats = FirestoreSim.subCategories.collectAsState().value
    val providers = FirestoreSim.serviceProviders.collectAsState().value
    val reviews = FirestoreSim.reviews.collectAsState().value
    val banners = FirestoreSim.banners.collectAsState().value
    val promotedAds = FirestoreSim.promotedAds.collectAsState().value
    val cities = FirestoreSim.cities.collectAsState().value
    val favorites = FirestoreSim.favorites.collectAsState().value
    val contactHistory = FirestoreSim.contactHistory.collectAsState().value

    // User Dashboard dialogue state
    var showDashboardDialog by remember { mutableStateOf(false) }

    // Language Toggle State: False = Arabic (Default), True = English
    var isEnglishLanguage by remember { mutableStateOf(false) }

    // Click counter on Home button for Backdoor Trigger
    var homeClickCount by remember { mutableStateOf(0) }
    var lastHomeClickTime by remember { mutableStateOf(0L) }

    // UI interactive states
    var searchQuery by remember { mutableStateOf("") }
    var selectedCityFilter by remember { mutableStateOf("") }
    var selectedDistrictFilter by remember { mutableStateOf("") }
    var filterExpanded by remember { mutableStateOf(false) }

    // Browsing Category path selection
    var selectedMainCategoryPath by remember { mutableStateOf<MainCategory?>(null) }
    var selectedSubCategoryTab by remember { mutableStateOf<SubCategory?>(null) }

    // Detailed service provider sheet
    var activeProviderDetailSheet by remember { mutableStateOf<ServiceProvider?>(null) }
    
    // Rating / complaint dialog toggles
    var providerToRateDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var ratingStarsSelected by remember { mutableStateOf(5) }
    var ratingCommentText by remember { mutableStateOf("") }
    var raterNameInput by remember { mutableStateOf("") }

    var providerToReportDialog by remember { mutableStateOf<ServiceProvider?>(null) }
    var reportCommentText by remember { mutableStateOf("") }
    var reporterNameInput by remember { mutableStateOf("") }

    // Assistant Dialogue toggle
    var isAssistantChatOpen by remember { mutableStateOf(false) }
    var isContactOptionsOpen by remember { mutableStateOf(false) }
    var isAdminChatOpen by remember { mutableStateOf(false) }

    // Voice search simulator toggle
    var isVoiceRecordingActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ====================================================================================
        // Custom Styled NO-BOTTOM NO-TOP App Bar (Top layout right below System Notch status)
        // Header Arrangement: 🏠 🔐 👤 🌐 🔄 (From Right-To-Left or Left-To-Right appropriately)
        // ====================================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Group 1: Title and sync notification
            Column {
                Text(
                    text = appConfigs.value.appName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

            }

            // Group 2: The Core 5 Header Navigation Buttons (RTL ordered functionally)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // ICON 1: 🏠 Home Browse Screen (Triggers backdoor if clicked 5 times consecutively)
                IconButton(
                    onClick = {
                        val currTime = System.currentTimeMillis()
                        if (currTime - lastHomeClickTime < 1000) {
                            homeClickCount++
                        } else {
                            homeClickCount = 1
                        }
                        lastHomeClickTime = currTime

                        if (homeClickCount >= 5) {
                            homeClickCount = 0
                            onBackdoorUnlock() // Unlock hidden owner password backdoor instantly!
                        } else {
                            // Go back to main directory browse node
                            selectedMainCategoryPath = null
                            selectedSubCategoryTab = null
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .background(
                            if (selectedMainCategoryPath == null) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "الرئيسية",
                        tint = if (selectedMainCategoryPath == null) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // ICON Dashboard: 📱 User Dashboard (Favorites & Contacts)
                IconButton(
                    onClick = {
                        showDashboardDialog = true
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "المفضلة والمكالمات",
                        tint = Color(0xFFFFD700)
                    )
                }

                // ICON 2: 🔐 Login / Access Dashboard (No logout loop bug!)
                IconButton(
                    onClick = {
                        when (currentSession) {
                            "moderator" -> onNavigate("admin")
                            "owner" -> onNavigate("owner")
                            else -> onNavigate("login")
                        }
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (currentSession != "none") Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                        contentDescription = "تسجيل الدخول",
                        tint = if (currentSession != "none") MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // ICON 3: 👤 Join/Enlist (Create Service Provider join request)
                IconButton(
                    onClick = { onNavigate("join") },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = "تسجيل حرفي",
                        tint = Color.White
                    )
                }

                // ICON 4: 🌐 Swap Language (Arabic / English Layout flip)
                IconButton(
                    onClick = {
                        isEnglishLanguage = !isEnglishLanguage
                        Toast.makeText(
                            context,
                            if (isEnglishLanguage) "Swapped Layout to English!" else "تم إرجاع واجهة التطبيق للعربية!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "تبديل اللغة",
                        tint = if (isEnglishLanguage) MaterialTheme.colorScheme.primary else Color.White
                    )
                }

                // ICON 5: 🔄 Instant cloud sync manual update (Refresh)
                IconButton(
                    onClick = {
                        Toast.makeText(context, "جاري تحزيم لقطات Snapshot للمصفوفات... تمت المزامنة!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "تحديث البيانات",
                        tint = Color.White
                    )
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

        // Main Scrolling Core Area
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (selectedMainCategoryPath == null) {
                // ==========================================
                // HOME BROWSE VIEW (أقسام التطبيق الرئيسية)
                // ==========================================
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Welcome announcement
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Celebration, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                                Column {
                                    Text("أهلاً بك في الدليل الخدمي", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                    Text(appConfigs.value.welcomeMessage, fontSize = 11.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }

                    // Dynamic Carousel Marketing Banners
                    if (banners.isNotEmpty()) {
                        item {
                            val activeBanner = banners.first() // Rotates or picks prominent
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.surface,
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                                )
                                            )
                                        )
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.align(Alignment.CenterStart),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = activeBanner.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = activeBanner.description,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(isEnglishLanguage.let { "إعلان معتمد" }, fontSize = 8.sp, color = Color.Black, modifier = Modifier.padding(2.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Golden recommended Slider ("موصى بهم ⭐")
                    val recommendedList = providers.filter { it.isRecommended }
                    if (recommendedList.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = RatingGold, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "مقدمو الخدمة الموصى بهم من دليلك ⭐",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                }

                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(recommendedList) { p ->
                                        Card(
                                            modifier = Modifier
                                                .width(170.dp)
                                                .clickable { activeProviderDetailSheet = p },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, RatingGold.copy(alpha = 0.4f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Default.AccountBox, contentDescription = null, tint = RatingGold)
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(12.dp))
                                                        Text(String.format("%.1f", p.averageRating), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp, maxLines = 1)
                                                Text(p.district, fontSize = 10.sp, color = Color.Gray, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Advanced Search Bar & Filters Trigger
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = { Text("ابحث بالاسم، المهنة، الهاتف...") },
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                                    )

                                    // Voice search button
                                    IconButton(
                                        onClick = {
                                            isVoiceRecordingActive = true
                                            searchQuery = "ماهر طاهر كهربائي" // Simulating voice filling search text!
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    ) {
                                        Icon(Icons.Default.Mic, contentDescription = "بحث صوتي", tint = Color.Black)
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(onClick = { filterExpanded = !filterExpanded }) {
                                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("فلاتر النطاق الجغرافي المعقدة")
                                    }

                                    if (searchQuery.isNotBlank() || selectedCityFilter.isNotBlank() || selectedDistrictFilter.isNotBlank()) {
                                        IconButton(onClick = {
                                            searchQuery = ""
                                            selectedCityFilter = ""
                                            selectedDistrictFilter = ""
                                        }) {
                                            Icon(Icons.Default.Clear, contentDescription = "كلير فلاتر", tint = AlertRed)
                                        }
                                    }
                                }

                                // Geographic Filter Drops
                                AnimatedVisibility(visible = filterExpanded) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("اختر نطاق المحافظة:", fontSize = 11.sp, color = Color.Gray)
                                        Row(
                                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            cities.forEach { city ->
                                                FilterChip(
                                                    selected = selectedCityFilter == city.name,
                                                    onClick = {
                                                        selectedCityFilter = city.name
                                                        selectedDistrictFilter = "" // reset district
                                                    },
                                                    label = { Text(city.name) }
                                                )
                                            }
                                        }

                                        // District filters
                                        val activeCityObj = cities.find { it.name == selectedCityFilter }
                                        if (activeCityObj != null) {
                                            Text("اختر مديريتك السكنية:", fontSize = 11.sp, color = Color.Gray)
                                            Row(
                                                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                activeCityObj.districts.forEach { dist ->
                                                    FilterChip(
                                                        selected = selectedDistrictFilter == dist,
                                                        onClick = { selectedDistrictFilter = dist },
                                                        label = { Text(dist) }
                                                    )
                                                }
                                            }
                                        }

                                        // Radius search visual slider
                                        var maxRadiusInput by remember { mutableStateOf(15f) }
                                        Text("نطاق البحث الجغرافي بالرادار: ${maxRadiusInput.toInt()} كم", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Slider(
                                            value = maxRadiusInput,
                                            onValueChange = { maxRadiusInput = it },
                                            valueRange = 5f..50f
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Promoted sponsored static banner (الأخبار الممولة)
                    val mainPromotedAd = promotedAds.firstOrNull()
                    if (mainPromotedAd != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                                Text("إشهار ممول", fontSize = 8.sp, color = Color.Black, modifier = Modifier.padding(2.dp))
                                            }
                                            Text(mainPromotedAd.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(mainPromotedAd.content, fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                                    }
                                    IconButton(
                                        onClick = {
                                            // Make contact direct
                                            Toast.makeText(context, "جاري إعداد محول المكالمة الفوري للإعلان الممورّق!", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.PhoneCallback, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Header category list title
                    item {
                        Text(
                            text = "تصفح الفنيين والخدمات المتكاملة 🛠️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    // Main Categories GRID Layout
                    // Show our categories
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.height(290.dp) // fits perfectly
                        ) {
                            items(mainCats.sortedBy { it.order }) { cat ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp)
                                        .clickable {
                                            selectedMainCategoryPath = cat
                                            // select first child category under this parent
                                            selectedSubCategoryTab = subCats.find { it.parentId == cat.id }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp).fillMaxSize(),
                                        verticalArrangement = Arrangement.Center,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size((appConfigs.value.categoryIconSize + 20f).dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = getIconForCode(cat.iconCode),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(appConfigs.value.categoryIconSize.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                                        val subCount = subCats.filter { it.parentId == cat.id }.size
                                        Text("تضم: $subCount مهن فرعية", fontSize = 9.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // ==========================================
                // SUB-CATEGORY BROWSE LIST (عرض الفنيين بداخل الأقسام)
                // ==========================================
                val activeMain = selectedMainCategoryPath!!
                val siblingSubCats = subCats.filter { it.parentId == activeMain.id }

                Column(modifier = Modifier.fillMaxSize()) {
                    // Category Selection Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = {
                                selectedMainCategoryPath = null
                                selectedSubCategoryTab = null
                            }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "عودة", tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(activeMain.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }

                        Text("${providers.filter { it.mainCategoryId == activeMain.id }.size} مهني متاح", fontSize = 11.sp, color = Color.Gray)
                    }

                    // Sibling child categories horizontal scroll bar selector
                    if (siblingSubCats.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            siblingSubCats.forEach { sub ->
                                val isSelected = selectedSubCategoryTab?.id == sub.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSubCategoryTab = sub },
                                    label = { Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.Black
                                    )
                                )
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

                    // Approved active service providers list
                    val renderedProviders = providers.filter {
                        it.mainCategoryId == activeMain.id &&
                                (selectedSubCategoryTab == null || it.subCategoryId == selectedSubCategoryTab!!.id) &&
                                (searchQuery.isBlank() || it.name.contains(searchQuery) || it.phone.contains(searchQuery) || it.address.contains(searchQuery)) &&
                                (selectedCityFilter.isBlank() || it.district.contains(selectedDistrictFilter.ifBlank { selectedCityFilter }))
                    }.sortedByDescending { it.isPinned } // Pinned providers appear at the very first highlighted!

                    if (renderedProviders.isEmpty()) {
                        EmptyStateView(
                            title = "دليل الخدمات فارغ حالياً",
                            subtitle = "لا يوجد أخصائيين مسجلين في هذا القطاع الفرعي لمدينتك المفلترة بعد."
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(renderedProviders) { p ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { activeProviderDetailSheet = p },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (p.isPinned) {
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    ),
                                    border = if (p.isPinned) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                }
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                                        if (p.isSubscribed) {
                                                            Icon(Icons.Default.Verified, contentDescription = "مشترك مميز", tint = Color(0xFF3897F0), modifier = Modifier.size(14.dp))
                                                        }
                                                        if (p.isPinned) {
                                                            Icon(Icons.Default.PushPin, contentDescription = "كادر مثبت", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                    Text(p.district, fontSize = 11.sp, color = Color.Gray)
                                                }
                                            }

                                            // Rating sum
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Icon(Icons.Default.Star, contentDescription = null, tint = RatingGold, modifier = Modifier.size(14.dp))
                                                Text(
                                                    text = if (p.ratingCount > 0) String.format("%.1f", p.averageRating) else "5.0",
                                                    color = Color.White,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text("(${p.ratingCount})", fontSize = 10.sp, color = Color.Gray)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text("العنوان: ${p.address}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                                        Text("الهاتف: ${p.phone}", fontSize = 12.sp, color = Color.LightGray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ====================================================================================
        // Clean Footer Promo Banner with info icon and AI Assistant Float Options
        // ====================================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .navigationBarsPadding()
                .padding(vertical = 12.dp, horizontal = 16.dp)
        ) {
            // LEFT FOOTER COMPONENT: ℹ️ About the app and helpful developer info button + Floating Contact Button
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.clickable { onNavigate("about") },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = "معلومات عن الصفحة", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Text("المعلومات", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }

                // FLOATING CONTACT BUTTON (أيقونة التواصل العائمة) - بجوار أيقونة المعلومات
                if (appConfigs.value.showFloatingContact) {
                    val contactBaseColor = safeParseColor(appConfigs.value.floatingContactColorHex, Color(0xFF25D366))
                    Box(
                        modifier = Modifier
                            .offset(x = appConfigs.value.floatingContactOffsetX.dp, y = appConfigs.value.floatingContactOffsetY.dp)
                            .size(appConfigs.value.floatingContactSize.dp)
                            .alpha(appConfigs.value.floatingContactOpacity)
                            .background(contactBaseColor, CircleShape)
                            .clickable { isContactOptionsOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "اتصال مباشر",
                            tint = Color.White,
                            modifier = Modifier.size((appConfigs.value.floatingContactSize * 0.5f).dp)
                        )
                    }
                }
            }

            // MIDDLE FOOTER COMPONENT: Custom Admin Promo WAM Badge
            if (appConfigs.value.showPromoFooter) {
                Text(
                    text = appConfigs.value.footerPromoText,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center
                )
            }

            // RIGHT FOOTER COMPONENT: The circular smart assistant float button with user configs
            val assistantBaseColor = safeParseColor(appConfigs.value.assistantColorHex, MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = appConfigs.value.assistantOffsetX.dp, y = appConfigs.value.assistantOffsetY.dp)
                    .alpha(appConfigs.value.assistantOpacity)
                    .clickable { isAssistantChatOpen = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("المساعد", fontSize = 12.sp, color = assistantBaseColor, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(appConfigs.value.smartAssistantSize.dp)
                        .background(assistantBaseColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "خدمات",
                        color = Color.Black,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Modal sheet: Service Provider Details Workspace
    activeProviderDetailSheet?.let { p ->
        val providerReviews = reviews.filter { it.providerId == p.id }

        Dialog(onDismissRequest = { activeProviderDetailSheet = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ملف الفني والتقييمات", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { activeProviderDetailSheet = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // Bio Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                                if (p.isSubscribed) {
                                    Icon(Icons.Default.Verified, contentDescription = "ممتاز", tint = Color(0xFF3897F0), modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(p.district, fontSize = 12.sp, color = Color.Gray)
                        }
                        val isFav = favorites.contains(p.id)
                        IconButton(
                            onClick = {
                                FirestoreSim.toggleFavorite(context, p.id)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "تفضيل",
                                tint = if (isFav) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Contact action cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                FirestoreSim.addContactLog(context, p.id, "اتصال هاتفي")
                                Toast.makeText(context, "جاري فتح مباشر تطبيق الاتصال للهاتف: ${p.phone}", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال هاتفي")
                        }

                        Button(
                            onClick = {
                                FirestoreSim.addContactLog(context, p.id, "واتساب")
                                Toast.makeText(context, "جاري تحويلك لمحادثة واتساب آمنة مع المهني!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("واتساب الدليل", color = Color.White)
                        }
                    }

                    Divider(color = Color.DarkGray)

                    // Details block
                    DetailItemLabel(title = "عنوان ومقر العمل الفعلي", content = p.address)
                    DetailItemLabel(title = "نقاط GPS المسجلة", content = p.gpsCoordinates ?: "غير مسجلة")

                    // Reports and abuse buttons
                    Button(
                        onClick = { providerToReportDialog = p },
                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed.copy(alpha = 0.15f), contentColor = AlertRed),
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, AlertRed),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Report, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الإبلاغ عن المقدّم أو المشتكى")
                    }

                    Divider(color = Color.DarkGray)

                    // Reviews section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("آراء وتقييمات العملاء الموثقة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                        Button(
                            onClick = { providerToRateDialog = p },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("تقييم المهني")
                        }
                    }

                    if (providerReviews.isEmpty()) {
                        Text("لا توجد مراجعات مكتوبة على هذا الملف بعد. كن أول من يكتب!", fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    } else {
                        providerReviews.forEach { r ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(r.userName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Row {
                                            (1..5).forEach { starIdx ->
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = if (starIdx <= r.rating) RatingGold else Color.Gray,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(r.comment, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog rating/review service provider (+ Loyalty loyalty rewards point increase!)
    providerToRateDialog?.let { p ->
        Dialog(onDismissRequest = { providerToRateDialog = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة تقييم لـ: ${p.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    
                    OutlinedTextField(
                        value = raterNameInput,
                        onValueChange = { raterNameInput = it },
                        label = { Text("اسمك الموقر") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    // Stars slider selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..5).forEach { index ->
                            Icon(
                                imageVector = if (index <= ratingStarsSelected) Icons.Default.Star else Icons.Outlined.Star,
                                contentDescription = null,
                                tint = RatingGold,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { ratingStarsSelected = index }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = ratingCommentText,
                        onValueChange = { ratingCommentText = it },
                        label = { Text("رأيك بخدمات الفني وتقييمك للأمانة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(onClick = { providerToRateDialog = null }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (raterNameInput.isNotBlank() && ratingCommentText.isNotBlank()) {
                                    val r = Review(
                                        providerId = p.id,
                                        userName = raterNameInput,
                                        rating = ratingStarsSelected.toFloat(),
                                        comment = ratingCommentText
                                    )
                                    FirestoreSim.addReview(context, r)

                                    // Add loyalty points
                                    FirestoreSim.addLoyaltyPoints(context, 100) // 100 loyalty points awarded!
                                    Toast.makeText(context, "تم إرسال مراجعتك بنجاح! كسبت 100 نقطة ولاء إضافية لدعمك التطوير!", Toast.LENGTH_LONG).show()

                                    providerToRateDialog = null
                                    ratingCommentText = ""
                                    raterNameInput = ""
                                } else {
                                    Toast.makeText(context, "يرجى كتابة الاسم والتعليق قبل الإرسال", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إرسال وكسب نقاط")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Abuse report dialogue
    providerToReportDialog?.let { p ->
        Dialog(onDismissRequest = { providerToReportDialog = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("رفع شكوى ضد المهني: ${p.name}", fontWeight = FontWeight.Bold, color = AlertRed, fontSize = 16.sp)

                    OutlinedTextField(
                        value = reporterNameInput,
                        onValueChange = { reporterNameInput = it },
                        label = { Text("اسمك الموفر للتحقق") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = reportCommentText,
                        onValueChange = { reportCommentText = it },
                        label = { Text("اكتب تفاصيل المخالفة والأسباب بالتفصيل") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { providerToReportDialog = null }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (reporterNameInput.isNotBlank() && reportCommentText.isNotBlank()) {
                                    val logReport = Report(
                                        providerId = p.id,
                                        userName = reporterNameInput,
                                        comment = reportCommentText
                                    )
                                    FirestoreSim.addReport(context, logReport)
                                    Toast.makeText(context, "تم استلام تقرير البلاغ بنجاح وتوثيقه بسجلات المشرفين للمراجعة التلقائية!", Toast.LENGTH_LONG).show()

                                    providerToReportDialog = null
                                    reportCommentText = ""
                                    reporterNameInput = ""
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة الاسم وتفاصيل الشكوى أولاً", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إرسال الشكوى")
                        }
                    }
                }
            }
        }
    }

    // Speech Simulator Dialog overlay
    if (isVoiceRecordingActive) {
        Dialog(onDismissRequest = { isVoiceRecordingActive = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
                modifier = Modifier.fillMaxWidth().height(220.dp).padding(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("جاري الاستماع للبحث الصوتي الفوري...", fontWeight = FontWeight.Bold, color = Color.White)
                    Text("تفوه باسم المهني أو تخصص الخدمة السكنية", color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        (1..8).forEach { _ ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height((10..40).random().dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(onClick = {
                        isVoiceRecordingActive = false
                        Toast.makeText(context, "تم التقاط الجملة الصوتية بنجاح!", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("تم الإملاء")
                    }
                }
            }
        }
    }

    // Opens Smart Assistant chat
    if (isAssistantChatOpen) {
        AssistantDialog(onDismiss = { isAssistantChatOpen = false })
    }

    // Direct Admin/Provider Chat Dialog
    if (isAdminChatOpen) {
        var chatInputText by remember { mutableStateOf("") }
        val chatMessages = FirestoreSim.chatMessages.collectAsState().value

        Dialog(onDismissRequest = { isAdminChatOpen = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("دردشة الدعم والمهن 💬", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                        IconButton(onClick = { isAdminChatOpen = false }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                        }
                    }
                    
                    Text(
                        "تواصل آمن ومباشر مع عملاء وإدارة الدليل لطرح الشكاوى، طلب الدعم، أو توثيق العضوية الفنية لعام 2026.",
                        fontSize = 11.sp,
                        color = Color.LightGray,
                        lineHeight = 16.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                    // Chat message bubbles
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        androidx.compose.foundation.lazy.LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chatMessages) { chatMsg ->
                                val isMe = chatMsg.sender == "provider"
                                val bubbleBg = if (isMe) MaterialTheme.colorScheme.primary else Color(0xFF2C2C2E)
                                val bubbleContentColor = if (isMe) MaterialTheme.colorScheme.onPrimary else Color.White
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = bubbleBg),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.widthIn(max = 240.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = if (isMe) "أنت" else "الإدارة الذكية 🛡️",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                color = bubbleContentColor.copy(alpha = 0.7f)
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = chatMsg.message,
                                                fontSize = 13.sp,
                                                color = bubbleContentColor,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Compose chat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = chatInputText,
                            onValueChange = { chatInputText = it },
                            placeholder = { Text("اكتب رسالتك للإدارة هنا...") },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = writeTextColor,
                                unfocusedTextColor = writeTextColor
                            ),
                            singleLine = true
                        )
                        Button(
                            onClick = {
                                if (chatInputText.isNotBlank()) {
                                    FirestoreSim.sendChatMessage(
                                        context,
                                        ChatMessage(
                                            id = "msg_${System.currentTimeMillis()}",
                                            providerId = "direct_user",
                                            sender = "provider",
                                            message = chatInputText,
                                            timestamp = System.currentTimeMillis()
                                        )
                                    )
                                    chatInputText = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("إرسال", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Floating Contact selection Dialog (Support menu)
    if (isContactOptionsOpen) {
        val configs = appConfigs.value
        Dialog(onDismissRequest = { isContactOptionsOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("التواصل المباشر مع الدعم والإدارة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text("يسعدنا خدمتك والرد على استفسارك طوال 24 ساعة!", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // 1. Phone call option
                    Button(
                        onClick = {
                            isContactOptionsOpen = false
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                    data = android.net.Uri.parse("tel:${configs.supportPhone}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "جاري طلب الرقم المباشر: ${configs.supportPhone}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("اتصال مباشر بالهاتف (${configs.supportPhone})", fontWeight = FontWeight.Bold)
                    }

                    // 2. WhatsApp support option
                    Button(
                        onClick = {
                            isContactOptionsOpen = false
                            try {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                    data = android.net.Uri.parse("https://api.whatsapp.com/send?phone=${configs.supportWhatsApp}")
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "رابط واتساب الدعم: ${configs.supportWhatsApp}", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))
                    ) {
                        Text("مراسلة واتساب الدعم المباشر (2 hour response)", fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    // 3. Admin direct chat option
                    OutlinedButton(
                        onClick = {
                            isContactOptionsOpen = false
                            isAdminChatOpen = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("محادثة فورية (شات المهن والدعم مع الإدارة)", color = MaterialTheme.colorScheme.primary)
                    }

                    OutlinedButton(
                        onClick = { isContactOptionsOpen = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إغلاق القائمة")
                    }
                }
            }
        }
    }

    // Opens User Dashboard (M3 Dialog)
    if (showDashboardDialog) {
        val configs = appConfigs.value
        Dialog(onDismissRequest = { showDashboardDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D1B20)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Dashboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Text("لوحتك لخدماتك المفضلة وسجلاتك 📱", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                        }
                        IconButton(onClick = { showDashboardDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // Custom Welcome Message (set/configured by Admin!)
                    if (configs.dashboardCustomMessage.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        ) {
                            Text(
                                text = configs.dashboardCustomMessage,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(10.dp),
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Content Area with Scrolling
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Determine layout order based on admin custom config!
                        val favsBlock = @Composable {
                            if (configs.showDashboardFavorites) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Text("الخدمات والمهنيين والمحلات المفضلة (${favorites.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    }

                                    val faveProviders = providers.filter { favorites.contains(it.id) }
                                    if (faveProviders.isEmpty()) {
                                        Text("لا توجد خدمات في مذكرتك المفضلة حالياً. تفضل بتصفح الأقسام واضغط على علامة النجمة (⭐) لحفظها هنا للرجوع الفوري الفائق!", color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
                                    } else {
                                        faveProviders.forEach { p ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        activeProviderDetailSheet = p
                                                        showDashboardDialog = false
                                                    }
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                        Text(p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                                        if (p.isSubscribed) {
                                                            Icon(Icons.Default.Verified, contentDescription = null, tint = Color(0xFF3897F0), modifier = Modifier.size(12.dp))
                                                        }
                                                    }
                                                    Text("الحرفة: ${p.address} | الهاتف: ${p.phone}", color = Color.Gray, fontSize = 10.sp)
                                                }
                                                IconButton(
                                                    onClick = { FirestoreSim.toggleFavorite(context, p.id) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "إزالة", tint = AlertRed, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        val historyBlock = @Composable {
                            if (configs.showDashboardCallHistory) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Call, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Text("سجل ومذكرات التواصل التاريخية (${contactHistory.size})", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        }
                                        if (contactHistory.isNotEmpty()) {
                                            TextButton(
                                                onClick = {
                                                    FirestoreSim.clearContactHistory(context)
                                                    Toast.makeText(context, "تم تصفير وتطهير سجل التواصل بنجاح!", Toast.LENGTH_SHORT).show()
                                                },
                                                contentPadding = PaddingValues(0.dp)
                                            ) {
                                                Text("تفريغ السجل", color = AlertRed, fontSize = 10.sp)
                                            }
                                        }
                                    }

                                    if (contactHistory.isEmpty()) {
                                        Text("سجل تواصلك خالٍ بالكامل في هذا الوقت. أي اتصال تجريه بالضغط على زر (اتصال) أو (واتساب) سيقيد تلقائياً وسرياً هنا مع التوقيت المزدوج!", color = Color.Gray, fontSize = 11.sp, lineHeight = 16.sp)
                                    } else {
                                        contactHistory.sortedByDescending { it.timestamp }.forEach { log ->
                                            val p = providers.find { it.id == log.providerId }
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        if (p != null) {
                                                            activeProviderDetailSheet = p
                                                            showDashboardDialog = false
                                                        }
                                                    }
                                                    .padding(10.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(p?.name ?: "مهني غير مسجل حالياً", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                                    Text("وسيلة الاتصال: ${log.mode} | هاتف: ${p?.phone ?: "غير متوفر"}", color = Color.LightGray, fontSize = 10.sp)
                                                    Text("تفاصيل التوقيت: ${log.timestamp}", color = Color.Gray, fontSize = 9.sp)
                                                }
                                                Icon(
                                                    imageVector = if (log.mode == "واتساب") Icons.Default.ChatBubble else Icons.Default.Call,
                                                    contentDescription = null,
                                                    tint = if (log.mode == "واتساب") Color(0xFF25D366) else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Order switching
                        if (configs.dashboardFavoritesFirst) {
                            favsBlock()
                            Spacer(modifier = Modifier.height(8.dp))
                            historyBlock()
                        } else {
                            historyBlock()
                            Spacer(modifier = Modifier.height(8.dp))
                            favsBlock()
                        }
                    }
                }
            }
        }
    }
}

private val RatingGold = Color(0xFFFFB000)
private val SoftGray = Color(0xFF8E8E93)
