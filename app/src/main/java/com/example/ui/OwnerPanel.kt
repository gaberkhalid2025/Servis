@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppConfigs
import com.example.data.FirestoreSim
import com.example.data.PendingProvider
import com.example.data.Provider
import com.example.data.ReportNotification
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

val RatingGold = Color(0xFFFFD700)

@Composable
fun OwnerPanel(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val providers by FirestoreSim.providers.collectAsState()
    val pendingList by FirestoreSim.pendingProviders.collectAsState()
    val reportsList by FirestoreSim.reportNotifications.collectAsState()
    val templates by FirestoreSim.notificationTemplates.collectAsState()
    val configs by FirestoreSim.configs.collectAsState()
    val roomsList by FirestoreSim.chatRooms.collectAsState()
    val messagesList by FirestoreSim.messages.collectAsState()

    var activeTab by remember { mutableStateOf("dashboard") } // dashboard, approvals, notifications, styling, chats

    // Category assignment states for approved providers
    var showApproveDialog by remember { mutableStateOf<PendingProvider?>(null) }
    var selectedMainCat by remember { mutableStateOf("electricity") }
    var selectedSubCat by remember { mutableStateOf("solar") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("بوابة الإرساء والتحكم للمشرف 👑 (الآدمن)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.height(72.dp)
            ) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { activeTab = "dashboard" },
                    icon = { Icon(Icons.Default.Assessment, null) },
                    label = { Text("المؤشرات 📊", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "approvals",
                    onClick = { activeTab = "approvals" },
                    icon = {
                        BadgedBox(badge = {
                            if (pendingList.isNotEmpty()) {
                                Badge(containerColor = Color.Red) { Text("${pendingList.size}", color = Color.White) }
                            }
                        }) {
                            Icon(Icons.Default.GroupAdd, null)
                        }
                    },
                    label = { Text("الطلبات ⏳", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "notifications",
                    onClick = { activeTab = "notifications" },
                    icon = { Icon(Icons.Default.NotificationsActive, null) },
                    label = { Text("الإشعارات 🔔", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "styling",
                    onClick = { activeTab = "styling" },
                    icon = { Icon(Icons.Default.Palette, null) },
                    label = { Text("التخصيص 🎨", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "chats",
                    onClick = { activeTab = "chats" },
                    icon = { Icon(Icons.Default.LiveHelp, null) },
                    label = { Text("الدردشات 💬", fontSize = 10.sp) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Quick Emergency Bar on top if any complaints exist
                val activeComplaints = reportsList.filter { !it.isReviewed }
                if (activeComplaints.isNotEmpty() && activeTab != "dashboard") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF7F1D1D))
                            .clickable { activeTab = "dashboard" }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ReportProblem, null, tint = Color.Red)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تنبيه أمني عاجل: لديك (${activeComplaints.size}) شكاوى وبلاغات تتطلب مراجعتك فوراً!", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Icon(Icons.Default.ArrowForward, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }

                // Main Content depending on Tab selection
                Box(modifier = Modifier.weight(1f)) {
                    when (activeTab) {
                        "dashboard" -> DashboardTab(providers, pendingList, reportsList)
                        "approvals" -> ApprovalsTab(pendingList, onApprove = { pending ->
                            showApproveDialog = pending
                        }, onReject = { id ->
                            FirestoreSim.rejectPendingProvider(id)
                            Toast.makeText(context, "تم رفض وحذف طلب انضمام الفني", Toast.LENGTH_SHORT).show()
                        })
                        "notifications" -> NotificationsTab(templates)
                        "styling" -> StylingTab(configs, context)
                        "chats" -> AdminChatsTab(roomsList, messagesList, providers, context)
                    }
                }
            }

            // Category assignment dialogue
            showApproveDialog?.let { pending ->
                AlertDialog(
                    onDismissRequest = { showApproveDialog = null },
                    title = { Text("تعيين تخصص مقدم الخدمة 🛠️", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                    text = {
                        Column {
                            Text("الاسم: ${pending.name}", fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("المنطقة: ${pending.region}", fontSize = 11.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text("اختر القسم الأساسي المعتمد:", fontSize = 12.sp, color = Color.LightGray)
                            val mainCats = listOf(
                                "electricity" to "كهرباء وتمديدات ⚡",
                                "plumbing" to "سباكة وصحي 🚰",
                                "tech" to "برمجة وتكنولوجيا 📱",
                                "carpentry" to "نجارة وحرف يدوية 🪵",
                                "mechanics" to "ميكانيك سيارات 🚗"
                            )
                            mainCats.forEach { (catId, catLabel) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedMainCat = catId }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = selectedMainCat == catId, onClick = { selectedMainCat = catId })
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(catLabel, fontSize = 13.sp, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text("المسمى المهني الفرعي للوحة:", fontSize = 12.sp, color = Color.LightGray)
                            OutlinedTextField(
                                value = selectedSubCat,
                                onValueChange = { selectedSubCat = it },
                                placeholder = { Text("مثال: solar, hardware") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                FirestoreSim.approvePendingProvider(pending.id, selectedMainCat, selectedSubCat)
                                showApproveDialog = null
                                Toast.makeText(context, "تم توجيه وتفعيل مقدم الخدمة بالدليل المباشر بنجاح!", Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Text("قبول وتوثيق ✅")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showApproveDialog = null }) {
                            Text("إلغاء", color = Color.LightGray)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardTab(
    providers: List<Provider>,
    pending: List<PendingProvider>,
    reports: List<ReportNotification>
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Stats Cards grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "مقدمو الخدمات النشطون",
                value = "${providers.size}",
                icon = Icons.Default.Groups,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "طلبات معلقة للموافقة",
                value = "${pending.size}",
                icon = Icons.Default.Pending,
                color = RatingGold,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatsCard(
                title = "بلاغات وشكاوى وحالات",
                value = "${reports.size}",
                icon = Icons.Default.Warning,
                color = Color.Red,
                modifier = Modifier.weight(1f)
            )
            StatsCard(
                title = "مجموع التقييمات العامة",
                value = "${providers.map { it.reviewCount }.sum()} تقييم",
                icon = Icons.Default.Star,
                color = Color.Cyan,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Simulated Recharts Dashboard Canvas Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "مخطط توزيع أعداد المنضمين بالأقسام النشطة 📊 (Yemeni Recharts Engine)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Define categories analytics
                val electricSize = providers.count { it.mainCategoryId == "electricity" }
                val plumbingSize = providers.count { it.mainCategoryId == "plumbing" }
                val techSize = providers.count { it.mainCategoryId == "tech" }
                val carpentrySize = providers.count { it.mainCategoryId == "carpentry" }
                val mechanicsSize = providers.count { it.mainCategoryId == "mechanics" }

                val maxVal = maxOf(electricSize, plumbingSize, techSize, carpentrySize, mechanicsSize, 1)

                val barItems = listOf(
                    Triple("كهرباء", electricSize, MaterialTheme.colorScheme.primary),
                    Triple("سباكة", plumbingSize, Color.Green),
                    Triple("تقنية", techSize, Color.Cyan),
                    Triple("نجارة", carpentrySize, RatingGold),
                    Triple("ميكانيك", mechanicsSize, Color.Magenta)
                )

                // Custom charts drawn with pure Compose Layout Row Weights representing Bar Charts
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    barItems.forEach { (label, count, barColor) ->
                        val ratio = count.toFloat() / maxVal.toFloat()
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(label, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("$count موفر فني مهيأ", fontSize = 11.sp, color = Color.LightGray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Progress/Bar visual representation
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(12.dp)
                                    .background(Color(0xFF374151), RoundedCornerShape(30.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .fillMaxHeight()
                                        .background(barColor, RoundedCornerShape(30.dp))
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security Incident Logs View (نظام إشعارات فورية للمالك عند حدوث أي نشاط)
        Text("سجل الإشعارات الأمنية والبلاغات الفورية 🚨", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White, modifier = Modifier.padding(bottom = 10.dp))
        if (reports.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("سجل البلاغات فارغ تماماً وأجهزة البوابة آمنة! 🔒", color = Color.LightGray, fontSize = 12.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                reports.forEach { rep ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (rep.isReviewed) Color(0xFF374151) else Color(0xFF451A1A)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val icon = when (rep.type) {
                                        "UNAUTHORIZED_LOGIN" -> Icons.Default.Lock
                                        "COMPLAINT" -> Icons.Default.Feedback
                                        else -> Icons.Default.Stars
                                    }
                                    Icon(icon, null, tint = if (rep.isReviewed) Color.LightGray else Color.Red, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(rep.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                Text(
                                    text = if (rep.isReviewed) "تمت المراجعة ✔️" else "غير مقروء 🆕",
                                    fontSize = 11.sp,
                                    color = if (rep.isReviewed) Color.Green else Color.Red,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(rep.content, fontSize = 11.sp, color = Color.LightGray)
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!rep.isReviewed) {
                                    Button(
                                        onClick = { FirestoreSim.markReportFileReviewed(rep.id) },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("تأكيد المراجعة", fontSize = 10.sp)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Button(
                                    onClick = {
                                        FirestoreSim.deleteAdminReport(rep.id)
                                        Toast.makeText(context, "تم حذف البلاغ نهائياً", Toast.LENGTH_SHORT).show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier
                                        .height(28.dp)
                                        .border(1.dp, Color.Red, RoundedCornerShape(8.dp))
                                ) {
                                    Text("حذف البلاغ", fontSize = 10.sp, color = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontSize = 11.sp, color = Color.Gray)
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun ApprovalsTab(
    pendingList: List<PendingProvider>,
    onApprove: (PendingProvider) -> Unit,
    onReject: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("طلبات الحرفيين والفنيين الجدد قيد المراجعة ⏳", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

        if (pendingList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F2937), RoundedCornerShape(12.dp))
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Celebration, null, tint = RatingGold, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("لا توجد طلبات معلقة حالياً، عمل عظيم!", color = Color.LightGray, fontSize = 12.sp)
                }
            }
        } else {
            pendingList.forEach { pending ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(pending.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text(pending.region, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(pending.title, fontSize = 12.sp, color = Color.LightGray)
                        Text("رقم التواصل: ${pending.phone}", fontSize = 11.sp, color = Color.LightGray)
                        Spacer(modifier = Modifier.height(10.dp))

                        // Documents section (Selfie + ID)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Face, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                    Text("صورة السيلفي 🤳", fontSize = 10.sp, color = Color.White)
                                    Text(pending.selfieUri, fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CreditCard, null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                                    Text("بطاقة الهوية 💳", fontSize = 10.sp, color = Color.White)
                                    Text(pending.idCardUri, fontSize = 8.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                onClick = { onApprove(pending) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                            ) {
                                Text("توجيه وقبول الحساب", fontSize = 11.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onReject(pending.id) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("رفض الطلب ❌", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsTab(templates: List<com.example.data.NotificationTemplate>) {
    val context = LocalContext.current
    var editingTemplateId by remember { mutableStateOf<String?>(null) }
    var currentTitle by remember { mutableStateOf("") }
    var currentBody by remember { mutableStateOf("") }
    var currentDelay by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("قوالب الإشعارات التلقائية الفورية بالسيارة 🤖", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White, modifier = Modifier.padding(bottom = 6.dp))
        Text("تخصيص الرسائل التلقائية وحفظها في الـ Firestore مع تفعيل أو تعطيل التغذية الراجعة فوراً.", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 16.dp))

        templates.forEach { temp ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(temp.nameAr, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("توقيت الإرسال: بعد ${temp.sendDelayMinutes} دقيقة", fontSize = 10.sp, color = Color.LightGray)
                        }
                        Switch(
                            checked = temp.isEnabled,
                            onCheckedChange = {
                                FirestoreSim.toggleTemplateEnabled(temp.id)
                                Toast.makeText(context, "تم تعديل حالة تفعيل القالب", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF111827), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text(temp.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(temp.body, fontSize = 11.sp, color = Color.LightGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                editingTemplateId = temp.id
                                currentTitle = temp.title
                                currentBody = temp.body
                                currentDelay = temp.sendDelayMinutes.toString()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("تعديل النموذج 📝", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Custom template modifying layout popup
        editingTemplateId?.let { templateId ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("نافذة تعديل القالب المعياري", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = currentTitle,
                        onValueChange = { currentTitle = it },
                        label = { Text("أبرز اسم للتنبيه (عنوان الإشعار)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                    )

                    OutlinedTextField(
                        value = currentBody,
                        onValueChange = { currentBody = it },
                        label = { Text("مضمون ونص التنبيه التلقائي") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = currentDelay,
                        onValueChange = { currentDelay = it },
                        label = { Text("مدة التأخير المقررة (بالدقائق)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = {
                                val delay = currentDelay.toIntOrNull() ?: 1
                                FirestoreSim.updateTemplate(templateId, currentTitle, currentBody, delay)
                                editingTemplateId = null
                                Toast.makeText(context, "تم تسجيل وحفظ القالب وتطبيقه على Firestore بنجاح ✅", Toast.LENGTH_LONG).show()
                            }
                        ) {
                            Text("حفظ التغييرات 💾")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = { editingTemplateId = null }) {
                            Text("إلغاء", color = Color.LightGray)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StylingTab(configs: AppConfigs, context: android.content.Context) {
    var primaryColorField by remember { mutableStateOf(configs.primaryHex) }
    var secondaryColorField by remember { mutableStateOf(configs.secondaryHex) }

    var smartAssSize by remember { mutableStateOf(configs.smartAssistantIconSize.toFloat()) }
    var smartAssVisible by remember { mutableStateOf(configs.smartAssistantVisible) }

    var appInfoIconSize by remember { mutableStateOf(configs.appInfoIconSize.toFloat()) }
    var appInfoVisible by remember { mutableStateOf(configs.appInfoVisible) }

    // Floating Bubble interactive custom coordinates and size params
    var bubbleX by remember { mutableStateOf(configs.bubbleXOffset) }
    var bubbleY by remember { mutableStateOf(configs.bubbleYOffset) }
    var bubbleS by remember { mutableStateOf(configs.bubbleSize.toFloat()) }

    var fcmEnabled by remember { mutableStateOf(configs.fcmEnabled) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("تخصيص اللوحة والألوان المعتمدة 🎨", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RatingGold)
        Text("تعديل ألوان السمة (Blue/Red Hex) والموقع لحفظها وتطبيقها تلقائياً بالوجهات.", fontSize = 11.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تخصيص سمة الألوان (Hex Code Palette)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = primaryColorField,
                    onValueChange = { primaryColorField = it },
                    label = { Text("قيمة اللون الأساسي الأزرق (مثل #0284C7)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                )

                OutlinedTextField(
                    value = secondaryColorField,
                    onValueChange = { secondaryColorField = it },
                    label = { Text("قيمة اللون الفرعي الأحمر (مثل #EF4444)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("إعدادات تحريك وتصميم وتحجيم الأيقونة العائمة 💬", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                Text("الإزاحة الأفقية للأيقونة (X Offset): ${bubbleX.toInt()} dp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = bubbleX,
                    onValueChange = { bubbleX = it },
                    valueRange = -200f..200f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("الإزاحة الرأسية للأيقونة (Y Offset): ${bubbleY.toInt()} dp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = bubbleY,
                    onValueChange = { bubbleY = it },
                    valueRange = -400f..400f,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("حجم قطر الدائرة (Floating Diameter): ${bubbleS.toInt()} dp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = bubbleS,
                    onValueChange = { bubbleS = it },
                    valueRange = 40f..100f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("تخصيص وحجب أيقونات المساعد والمعلومات ⚙️", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Spacer(modifier = Modifier.height(10.dp))

                // Smart assistant edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إظهار أيقونة المساعد الذكي 🤖", fontSize = 12.sp, color = Color.White)
                    Switch(checked = smartAssVisible, onCheckedChange = { smartAssVisible = it })
                }
                Text("حجم المساعد الذكي: ${smartAssSize.toInt()} dp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = smartAssSize,
                    onValueChange = { smartAssSize = it },
                    valueRange = 24f..80f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // App Info config edit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("إظهار أيقونة معلومات التطبيق ℹ️", fontSize = 12.sp, color = Color.White)
                    Switch(checked = appInfoVisible, onCheckedChange = { appInfoVisible = it })
                }
                Text("حجم أيقونة المعلومات: ${appInfoIconSize.toInt()} dp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = appInfoIconSize,
                    onValueChange = { appInfoIconSize = it },
                    valueRange = 24f..80f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Firebase Cloud Messaging (FCM) Integration toggle
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("مزامنة خدمة الإشعارات السحابية (FCM) 📬", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                        Text("إرسال إشعار فوري للعملاء عند قبول الطلب أو رد الدردشة.", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Switch(
                        checked = fcmEnabled,
                        onCheckedChange = { fcmEnabled = it }
                    )
                }
            }
        }

        Button(
            onClick = {
                val updated = AppConfigs(
                    primaryHex = primaryColorField,
                    secondaryHex = secondaryColorField,
                    smartAssistantIconSize = smartAssSize.toInt(),
                    smartAssistantVisible = smartAssVisible,
                    appInfoIconSize = appInfoIconSize.toInt(),
                    appInfoVisible = appInfoVisible,
                    bubbleXOffset = bubbleX,
                    bubbleYOffset = bubbleY,
                    bubbleSize = bubbleS.toInt(),
                    fcmEnabled = fcmEnabled
                )
                FirestoreSim.updateAppConfigs(updated)
                Toast.makeText(context, "تم حفظ وتطبيق وتعميم الإستايل والأيقونات بنجاح فورياً! ✅", Toast.LENGTH_LONG).show()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("حفظ وتحديث الاستايل فوراً 🎨" , fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AdminChatsTab(
    roomsList: List<com.example.data.ChatRoom>,
    messages: List<com.example.data.ChatMessage>,
    providers: List<Provider>,
    context: android.content.Context
) {
    var selectedRoomId by remember { mutableStateOf<String?>(null) }
    var serviceShutdownAllBlocked by remember { mutableStateOf(providers.all { it.isBlocked }) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("إدارة وبوابة الدردشات الحية والرقابة الفورية 👮‍♂️", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text("حظر وإيقاف فنيين أو تعطيل الخدمة بأكملها.", fontSize = 10.sp, color = Color.LightGray)
            }

            // Shutdown toggle to stop/shutdown services to all or individual providers
            Column(horizontalAlignment = Alignment.End) {
                Text(if (serviceShutdownAllBlocked) "الخدمات معطلة ⛔" else "الخدمات مفعلة ✅", fontSize = 10.sp, color = if (serviceShutdownAllBlocked) Color.Red else Color.Green, fontWeight = FontWeight.Bold)
                Switch(
                    checked = serviceShutdownAllBlocked,
                    onCheckedChange = { blockAll ->
                        serviceShutdownAllBlocked = blockAll
                        // Block all providers
                        providers.forEach {
                            FirestoreSim.setProviderBlocked(it.id, blockAll)
                        }
                        Toast.makeText(context, if (blockAll) "تم إيقاف وتعطيل الخدمات كلياً عن كافة مقدمي الدليل 🛑" else "تم إعادة تشغيل الخدمات لجميع مقدمي الدليل بنجاح 🟢", Toast.LENGTH_LONG).show()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.Red)
                )
            }
        }

        if (selectedRoomId == null) {
            // General Chats list
            if (roomsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF1F2937), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("لا توجد دردشات نشطة بين المستخدمين حالياً", color = Color.LightGray, fontSize = 12.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(roomsList.size) { index ->
                        val room = roomsList[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedRoomId = room.id },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("بين: ${room.userName} 🔀 ${room.providerName}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("آخر رسالة: ${room.lastMessage}", fontSize = 11.sp, color = Color.LightGray)
                                }
                                Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Transcript view of a chat room
            val room = roomsList.find { it.id == selectedRoomId }
            if (room != null) {
                val roomMessages = messages.filter { it.chatRoomId == room.id }
                val currentProviderRef = providers.find { it.id == room.providerId }
                val isSelfBlocked = currentProviderRef?.isBlocked == true

                Column(modifier = Modifier.weight(1f)) {
                    // Chat header inside audit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF374151), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("مراقبة التراسل: ${room.userName} 💬", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("الفني: ${room.providerName}", fontSize = 11.sp, color = RatingGold)
                        }
                        IconButton(onClick = { selectedRoomId = null }) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                        }
                    }

                    // Messages box scroll
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFF1F2937), RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(roomMessages.size) { mIdx ->
                            val msg = roomMessages[mIdx]
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (msg.senderId == room.userId) Alignment.Start else Alignment.End
                            ) {
                                Text(msg.senderName, fontSize = 9.sp, color = Color.Gray)
                                Box(
                                    modifier = Modifier
                                        .background(
                                            if (msg.senderId == room.userId) Color(0xFF1E3A8A) else Color(0xFF0F172A),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
                                ) {
                                    Column {
                                        Text(msg.content, color = Color.White, fontSize = 11.sp)
                                        if (msg.imageAttachedUri != null) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("[مرفق صورة كاميرا: ${msg.imageAttachedUri}]", color = Color.Green, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Suspension actions for specific provider
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF451A1A))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("صلاحية التحكم بالموفر الحالي:", fontSize = 11.sp, color = Color.White)
                            Button(
                                onClick = {
                                    FirestoreSim.setProviderBlocked(room.providerId, !isSelfBlocked)
                                    Toast.makeText(context, if (!isSelfBlocked) "تم قفل وحظر حساب الفني ${room.providerName} مؤقتاً 🚫" else "تم فك الحظر لتيسير التراسل للموفر 🟢", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isSelfBlocked) Color.Green else Color.Red)
                            ) {
                                Text(if (isSelfBlocked) "فك حظر الفني 🟢" else "إيقاف وحظر الفني 🛑", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
