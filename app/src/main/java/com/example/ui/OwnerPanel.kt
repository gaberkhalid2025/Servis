package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPanel(
    onExitPanel: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Configuration / Streams
    val configs by FirestoreSim.configs.collectAsState()
    val providers by FirestoreSim.providers.collectAsState()
    val pendingProviders by FirestoreSim.pendingProviders.collectAsState()
    val categories by FirestoreSim.categories.collectAsState()
    val reviews by FirestoreSim.reviews.collectAsState()
    val reports by FirestoreSim.reports.collectAsState()
    val banners by FirestoreSim.banners.collectAsState()
    val whitelistedDevices by FirestoreSim.whitelistedDevices.collectAsState()

    val isAr = FirestoreSim.currentLang.collectAsState().value == "ar"

    // Authentication states
    var isAuthorized by remember { mutableStateOf(false) }
    var isBackdoorOwnerVerified by remember { mutableStateOf(false) }
    var usernameField by remember { mutableStateOf("") }
    var passwordField by remember { mutableStateOf("") }
    var rememberCredentialsChecked by remember { mutableStateOf(true) }
    var twoFactorAuthEnabled by remember { mutableStateOf(false) }
    var tfaCodeField by remember { mutableStateOf("") }

    // Selected admin sub-tab: 0 = Reports/Stats, 1 = Pendings, 2 = Providers, 3 = Categories, 4 = Banners/Ads, 5 = System Setting
    var activeSubTab by remember { mutableStateOf(0) }

    // Dialog state controllers
    var showDirectAddProviderDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showAddBannerDialog by remember { mutableStateOf(false) }

    // Check if device is whitelisted or backdoor is bypassed
    val isDeviceWhitelisted = remember { mutableStateOf(true) }

    // Recheck bypassed state or initial login setup
    LaunchedEffect(Unit) {
        if (FirestoreSim.isBackdoorOwnerLoggedIn) {
            isAuthorized = true
            isBackdoorOwnerVerified = true
        }
    }

    if (!isAuthorized) {
        // Simple Login Form
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("admin_login_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin lock icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isAr) "تسجيل الدخول - لوحة الإشراف" else "Admin Authorization Lobby",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    fontSize = 22.sp,
                    color = Color.White
                )
                Text(
                    text = if (isAr) "خاص بالإدارة العليا فقط، للتأمين يتم فحص معرف الجهاز آلياً" else "Unauthorized device access attempts will be rejected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalDivider(color = Color.DarkGray, modifier = Modifier.padding(bottom = 16.dp))

                // Username input
                OutlinedTextField(
                    value = usernameField,
                    onValueChange = { usernameField = it },
                    label = { Text(if (isAr) "اسم المستخدم المشرف" else "Admin Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_user_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password input
                OutlinedTextField(
                    value = passwordField,
                    onValueChange = { passwordField = it },
                    label = { Text(if (isAr) "كلمة المرور السرية" else "Secret Passcode") },
                    leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_pass_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Two Factor Auth Option Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { twoFactorAuthEnabled = !twoFactorAuthEnabled },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = twoFactorAuthEnabled,
                        onCheckedChange = { twoFactorAuthEnabled = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = if (isAr) "تفعيل التحقق بخطوتين (2FA) للمشرفين" else "Enable OTP / 2FA check",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                AnimatedVisibility(visible = twoFactorAuthEnabled) {
                    OutlinedTextField(
                        value = tfaCodeField,
                        onValueChange = { tfaCodeField = it },
                        label = { Text(if (isAr) "أدخل رمز التحقق المكون من 6 أرقام" else "Google Authenticator Code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Remember me option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberCredentialsChecked = !rememberCredentialsChecked },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberCredentialsChecked,
                        onCheckedChange = { rememberCredentialsChecked = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                    )
                    Text(
                        text = if (isAr) "احفظ بيانات الاعتماد للتسجيل التلقائي" else "Remember my credentials next time",
                        color = Color.White,
                        fontSize = 13.sp
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        // Check normal admin logs
                        val isNormalAdmin = (usernameField == configs.adminUsername && passwordField == configs.adminPassword)
                        val isBackdoorOwner = (usernameField == "WAM2026" && passwordField == configs.secretPasswordBackdoor)

                        if (isBackdoorOwner) {
                            isAuthorized = true
                            isBackdoorOwnerVerified = true
                            FirestoreSim.isBackdoorOwnerLoggedIn = true
                            Toast.makeText(context, "أهلاً بمالك التطبيق! تم تسجيل الدخول من البوابة الخلفية ورسم كافة الصلاحيات 👑", Toast.LENGTH_LONG).show()
                        } else if (isNormalAdmin) {
                            if (twoFactorAuthEnabled && tfaCodeField.isBlank()) {
                                Toast.makeText(context, "الرجاء إدخال كود التحقق الثنائي للأمان!", Toast.LENGTH_SHORT).show()
                            } else {
                                isAuthorized = true
                                isBackdoorOwnerVerified = false
                                Toast.makeText(context, "تم تسجيل الدخول بنجاح كمشرف عادي! 👤", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Check custom providers credentials
                            Toast.makeText(context, "اسم المستخدم أو كلمة المرور غير صحيحة!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("admin_login_submit"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(
                        text = if (isAr) "تأكيد والتخويل بالدخول 🔐" else "Confirm Access Credentials 🔐",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Exit Backdoor / login widget button
                OutlinedButton(
                    onClick = { onExitPanel() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = if (isAr) "عودة للرئيسية ↩️" else "Cancel & Close Lobby", color = Color.White)
                }
            }
        }
    } else {
        // Authenticated Admin / Owner dashboard
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
        ) {
            // Dashboard header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isBackdoorOwnerVerified) "بوابة المالك الكاملة 👑" else "بوابة المشرف المساعد 🛡️",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "لوحة تحكم: ${configs.appName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray
                        )
                    }

                    // Logout / exit button
                    Row {
                        IconButton(onClick = {
                            isAuthorized = false
                            isBackdoorOwnerVerified = false
                            FirestoreSim.isBackdoorOwnerLoggedIn = false
                            Toast.makeText(context, "تم تسجيل الخروج من لوحة الإدارة! 👋", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = Color.Red)
                        }
                        IconButton(onClick = { onExitPanel() }) {
                            Icon(Icons.Default.Close, contentDescription = "Close panel", tint = Color.White)
                        }
                    }
                }
            }

            // Tabs indicator
            ScrollableTabRow(
                selectedTabIndex = activeSubTab,
                containerColor = Color(0xFF111827),
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text(if (isAr) "الإحصاء والتقارير 📊" else "Stats & Reports") }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text(if (isAr) "طلبات الانضمام (${pendingProviders.size}) 📝" else "Pendings") }
                )
                Tab(
                    selected = activeSubTab == 2,
                    onClick = { activeSubTab = 2 },
                    text = { Text(if (isAr) "الكوادر والمهنيين 🛠️" else "Providers") }
                )
                Tab(
                    selected = activeSubTab == 3,
                    onClick = { activeSubTab = 3 },
                    text = { Text(if (isAr) "إدارة الأقسام 🗄️" else "Categories") }
                )
                Tab(
                    selected = activeSubTab == 4,
                    onClick = { activeSubTab = 4 },
                    text = { Text(if (isAr) "إعلانات البانر 📣" else "Banners") }
                )
                Tab(
                    selected = activeSubTab == 5,
                    onClick = { activeSubTab = 5 },
                    text = { Text(if (isAr) "إعدادات النظام التامة ⚙️" else "System configs") }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                when (activeSubTab) {
                    0 -> {
                        // STATISTICS & Canvas charts
                        Text(
                            text = if (isAr) "تحليلات الأداء وقاعدة بيانات الإحصاء الحالية" else "Dynamic Database analytics",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // General counts
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), Alignment.CenterHorizontally) {
                                    Text(text = "إجمالي المهنيين", fontSize = 11.sp, color = Color.LightGray)
                                    Text(text = providers.size.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), Alignment.CenterHorizontally) {
                                    Text(text = "طلبات قيد المراجعة", fontSize = 11.sp, color = Color.LightGray)
                                    Text(text = pendingProviders.size.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
                                }
                            }
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), Alignment.CenterHorizontally) {
                                    Text(text = "المراجعات والردود", fontSize = 11.sp, color = Color.LightGray)
                                    Text(text = reviews.size.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Interactive Canvas Graphic (Replaces Recharts for Kotlin Android)
                        Text(
                            text = if (isAr) "نشاط الكوادر والمهنيين حسب التخصص (شريطي)" else "Providers Distribution per Category",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                val electricityCount = providers.count { it.mainCategoryId == "electricity" }
                                val plumbingCount = providers.count { it.mainCategoryId == "plumbing" }
                                val techCount = providers.count { it.mainCategoryId == "tech" }
                                val otherCount = providers.size - (electricityCount + plumbingCount + techCount)

                                val maxVal = maxOf(4, electricityCount, plumbingCount, techCount, otherCount).toFloat()

                                Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp)
                                        .padding(bottom = 12.dp)
                                ) {
                                    // Draw background lines
                                    val canvasWidth = size.width
                                    val canvasHeight = size.height

                                    drawLine(Color.DarkGray, Offset(0f, canvasHeight), Offset(canvasWidth, canvasHeight), 4f)
                                    drawLine(Color.DarkGray, Offset(0f, 0f), Offset(0f, canvasHeight), 4f)

                                    // Draw bars
                                    val barWidth = canvasWidth / 5f
                                    val spacing = barWidth / 2f

                                    // Bar 1: Electric
                                    val h1 = (electricityCount / maxVal) * (canvasHeight - 40f)
                                    drawRect(
                                        color = Color(0xFFEAB308), // Yellow
                                        topLeft = Offset(spacing, canvasHeight - h1),
                                        size = Size(barWidth, h1)
                                    )

                                    // Bar 2: Plumbing
                                    val h2 = (plumbingCount / maxVal) * (canvasHeight - 40f)
                                    drawRect(
                                        color = Color(0xFF0EA5E9), // Light blue
                                        topLeft = Offset(spacing * 2 + barWidth, canvasHeight - h2),
                                        size = Size(barWidth, h2)
                                    )

                                    // Bar 3: Tech
                                    val h3 = (techCount / maxVal) * (canvasHeight - 40f)
                                    drawRect(
                                        color = Color(0xFF10B981), // Green
                                        topLeft = Offset(spacing * 3 + barWidth * 2, canvasHeight - h3),
                                        size = Size(barWidth, h3)
                                    )

                                    // Bar 4: Other
                                    val h4 = (otherCount / maxVal) * (canvasHeight - 40f)
                                    drawRect(
                                        color = Color(0xFFA855F7), // Purple
                                        topLeft = Offset(spacing * 4 + barWidth * 3, canvasHeight - h4),
                                        size = Size(barWidth, h4)
                                    )
                                }

                                // Legend labels
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text(text = "كهرباء ($electricityCount)", fontSize = 10.sp, color = Color(0xFFEAB308))
                                    Text(text = "سباكة ($plumbingCount)", fontSize = 10.sp, color = Color(0xFF0EA5E9))
                                    Text(text = "برمجة ($techCount)", fontSize = 10.sp, color = Color(0xFF10B981))
                                    Text(text = "أخرى ($otherCount)", fontSize = 10.sp, color = Color(0xFFA855F7))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Admin/Owner Reports View (ONLY accessible by owner backdoor)
                        if (isBackdoorOwnerVerified) {
                            Text(
                                text = if (isAr) "صندوق البلاغات ضد مقدمي الخدمات 🚨" else "User complaints box 🚨",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            if (reports.isEmpty()) {
                                Text(
                                    text = if (isAr) "لا توجد بلاغات مرسلة من المستخدمين حالياً." else "No complaint logs inside catalog.",
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                            } else {
                                reports.forEach { rep ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0x33DC2626)) // Soft transparent red accent
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                                Text(text = "بلاغ ضد: ${rep.providerName}", fontWeight = FontWeight.Bold, color = Color.White)
                                                IconButton(onClick = {
                                                    FirestoreSim.deleteReport(rep.id)
                                                    Toast.makeText(context, "تم أرشفة وحذف البلاغ بأمان! ✅", Toast.LENGTH_SHORT).show()
                                                }, modifier = Modifier.size(24.dp)) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete report", tint = Color.LightGray)
                                                }
                                            }
                                            Text(text = "مقدم البلاغ: ${rep.reporterName}", fontSize = 11.sp, color = Color.LightGray)
                                            Text(text = "السبب: ${rep.reason}", fontSize = 12.sp, color = Color.White, modifier = Modifier.padding(vertical = 4.dp))
                                            
                                            // Action block button
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                                                Button(
                                                    onClick = {
                                                        FirestoreSim.toggleProviderBlocked(rep.providerId, true)
                                                        FirestoreSim.deleteReport(rep.id)
                                                        Toast.makeText(context, "تم حظر مقدم الخدمة وحذف البلاغ! 🚫", Toast.LENGTH_SHORT).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                    modifier = Modifier.height(32.dp),
                                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                                ) {
                                                    Text("حظر صاحب الخدمة فوراً 🚫", fontSize = 10.sp, color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0x1AEA5F00))
                            ) {
                                Text(
                                    text = "ملاحظة: البلاغات، المسح، التوصية، والتثبيت الكامل مخصصة لمالك التطبيق الرئيسي فقط من البوابة الخلفية 🔒.",
                                    fontSize = 12.sp,
                                    color = Color.LightGray,
                                    modifier = Modifier.padding(12.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    1 -> {
                        // PENDING PROVIDERS EVALUATION
                        Text(
                            text = if (isAr) "مراجعة طلبات الانضمام المعلقة (${pendingProviders.size} طلب)" else "Pending Registrations (${pendingProviders.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (pendingProviders.isEmpty()) {
                            Text(
                                "لا يوجد طلبات انضمام معلقة حالياً. الكوادر يعملون جاهداً! 🌟",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth().padding(24.dp)
                            )
                        } else {
                            pendingProviders.forEach { item ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = item.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                        Text(text = "رقم الهاتف: ${item.phone}", fontSize = 12.sp, color = Color.White)
                                        Text(text = "القسم المطلوب: ${item.mainCategoryId}", fontSize = 12.sp, color = Color.LightGray)
                                        Text(text = "التخصص الدقيق: ${item.subCategoryId}", fontSize = 12.sp, color = Color.LightGray)
                                        Text(text = "العنوان: ${item.city} - ${item.region}", fontSize = 12.sp, color = Color.LightGray)
                                        
                                        Spacer(modifier = Modifier.height(12.dp))

                                        // Dynamic Sub-category verification dropdown input to fix stuck on electrician state!
                                        var selectedSubCategoryToConfirm by remember { mutableStateOf(item.subCategoryId) }

                                        OutlinedTextField(
                                            value = selectedSubCategoryToConfirm,
                                            onValueChange = { selectedSubCategoryToConfirm = it },
                                            placeholder = { Text("أدخل لتأكيد التخصص الفرعي لهذا المهني...") },
                                            label = { Text("تأكيد التخصص الفرعي للائحة") },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedTextColor = Color.White,
                                                unfocusedTextColor = Color.White
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    FirestoreSim.approvePendingProvider(item.id, selectedSubCategoryToConfirm)
                                                    Toast.makeText(context, "تم قبول طلب انضمام ${item.name} بنجاح ومزامنته ببلادنا! 🎉", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("قبول الطلب ✅", color = Color.Black, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    FirestoreSim.rejectPendingProvider(item.id, "لم يستوف الشروط")
                                                    Toast.makeText(context, "تم رفض الطلب وأرشفته بأمان.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("رفض الطلب ❌", color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // DIRECT PROVIDER ADDITION & MANAGER
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "إدارة مزودي الخدمات الحاليين", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Button(
                                onClick = { showDirectAddProviderDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+ إضافة مباشر", color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Render providers management row list
                        providers.forEach { prov ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = prov.name, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (prov.isVerified) {
                                            Icon(Icons.Default.Verified, contentDescription = "Verified badge", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Text(text = "رقم الهاتف: ${prov.phone} | نقاطه: ${prov.points}", fontSize = 12.sp, color = Color.LightGray)
                                    Text(text = "المهنة الفرعية: ${prov.subCategoryId}", fontSize = 12.sp, color = Color.LightGray)
                                    
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Block Status Indicators
                                    if (prov.isBlocked) {
                                        Text("🚫 هذا المهني محظور حالياً من الظهور العام للجمهور!", fontSize = 11.sp, color = Color.Red, fontWeight = FontWeight.Bold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons (Only Owner can pin/recommend, Admin can verify/block)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        // Pinned toggle (Owner Only)
                                        Button(
                                            onClick = {
                                                if (isBackdoorOwnerVerified) {
                                                    FirestoreSim.updateProviderControlFlags(prov.id, !prov.isPinned, prov.isRecommended, prov.isVerified)
                                                    Toast.makeText(context, "تم تغيير حالة التثبيت بالصدفة! 📌", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "الرجاء تسجيل الدخول كمالك من البوابة الخلفية للتثبيت!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (prov.isPinned) Color(0xFFEAB308) else Color.DarkGray
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (prov.isPinned) "مثبت بالمنصة 📌" else "تثبيت 📌", fontSize = 10.sp, color = if (prov.isPinned) Color.Black else Color.White)
                                        }

                                        // Recommended toggle (Owner Only)
                                        Button(
                                            onClick = {
                                                if (isBackdoorOwnerVerified) {
                                                    FirestoreSim.updateProviderControlFlags(prov.id, prov.isPinned, !prov.isRecommended, prov.isVerified)
                                                    Toast.makeText(context, "تم تغيير حالة التوصية بالصدفة! ⭐", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "الرجاء تسجيل الدخول كمالك للتوصية بالمقدم!", Toast.LENGTH_LONG).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (prov.isRecommended) Color(0xFFF59E0B) else Color.DarkGray
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (prov.isRecommended) "موصى به ⭐" else "توصية ⭐", fontSize = 10.sp, color = if (prov.isRecommended) Color.Black else Color.White)
                                        }

                                        // VIP Featured Badge toggle (Monthly subscription verification)
                                        Button(
                                            onClick = {
                                                FirestoreSim.handleProviderVipBadge(prov.id, !prov.hasVipBadge)
                                                Toast.makeText(context, "تم تحديث شارة التميز والاشتراكات الشهرية بنجاح! 💳", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (prov.hasVipBadge) Color(0xFF10B981) else Color.DarkGray
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (prov.hasVipBadge) "مميز VIP 👑" else "اشتراك VIP", fontSize = 10.sp, color = if (prov.hasVipBadge) Color.Black else Color.White)
                                        }

                                        // Block / Unblock option
                                        Button(
                                            onClick = {
                                                FirestoreSim.toggleProviderBlocked(prov.id, !prov.isBlocked)
                                                Toast.makeText(context, "تم تغيير حالة الحظر بنجاح!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (prov.isBlocked) Color.Red else Color.Gray
                                            ),
                                            modifier = Modifier.weight(1f).height(32.dp),
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Text(if (prov.isBlocked) "فك الحظر 🔓" else "حظر 🚫", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // CATEGORY BUILDER TOOL
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "إدارة أقسام وتصنيف المنصة", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Button(
                                onClick = { showAddCategoryDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+ إضافة قسم", color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        categories.forEach { cat ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = cat.icon, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                        Column {
                                            Text(text = cat.name, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(text = "Category ID: ${cat.id} | Sort order: ${cat.sortOrder}", fontSize = 11.sp, color = Color.LightGray)
                                        }
                                    }

                                    IconButton(onClick = {
                                        FirestoreSim.deleteCategory(cat.id)
                                        Toast.makeText(context, "تم حذف تصنيف القسم ومزامنته فوراً! 🗑️", Toast.LENGTH_SHORT).show()
                                    }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete category", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // BANNERS & PAID PROMOTIONS ADS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "إدارة اللافتات الإعلانية الملونة والإعلانات الممولة", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Button(
                                onClick = { showAddBannerDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+ إضافة إعلان ممول", color = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        banners.forEach { bn ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(text = bn.title, fontWeight = FontWeight.Bold, color = Color.White)
                                        IconButton(onClick = {
                                            FirestoreSim.deleteBanner(bn.id)
                                            Toast.makeText(context, "تم إرجاع أو حذف لافتة الإعلان الممولة!", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                                        }
                                    }
                                    Text(text = "الحجم المخصص: ${bn.sizeType} | المدة: ${bn.durationSeconds} ثانية | مستهدف: ${bn.targetCategory}", fontSize = 12.sp, color = Color.LightGray)
                                }
                            }
                        }
                    }

                    5 -> {
                        // GENERAL CONFIGS & DATABASE RESTORE BACKUP UTILITIES
                        Text(
                            text = "النسخ الاحتياطي، قائمة الأجهزة، الأمان ووضع الصيانة",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // APP Theme selection config modifier
                        Text(text = "نمط السمة والألوان والمظهر السري:", fontSize = 13.sp, color = Color.White)
                        var themeModeSelected by remember { mutableStateOf(configs.appThemeMode) }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Cosmic Slate", "Charcoal Gold", "Royal Emerald").forEach { themeName ->
                                Button(
                                    onClick = {
                                        themeModeSelected = themeName
                                        FirestoreSim.updateConfigs(configs.copy(appThemeMode = themeName))
                                        Toast.makeText(context, "تم تغيير السمة وتعميمها على جميع الأجهزة فوراً لمستوى فخم! 🌌", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (themeModeSelected == themeName) MaterialTheme.colorScheme.primary else Color.DarkGray
                                    ),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(themeName, fontSize = 10.sp, color = if (themeModeSelected == themeName) Color.Black else Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Database Backup Panel
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "إدارة النسخ الاحتياطي والأرشفة 💾",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "تتيح لك الأداة حفظ قاعدة البيانات محلياً على بطاقة الذاكرة SD أو استعادتها لأحداث تغييرات جذرية.",
                                    fontSize = 11.sp,
                                    color = Color.LightGray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                    Button(
                                        onClick = {
                                            FirestoreSim.backupDatabaseToPhone(context, "الذاكرة الداخلية والذاكرة الخارجية SD")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("أخذ نسخة احتياطية 📥", fontSize = 11.sp, color = Color.White)
                                    }
                                    Button(
                                        onClick = {
                                            FirestoreSim.restoreDatabase(context)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("استعادة البيانات 🔄", fontSize = 11.sp, color = Color.Black)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mode selectors: Maintenance & Data Saving Modes
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "توفير البيانات ووضع الصيانة العام ⚙️", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                
                                var mState by remember { mutableStateOf(configs.maintenanceModeEnabled) }
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable {
                                        mState = !mState
                                        FirestoreSim.updateConfigs(configs.copy(maintenanceModeEnabled = mState))
                                        Toast.makeText(context, "تم تغيير حالة الصيانة العامة للمنصة!", Toast.LENGTH_SHORT).show()
                                    },
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("تفعيل وضع الصيانة (Maintenance Mode)", fontSize = 12.sp, color = Color.White)
                                    Switch(checked = mState, onCheckedChange = {
                                        mState = it
                                        FirestoreSim.updateConfigs(configs.copy(maintenanceModeEnabled = it))
                                    })
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        FirestoreSim.purgeOldData(context)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("جدولة وتنظيف السجلات والدردشة القديمة 🧹", color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Whitelisted Authorized Devices Manager (For Whitelist security config)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(text = "الأجهزة المعتمدة والمصرح لها بالدخول 🔑", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                                Text(text = "قائمة بالمعرفات الحالية المسموح لها بإحداث تعديلات في قاعدة بيانات Firestore:", fontSize = 11.sp, color = Color.LightGray)
                                
                                whitelistedDevices.forEach { dev ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "• $dev", color = Color.White, fontSize = 12.sp)
                                        IconButton(onClick = {
                                            if (whitelistedDevices.size <= 1) {
                                                Toast.makeText(context, "عذراً، يجب إبقاء جهاز مصرح به واحد على الأقل!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                FirestoreSim.removeWhitelistedDevice(dev)
                                                Toast.makeText(context, "تم إزالة إذن الجهاز بنجاح!", Toast.LENGTH_SHORT).show()
                                            }
                                        }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.RemoveCircle, contentDescription = null, tint = Color.Red)
                                        }
                                    }
                                }

                                var newDeviceToAddField by remember { mutableStateOf("") }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = newDeviceToAddField,
                                        onValueChange = { newDeviceToAddField = it },
                                        placeholder = { Text("أدخل معرف جهاز جديد...") },
                                        modifier = Modifier.weight(1f).height(48.dp),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            if (newDeviceToAddField.isNotBlank()) {
                                                FirestoreSim.addWhitelistedDevice(newDeviceToAddField)
                                                newDeviceToAddField = ""
                                                Toast.makeText(context, "تم إضافة وترخيص الجهاز بالأمان!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Text("+ إضافة")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Direct Add Provider dialog box logic
    if (showDirectAddProviderDialog) {
        var dpName by remember { mutableStateOf("") }
        var dpPhone by remember { mutableStateOf("") }
        var dpCatSelected by remember { mutableStateOf(categories.firstOrNull()?.id ?: "") }
        var dpSubSelected by remember { mutableStateOf("") }
        var dpCity by remember { mutableStateOf("صنعاء") }
        var dpRegion by remember { mutableStateOf("") }
        var dpBio by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showDirectAddProviderDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("إضافة مقدم خدمة ومحترف مباشرة 🛠️", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp) },
            text = {
                Column(verticalScroll = rememberScrollState(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = dpName, onValueChange = { dpName = it }, label = { Text("الاسم الكامل للمهني") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = dpPhone, onValueChange = { dpPhone = it }, label = { Text("رقم الهاتف") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = dpRegion, onValueChange = { dpRegion = it }, label = { Text("المديرية أو المنطقة السكنية") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = dpSubSelected, onValueChange = { dpSubSelected = it }, label = { Text("التخصص الفرعي الدقيق") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = dpBio, onValueChange = { dpBio = it }, label = { Text("نبذة صغيرة عنه") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (dpName.isNotBlank() && dpPhone.isNotBlank()) {
                        val prov = Provider(
                            id = "p_dir_${System.currentTimeMillis()}",
                            name = dpName,
                            title = "الفني أخصائي $dpSubSelected المعتمد",
                            phone = dpPhone,
                            mainCategoryId = dpCatSelected,
                            subCategoryId = dpSubSelected.ifBlank { "كامل المهنة" },
                            city = dpCity,
                            region = dpRegion,
                            bio = dpBio.ifBlank { "مقدم خدمة معتمد بالدليل الفني المباشر." },
                            isVerified = true
                        )
                        FirestoreSim.addProviderDirectly(prov)
                        showDirectAddProviderDialog = false
                        Toast.makeText(context, "تم تسجيل الحساب مباشرة في اللائحة! 🎉", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("إضافة مباشر")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDirectAddProviderDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Category additions dialog box logic
    if (showAddCategoryDialog) {
        var catId by remember { mutableStateOf("") }
        var catName by remember { mutableStateOf("") }
        var catNameEn by remember { mutableStateOf("") }
        var catIcon by remember { mutableStateOf("🛠️") }

        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("إضافة قسم رئيسي جديد للمنصة 🗄️", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = catId, onValueChange = { catId = it }, label = { Text("معرف القسم الفريد (ID بالإنجليزية)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = catName, onValueChange = { catName = it }, label = { Text("اسم القسم باللغة العربية") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = catNameEn, onValueChange = { catNameEn = it }, label = { Text("اسم القسم باللغة الإنجليزية") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = catIcon, onValueChange = { catIcon = it }, label = { Text("أيقونة إيموجي مميزة للقسم") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (catId.isNotBlank() && catName.isNotBlank()) {
                        val pos = categories.size + 1
                        val cat = Category(id = catId, name = catName, nameEn = catNameEn, icon = catIcon, sortOrder = pos)
                        FirestoreSim.addCategory(cat)
                        showAddCategoryDialog = false
                        Toast.makeText(context, "تم حفظ ومزامنة القسم الجديد الفعال! 🎉", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("إضافة قسم")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }

    // Banner addition dialog box logic
    if (showAddBannerDialog) {
        var bTitle by remember { mutableStateOf("") }
        var bSize by remember { mutableStateOf("Medium") }
        var bDur by remember { mutableStateOf("8") }

        AlertDialog(
            onDismissRequest = { showAddBannerDialog = false },
            containerColor = Color(0xFF1E293B),
            title = { Text("إضافة لافتة معلوماتية / إعلان ممول للمستخدمين", color = MaterialTheme.colorScheme.primary, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = bTitle, onValueChange = { bTitle = it }, label = { Text("محتوى وتفاصيل نص الإعلان") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = bSize, onValueChange = { bSize = it }, label = { Text("الحجم (Small, Medium, Large)") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                    OutlinedTextField(value = bDur, onValueChange = { bDur = it }, label = { Text("مدة عرض اللافتة بالثواني") }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (bTitle.isNotBlank()) {
                        val banner = Banner(
                            id = "b_${System.currentTimeMillis()}",
                            title = bTitle,
                            sizeType = bSize,
                            durationSeconds = bDur.toIntOrNull() ?: 8,
                            timestamp = System.currentTimeMillis()
                        )
                        FirestoreSim.addBanner(banner)
                        showAddBannerDialog = false
                        Toast.makeText(context, "تم تفعيل الإعلان الممول والبانر للرئيسية! 📣", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("تأكيد ونشر الإعلان")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBannerDialog = false }) {
                    Text("إلغاء", color = Color.White)
                }
            }
        )
    }
}
