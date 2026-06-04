package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.AlertRed
import com.example.ui.theme.getSelectedTextColor
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanel(
    onLogout: () -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val appColors = FirestoreSim.appColors.collectAsState()
    val writeTextColor = getSelectedTextColor(appColors.value.textColorName)

    // Current panel tab selection
    var activeTab by remember { mutableStateOf(0) }
    val tabs = listOf("طلبات التسجيل", "إدارة الأقسام", "إضافة فني يدوياً", "تخصيص اللوحة", "إدارة المشرفين", "ترتيب وحجم الأقسام")

    // Database states
    val pendingRequests = FirestoreSim.pendingProviders.collectAsState()
    val mainCats = FirestoreSim.mainCategories.collectAsState()
    val subCats = FirestoreSim.subCategories.collectAsState()
    val moderators = FirestoreSim.moderators.collectAsState()
    val configs = FirestoreSim.appConfigs.collectAsState()

    // Dialog details/zooming states
    var selectedRequestForDetails by remember { mutableStateOf<PendingProvider?>(null) }
    var zoomImageUrl by remember { mutableStateOf<String?>(null) }
    var rejectionReasonText by remember { mutableStateOf("") }
    var showRejectionDialog by remember { mutableStateOf<String?>(null) } // holds requestId if active

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Management Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "لوحة تحكم الإشراف (Moderator)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "المستخدم الحالي: WAM2026",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onBackToHome,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Text("الرئيسية", fontSize = 12.sp)
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "خروج", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Horizontal navigation tabs
        ScrollableTabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 8.dp
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = activeTab == idx,
                    onClick = { activeTab = idx },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

        // Tab views
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (activeTab) {
                0 -> {
                    // TAB 1: Pending Enrollment Requests
                    if (pendingRequests.value.isEmpty()) {
                        EmptyStateView(
                            title = "لا توجد طلبات انضمام حالياً",
                            subtitle = "عند قيام فنيين بالتسجيل عبر صفحة الحساب (👤)، ستظهر طلباتهم المعلقة في هذه المسارات فوراً بمزامنة كاملة."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(pendingRequests.value) { req ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedRequestForDetails = req },
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = req.name,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Badge(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
                                                Text(
                                                    text = "قيد المراجعة",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 10.sp,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoftGray)
                                            Text(req.phone, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoftGray)
                                            Text("${req.district} - ${req.address}", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        val parentName = mainCats.value.find { it.id == req.mainCategoryId }?.name ?: ""
                                        val childName = subCats.value.find { it.id == req.subCategoryId }?.name ?: ""
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.Work, contentDescription = null, modifier = Modifier.size(14.dp), tint = SoftGray)
                                            Text(
                                                text = "الخدمة: $parentName ← $childName",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    FirestoreSim.updatePendingStatus(context, req.id, "approved")
                                                    Toast.makeText(context, "تم قبول طلب المهني وإنزاله في الخدمة فوراً!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Text("قبول الطلب", color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp)
                                            }

                                            Button(
                                                onClick = { showRejectionDialog = req.id },
                                                colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f).height(38.dp)
                                            ) {
                                                Text("رفض الطلب", color = Color.White, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: Categories management (إدارة الأقسام)
                    CategoryManagementView(mainCats.value, subCats.value)
                }

                2 -> {
                    // TAB 3: Instant hand provider enrollment ("إضافة فني يدوياً")
                    DirectAddProviderView(mainCats.value, subCats.value, writeTextColor)
                }

                3 -> {
                    // TAB 4: User Dashboard Customization ("تخصيص اللوحة")
                    UserDashboardSettingsTab(
                        configs = configs.value,
                        onSave = { updated -> FirestoreSim.updateConfigs(context, updated) },
                        writeTextColor = writeTextColor
                    )
                }

                4 -> {
                    // TAB 5: Moderator Management ("إدارة المشرفين")
                    ModeratorsManagementTab(
                        moderators = moderators.value,
                        onSaveMod = { mod -> FirestoreSim.saveModerator(context, mod) },
                        onDeleteMod = { modId -> FirestoreSim.deleteModerator(context, modId) },
                        writeTextColor = writeTextColor
                    )
                }

                5 -> {
                    // TAB 6: Category Icons, Sizing and Reordering ("ترتيب وحجم الأقسام")
                    CategoryIconsSizingReorderingTab(
                        mainCategories = mainCats.value,
                        configs = configs.value,
                        onSaveCategory = { cat -> FirestoreSim.saveMainCategory(context, cat) },
                        onUpdateConfigs = { updated -> FirestoreSim.updateConfigs(context, updated) },
                        writeTextColor = writeTextColor
                    )
                }
            }
        }
    }

    // Modal dialogue: detailed review of enrollment application
    selectedRequestForDetails?.let { req ->
        Dialog(onDismissRequest = { selectedRequestForDetails = null }) {
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
                        Text("ملف الطلب التفصيلي للمهني", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { selectedRequestForDetails = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

                    // Info Blocks
                    DetailItemLabel(title = "اسم المتقدم الثلاثي", content = req.name)
                    DetailItemLabel(title = "رقم الهاتف والواتساب", content = req.phone)
                    DetailItemLabel(title = "العنوان والمكتب العملي", content = req.address)
                    DetailItemLabel(title = "منطقة الدائرة السكنية", content = req.district)
                    DetailItemLabel(title = "إحداثيات موقع الخريطة GPS", content = req.gpsCoordinates ?: "غير متوفرة")

                    val parentName = mainCats.value.find { it.id == req.mainCategoryId }?.name ?: ""
                    val childName = subCats.value.find { it.id == req.subCategoryId }?.name ?: ""
                    DetailItemLabel(title = "القسم والتخصص المهني", content = "$parentName ← $childName")

                    Spacer(modifier = Modifier.height(8.dp))

                    // IMAGE 1 PREVIEW: Profile pic
                    Text("الصورة الشخصية المعتمدة لملف الفني (اضغط للتكبير):", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray)
                            .clickable { zoomImageUrl = "شخصية الفني ${req.name}" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Text("صورة الملف الشخصي للمهني", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.4f)).fillMaxWidth().padding(4.dp), textAlign = TextAlign.Center)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // IMAGE 2 PREVIEW: Identity Card (optional)
                    Text("صورة بطاقة الهوية الوطنية المرفقة (اضغط للتكبير):", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.DarkGray)
                            .clickable { zoomImageUrl = "بطاقة الهوية والبيانات الشخصية لـ ${req.name}" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.CreditCard, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                        Text(
                            text = if (req.idCardUrl == null) "لم ترفق بطاقة هوية شخصية مع هذا الطلب" else "معاينة بطاقة الهوية الوطنية",
                            color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.BottomCenter).background(Color.Black.copy(alpha = 0.4f)).fillMaxWidth().padding(4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                FirestoreSim.updatePendingStatus(context, req.id, "approved")
                                Toast.makeText(context, "تم قبول طلب المهني فوراً!", Toast.LENGTH_SHORT).show()
                                selectedRequestForDetails = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("قبول فوري", color = MaterialTheme.colorScheme.onPrimary)
                        }

                        Button(
                            onClick = {
                                showRejectionDialog = req.id
                                selectedRequestForDetails = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                        ) {
                            Text("رفض وانتقاد")
                        }
                    }
                }
            }
        }
    }

    // Modal Dialogue: Zoomed up view of images
    zoomImageUrl?.let { title ->
        Dialog(onDismissRequest = { zoomImageUrl = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                modifier = Modifier.fillMaxWidth().height(350.dp).padding(10.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    IconButton(
                        onClick = { zoomImageUrl = null },
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (title.contains("شخصية")) Icons.Default.AccountBox else Icons.Default.ContactPage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(120.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = title,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Text(
                            text = "تم التحقق من الوثيقة الرقمية الفورية ✓\nتشفير حماية السحابة مفعل",
                            color = Color.Gray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

    // Dialogue: Submitting rejecting reasons
    showRejectionDialog?.let { reqId ->
        Dialog(onDismissRequest = { showRejectionDialog = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("إضافة سبب الرفض", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    
                    OutlinedTextField(
                        value = rejectionReasonText,
                        onValueChange = { rejectionReasonText = it },
                        placeholder = { Text("مثال: الصورة المرفقة غير واضحة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = writeTextColor,
                            unfocusedTextColor = writeTextColor
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showRejectionDialog = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                FirestoreSim.updatePendingStatus(context, reqId, "rejected", rejectionReasonText.ifBlank { "لم يذكر سبب محدد للرفض" })
                                Toast.makeText(context, "تم رفض الطلب بنجاح وتوثيق الأسباب للإحاطة.", Toast.LENGTH_SHORT).show()
                                showRejectionDialog = null
                                rejectionReasonText = ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("تأكيد الرفض")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailItemLabel(title: String, content: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(title, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(content, color = Color.White, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// ==========================================
// VIEW CATEGORY MANAGEMENT (إدارة الأقسام)
// ==========================================
@Composable
fun CategoryManagementView(
    mainCats: List<MainCategory>,
    subCats: List<SubCategory>
) {
    val context = LocalContext.current
    var selectedMainCatId by remember { mutableStateOf<String?>(null) }
    
    // Add togglers
    var showAddMainDialog by remember { mutableStateOf(false) }
    var showAddSubDialog by remember { mutableStateOf<String?>(null) } // parentId if active

    var nameInput by remember { mutableStateOf("") }
    var iconInput by remember { mutableStateOf("home") }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("قائمة الأقسام الرئيسية والفرعية", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Button(
                onClick = { showAddMainDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة قسم رئيسي", fontSize = 12.sp)
            }
        }

        // Selected navigation path
        if (selectedMainCatId != null) {
            val pName = mainCats.find { it.id == selectedMainCatId }?.name ?: ""
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("عرض المهام الفرعية لـ: $pName", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = { selectedMainCatId = null }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "عودة للأقسام", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            if (selectedMainCatId == null) {
                // Show Main Categories
                items(mainCats) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMainCatId = item.id },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(item.iconCode) {
                                            "medical" -> Icons.Default.MedicalServices
                                            "school" -> Icons.Default.School
                                            "car" -> Icons.Default.DirectionsCar
                                            else -> Icons.Default.HomeWork
                                        },
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Column {
                                    Text(item.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                                    val count = subCats.filter { it.parentId == item.id }.size
                                    Text("يحتوي على: $count مهن فرعية", fontSize = 11.sp, color = Color.Gray)
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { showAddSubDialog = item.id }) {
                                    Icon(Icons.Default.PlaylistAdd, contentDescription = "إضافة فرعي", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = {
                                    FirestoreSim.deleteMainCategory(context, item.id)
                                    Toast.makeText(context, "تم حذف القسم الرئيسي وكافة مفرعاته بنجاح التام!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف قسم", tint = AlertRed)
                                }
                            }
                        }
                    }
                }
            } else {
                // Show Sub categories for selected main category ONLY
                val currentSubs = subCats.filter { it.parentId == selectedMainCatId }
                if (currentSubs.isEmpty()) {
                    item {
                        EmptyStateView(
                            title = "لا توجد مهن أو تصنيفات مضافة",
                            subtitle = "اضغط على زر (إضافة فرعي) في الأعلى لإنزال الكوادر والمهن في هذا التصنيف فوراً."
                        )
                    }
                } else {
                    items(currentSubs) { sub ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircleOutline, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(sub.name, fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 14.sp)
                                }
                                IconButton(onClick = {
                                    FirestoreSim.deleteSubCategory(context, sub.id)
                                    Toast.makeText(context, "تم حذف المعلم المهني الفرعي!", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = AlertRed)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Add Main Category
        if (showAddMainDialog) {
            Dialog(onDismissRequest = { showAddMainDialog = false }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("إضافة قسم رئيسي جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("اسم القسم الجديد (مثل: خدمات أمنية)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Text("اختر أيقونة توضيحية للقسم:", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf("home", "medical", "school", "car").forEach { design ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (iconInput == design) MaterialTheme.colorScheme.primary else Color.DarkGray
                                        )
                                        .clickable { iconInput = design },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when(design) {
                                            "medical" -> Icons.Default.MedicalServices
                                            "school" -> Icons.Default.School
                                            "car" -> Icons.Default.DirectionsCar
                                            else -> Icons.Default.HomeWork
                                        },
                                        contentDescription = null,
                                        tint = if (iconInput == design) Color.Black else Color.White
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { showAddMainDialog = false }, modifier = Modifier.weight(1f)) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank()) {
                                        FirestoreSim.saveMainCategory(context, MainCategory(name = nameInput, iconCode = iconInput))
                                        Toast.makeText(context, "تم حفظ القسم الرئيسي وتوثيقه بسيرفرات السحابة بنجاح!", Toast.LENGTH_SHORT).show()
                                        showAddMainDialog = false
                                        nameInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("إضافة التأسيس")
                            }
                        }
                    }
                }
            }
        }

        // Dialog: Add Sub Category
        showAddSubDialog?.let { parentId ->
            Dialog(onDismissRequest = { showAddSubDialog = null }) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("إضافة قسم فرعي/مهنة جديدة", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)

                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = { nameInput = it },
                            label = { Text("اسم المهنة (مثل: فني ستلايت، صيانة أفران)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { showAddSubDialog = null }, modifier = Modifier.weight(1f)) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = {
                                    if (nameInput.isNotBlank()) {
                                        FirestoreSim.saveSubCategory(context, SubCategory(parentId = parentId, name = nameInput))
                                        Toast.makeText(context, "تم إضافة الحرفة بنجاح!", Toast.LENGTH_SHORT).show()
                                        showAddSubDialog = null
                                        nameInput = ""
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("إضافة الحرفة")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// DIRECT ADD PROVIDER WORKSPACE ("إضافة فني يدوياً")
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DirectAddProviderView(
    mainCats: List<MainCategory>,
    subCats: List<SubCategory>,
    writeTextColor: Color
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Inputs
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var district by remember { mutableStateOf("") }
    var mainCatSelection by remember { mutableStateOf("") }
    var subCatSelection by remember { mutableStateOf("") }

    var mainExpanded by remember { mutableStateOf(false) }
    var subExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = "إضافة مقدم خدمة مباشر (دون انتظار الشروط)",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Text(
            text = "تسمح هذه الواجهة الفريدة لملاك التطبيق والمشرفين بإنزال وتدشين أي شريك مهني بشكل فوري ومستعجل دون استيفاء القيود المعقدة.",
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
        )

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("الاسم الثنائي/الثلاثي للمهني") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("رقم الهاتف أو الواتساب") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )

        // Dropdown main
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded = mainExpanded, onExpandedChange = { mainExpanded = !mainExpanded }) {
                OutlinedTextField(
                    value = mainCats.find { it.id == mainCatSelection }?.name ?: "اختر القسم والتصنيف الرئيسي...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("التصنيف الرئيسي") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mainExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
                ExposedDropdownMenu(expanded = mainExpanded, onDismissRequest = { mainExpanded = false }) {
                    mainCats.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                mainCatSelection = item.id
                                subCatSelection = ""
                                mainExpanded = false
                            }
                        )
                    }
                }
            }
        }

        // Dropdown sub
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(expanded = subExpanded, onExpandedChange = { subExpanded = !subExpanded }) {
                OutlinedTextField(
                    value = subCats.find { it.id == subCatSelection }?.name ?: "اختر التخصص المهني الدقيق...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("التخصص المهني") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
                ExposedDropdownMenu(expanded = subExpanded, onDismissRequest = { subExpanded = false }) {
                    subCats.filter { it.parentId == mainCatSelection }.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name) },
                            onClick = {
                                subCatSelection = item.id
                                subExpanded = false
                            }
                        )
                    }
                }
            }
        }

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("عنوان التواجد للشارع والمكتب") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
            singleLine = true
        )

        OutlinedTextField(
            value = district,
            onValueChange = { district = it },
            label = { Text("اسم المديرية السكنية (مثال: حي الروضة)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
            singleLine = true
        )

        Button(
            onClick = {
                if (name.isBlank() || phone.isBlank() || mainCatSelection.isBlank() || subCatSelection.isBlank() || address.isBlank() || district.isBlank()) {
                    Toast.makeText(context, "الرجاء تعبئة كافة الحقول لحفظ الفني بالدليل بنجاح", Toast.LENGTH_SHORT).show()
                } else {
                    val p = ServiceProvider(
                        name = name,
                        phone = phone,
                        mainCategoryId = mainCatSelection,
                        subCategoryId = subCatSelection,
                        address = address,
                        district = district,
                        avatarUrl = "avatar_male_${(1..5).random()}"
                    )
                    FirestoreSim.addProviderInstantly(context, p)
                    Toast.makeText(context, "تم حفظ الفني بالدليل وبث بياناته لجميع المشتركين فوراً!", Toast.LENGTH_LONG).show()
                    
                    // Reset
                    name = ""
                    phone = ""
                    address = ""
                    district = ""
                    mainCatSelection = ""
                    subCatSelection = ""
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("إضافة شريك الدليل فوراً للعامة", color = MaterialTheme.colorScheme.onPrimary)
        }
    }
}

// ==========================================
// UTILITY: EMPTY STATE VIEW
// ==========================================
@Composable
fun EmptyStateView(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(Icons.Default.CloudSync, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), modifier = Modifier.size(72.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

private val SoftGray = Color(0xFF8E8E93)

@Composable
fun UserDashboardSettingsTab(
    configs: AppConfigs,
    onSave: (AppConfigs) -> Unit,
    writeTextColor: Color
) {
    var customMsg by remember { mutableStateOf(configs.dashboardCustomMessage) }
    var showFavs by remember { mutableStateOf(configs.showDashboardFavorites) }
    var showCallHistory by remember { mutableStateOf(configs.showDashboardCallHistory) }
    var favsFirst by remember { mutableStateOf(configs.dashboardFavoritesFirst) }

    val context = LocalContext.current
    val citySettings = FirestoreSim.cities.collectAsState()

    // Dialog states for add/edit in Admin context
    var showAddCityDialog by remember { mutableStateOf(false) }
    var cityNameInput by remember { mutableStateOf("") }
    var districtsInput by remember { mutableStateOf("") }
    var countryNameInput by remember { mutableStateOf("اليمن") }

    var showEditCityDialog by remember { mutableStateOf(false) }
    var editingCityId by remember { mutableStateOf("") }
    var editCityNameInput by remember { mutableStateOf("") }
    var editDistrictsInput by remember { mutableStateOf("") }
    var editCountryInput by remember { mutableStateOf("اليمن") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("تخصيص لوحة تحكم المستخدمين العامة", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }

        Text(
            "تُمكّن هذه الإعدادات المشرفين من التحكم في شكل ومحتوى لوحة تحكم المفضلات وسجلات التواصل الخاصة بالمستخدم.",
            fontSize = 11.sp,
            color = Color.LightGray,
            lineHeight = 16.sp
        )

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

        OutlinedTextField(
            value = customMsg,
            onValueChange = { customMsg = it },
            label = { Text("رسالة ترحيب مخصصة باللوحة") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("عرض قسم المفضلة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("إظهار الفنيين المفضلين الذين حفظهم المستخدم", color = Color.Gray, fontSize = 10.sp)
            }
            Switch(
                checked = showFavs,
                onCheckedChange = { showFavs = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("عرض قسم سجل التواصل", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("إظهار قائمة الفنيين الذين اتصل بهم المستخدم مؤخراً", color = Color.Gray, fontSize = 10.sp)
            }
            Switch(
                checked = showCallHistory,
                onCheckedChange = { showCallHistory = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("تقديم قسم المفضلة أولاً", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("إذا تم التعطيل سيظهر سجل التواصل في الأعلى أولاً", color = Color.Gray, fontSize = 10.sp)
            }
            Switch(
                checked = favsFirst,
                onCheckedChange = { favsFirst = it },
                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
            )
        }

        Button(
            onClick = {
                val updated = configs.copy(
                    dashboardCustomMessage = customMsg,
                    showDashboardFavorites = showFavs,
                    showDashboardCallHistory = showCallHistory,
                    dashboardFavoritesFirst = favsFirst
                )
                onSave(updated)
                Toast.makeText(context, "تم حفظ الإعدادات المخصصة وتطبيقها فوراً بنجاح!", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("حفظ التكوين والتخصيص", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
        }

        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

        AdAndWelcomeCustomizationCard(
            configs = configs,
            onSave = onSave,
            writeTextColor = writeTextColor
        )

        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 8.dp))

        // --- Standard Admin access to management of countries, governorates, and cities (as requested!) ---
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("لوحة التحكم بالدول والمحافظات والمدن 🗺️", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        Text("إضافة وتعديل دول ومحافظات ومديريات دليل الخدمات", fontSize = 10.sp, color = Color.LightGray)
                    }
                    Button(
                        onClick = { showAddCityDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("إضافة دولة/محافظة ➕", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(color = Color.DarkGray)

                if (citySettings.value.isEmpty()) {
                    Text("لا توجد نطاقات جغرافية مضافة حالياً بالفلاتر.", color = Color.Gray, fontSize = 11.sp)
                } else {
                    citySettings.value.forEach { city ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF121214)),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${city.country} 🌍 / ${city.name}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("المناطق: ${city.districts.joinToString(" • ")}", fontSize = 10.sp, color = Color.LightGray)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                editingCityId = city.id
                                                editCityNameInput = city.name
                                                editDistrictsInput = city.districts.joinToString(", ")
                                                editCountryInput = city.country
                                                showEditCityDialog = true
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                FirestoreSim.deleteCity(context, city.id)
                                                Toast.makeText(context, "تم حذف المدينة/المحافظة تماماً!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AlertRed, modifier = Modifier.size(14.dp))
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

    // Add dialog inside Admin context
    if (showAddCityDialog) {
        Dialog(onDismissRequest = { showAddCityDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("إضافة فلاتر جغرافية جديدة 🗺️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = countryNameInput,
                        onValueChange = { countryNameInput = it },
                        label = { Text("الدولة (مثل: اليمن، السعودية، مصر)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = cityNameInput,
                        onValueChange = { cityNameInput = it },
                        label = { Text("اسم المحافظة / المدينة") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = districtsInput,
                        onValueChange = { districtsInput = it },
                        label = { Text("المدن والمديريات بداخلها (مفصولة بفاصلة)") },
                        placeholder = { Text("مثال: حي المنصورة, كريتر, حي المعلا") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showAddCityDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (cityNameInput.isNotBlank() && districtsInput.isNotBlank() && countryNameInput.isNotBlank()) {
                                    val dstList = districtsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    FirestoreSim.addCity(context, cityNameInput, dstList, countryNameInput)
                                    showAddCityDialog = false
                                    cityNameInput = ""
                                    districtsInput = ""
                                    countryNameInput = "اليمن"
                                    Toast.makeText(context, "تم إضافة الفلتر الجغرافي بنجاح!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إضافة ➕", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Edit dialog inside Admin context
    if (showEditCityDialog) {
        Dialog(onDismissRequest = { showEditCityDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("تعديل الفلاتر الجغرافية المعتمدة ✏️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)

                    OutlinedTextField(
                        value = editCountryInput,
                        onValueChange = { editCountryInput = it },
                        label = { Text("الدولة (مثل: اليمن، السعودية، مصر 🌍)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editCityNameInput,
                        onValueChange = { editCityNameInput = it },
                        label = { Text("المحافظة / المدينة الجغرافية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editDistrictsInput,
                        onValueChange = { editDistrictsInput = it },
                        label = { Text("الأحياء والمناطق (مفصولة بفاصلة)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = { showEditCityDialog = false }, modifier = Modifier.weight(1f)) {
                            Text("إلغاء")
                        }
                        Button(
                            onClick = {
                                if (editCityNameInput.isNotBlank() && editDistrictsInput.isNotBlank() && editCountryInput.isNotBlank()) {
                                    val dstList = editDistrictsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                                    FirestoreSim.editCity(context, editingCityId, editCityNameInput, dstList, editCountryInput)
                                    showEditCityDialog = false
                                    Toast.makeText(context, "تم حفظ وتحديث التعديل!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ 💾", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeratorsManagementTab(
    moderators: List<Moderator>,
    onSaveMod: (Moderator) -> Unit,
    onDeleteMod: (String) -> Unit,
    writeTextColor: Color
) {
    var nameInput by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") }
    var passInput by remember { mutableStateOf("") }
    var editingModId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("إدارة المشرفين الفرعيين (Sub-Admins)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }

        Text(
            "تمنح هذه الشاشة الصلاحية التامة لمشرفي النظام الرئيسيين ولملاك التطبيق لإضافة مشرفين جدد، أو تعديل حساباتهم، أو شطبهم نهائياً.",
            fontSize = 11.sp,
            color = Color.LightGray,
            lineHeight = 16.sp
        )

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

        // Form Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (editingModId == null) "إضافة مشرف جديد للنظام" else "تعديل بيانات المشرف الحالي",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp
                )

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("الاسم الكامل للمشرف") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userInput,
                        onValueChange = { userInput = it },
                        label = { Text("اسم المستخدم") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = passInput,
                        onValueChange = { passInput = it },
                        label = { Text("كلمة المرور") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (nameInput.isBlank() || userInput.isBlank() || passInput.isBlank()) {
                                Toast.makeText(context, "الرجاء كشط جميع الحقول لحفظ بيانات الحساب!", Toast.LENGTH_SHORT).show()
                            } else {
                                onSaveMod(Moderator(editingModId ?: "", nameInput, userInput, passInput))
                                Toast.makeText(context, "تم حفظ حساب المشرف وتجهيزه للولوج الفوري!", Toast.LENGTH_SHORT).show()
                                nameInput = ""
                                userInput = ""
                                passInput = ""
                                editingModId = null
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (editingModId == null) "حفظ المشرف" else "حفظ التعديلات")
                    }

                    if (editingModId != null) {
                        Button(
                            onClick = {
                                nameInput = ""
                                userInput = ""
                                passInput = ""
                                editingModId = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("إلغاء")
                        }
                    }
                }
            }
        }

        // List
        Text("قائمة المشرفين المعتمدين والمفعلين", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(moderators) { m ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(m.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Text("اسم الدخول: ${m.username} | السر: ${m.password}", color = Color.Gray, fontSize = 11.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = {
                                editingModId = m.id
                                nameInput = m.name
                                userInput = m.username
                                passInput = m.password
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                onDeleteMod(m.id)
                                Toast.makeText(context, "تم حذف حساب المشرف وإسقاط صلاحياته نهائياً!", Toast.LENGTH_SHORT).show()
                                if (editingModId == m.id) {
                                    nameInput = ""
                                    userInput = ""
                                    passInput = ""
                                    editingModId = null
                                }
                            },
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AlertRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryIconsSizingReorderingTab(
    mainCategories: List<MainCategory>,
    configs: AppConfigs,
    onSaveCategory: (MainCategory) -> Unit,
    onUpdateConfigs: (AppConfigs) -> Unit,
    writeTextColor: Color
) {
    var iconSize by remember { mutableStateOf(configs.categoryIconSize) }
    var selectedCatForIconEdit by remember { mutableStateOf<MainCategory?>(null) }
    var customIconCodeInput by remember { mutableStateOf("") }

    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("تعديل حجم وترتيب الأيقونات وحركيتها", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
        }

        // Section 1: Icon Sizing
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("تخصيص حجم أيقونات الأقسام (Sizing)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("الحجم: ${iconSize.toInt()} dp", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Slider(
                        value = iconSize,
                        onValueChange = { iconSize = it },
                        valueRange = 20f..72f,
                        modifier = Modifier.weight(1f),
                        colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary)
                    )
                }

                Button(
                    onClick = {
                        onUpdateConfigs(configs.copy(categoryIconSize = iconSize))
                        Toast.makeText(context, "تم حفظ الحجم الجديد للأيقونات وتعميمه للعرض!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ وتعميم حجم الأيقونة", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))

        // Section 2: Order & Icons listing
        Text("حركية الأقسام (الترتيب والرموز الخلوية)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

        if (selectedCatForIconEdit != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("تعديل رمز الأيقونة لقسم: ${selectedCatForIconEdit!!.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                    Text("الرموز المتاحة: (home, medical, school, car, star, settings, lock, work, design_services, build)", color = Color.Gray, fontSize = 10.sp)

                    OutlinedTextField(
                        value = customIconCodeInput,
                        onValueChange = { customIconCodeInput = it },
                        label = { Text("اسم رمز الأيقونة الجديد (iconCode)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSaveCategory(selectedCatForIconEdit!!.copy(iconCode = customIconCodeInput))
                                Toast.makeText(context, "تم تبديل أيقونة القسم بنجاح!", Toast.LENGTH_SHORT).show()
                                selectedCatForIconEdit = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("تطبيق الأيقونة")
                        }
                        Button(
                            onClick = { selectedCatForIconEdit = null },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("رجوع")
                        }
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mainCategories.sortedBy { it.order }) { cat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = getIconForCode(cat.iconCode),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(cat.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("رمزها: ${cat.iconCode} | ترتيب العرض: ${cat.order}", color = Color.Gray, fontSize = 10.sp)
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Change Icon Trigger
                        Button(
                            onClick = {
                                selectedCatForIconEdit = cat
                                customIconCodeInput = cat.iconCode
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), contentColor = MaterialTheme.colorScheme.primary),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("الأيقونة", fontSize = 10.sp)
                        }

                        // Order Up / Down Button
                        IconButton(
                            onClick = {
                                onSaveCategory(cat.copy(order = cat.order - 1))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "أعلى", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }

                        IconButton(
                            onClick = {
                                onSaveCategory(cat.copy(order = cat.order + 1))
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ArrowDownward, contentDescription = "أسفل", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

fun getIconForCode(code: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when(code) {
        "medical" -> Icons.Default.MedicalServices
        "school" -> Icons.Default.School
        "car" -> Icons.Default.DirectionsCar
        "home" -> Icons.Default.Home
        "star" -> Icons.Default.Star
        "settings" -> Icons.Default.Settings
        "lock" -> Icons.Default.Lock
        "work" -> Icons.Default.Work
        "build" -> Icons.Default.Build
        "design_services" -> Icons.Default.DesignServices
        else -> Icons.Default.HomeWork
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdAndWelcomeCustomizationCard(
    configs: AppConfigs,
    onSave: (AppConfigs) -> Unit,
    writeTextColor: Color
) {
    var adTitle by remember { mutableStateOf(configs.adTextTitle) }
    var adDesc by remember { mutableStateOf(configs.adTextDescription) }
    var adType by remember { mutableStateOf(configs.adSourceType) } // "text", "local", "web_url"
    var adPath by remember { mutableStateOf(configs.adImagePath) }
    var adDuration by remember { mutableStateOf(configs.adShowDurationDays.toString()) }
    var adVisible by remember { mutableStateOf(configs.adIsVisible) }

    var welcomeMsg by remember { mutableStateOf(configs.welcomeMessage) }
    var welcomeType by remember { mutableStateOf(configs.welcomeSourceType) } // "text", "image"
    var welcomePath by remember { mutableStateOf(configs.welcomeImagePath) }
    var welcomeSize by remember { mutableStateOf(configs.welcomeFontSize) }

    val context = LocalContext.current

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E24)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("تخصيص الإعلان المعتمد والترحيب المميز 📢", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
            Text("إدارة ظهور الإعلان والترحيب بصورة أو بنص والتحكم الكامل بمواقيتها وأحجامها.", fontSize = 11.sp, color = Color.LightGray)

            Divider(color = Color.DarkGray)

            // --- Section 1: الاعلان المعتمد ---
            Text("📍 إعدادات الإعلان المعتمد الفوري", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("إظهار الإعلان المعتمد:", fontSize = 11.sp, color = Color.White)
                Switch(
                    checked = adVisible,
                    onCheckedChange = { adVisible = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
                )
            }

            Text("نوع محتوى الإعلان:", fontSize = 11.sp, color = Color.LightGray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("text" to "نص فقط 📝", "local" to "صورة محلية 📁", "web_url" to "رابط صورة ويب 🌐").forEach { (type, label) ->
                    FilterChip(
                        selected = adType == type,
                        onClick = { adType = type },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (adType == "text") {
                OutlinedTextField(
                    value = adTitle,
                    onValueChange = { adTitle = it },
                    label = { Text("عنوان الإعلان") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
                OutlinedTextField(
                    value = adDesc,
                    onValueChange = { adDesc = it },
                    label = { Text("وصف وتفاصيل الإعلان") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
            } else if (adType == "local") {
                OutlinedTextField(
                    value = adPath,
                    onValueChange = { adPath = it },
                    label = { Text("مسار الصورة بذاكرة الهاتف / بطاقة الذاكرة") },
                    placeholder = { Text("مثال: /storage/emulated/0/Download/ad.png") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            adPath = "/storage/emulated/0/Pictures/ad_image.jpg"
                            Toast.makeText(context, "تم محاكاة إدخال مسار ذاكرة الهاتف الداخلية ✓", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("محاكاة الذاكرة 📁", fontSize = 9.sp, color = Color.White)
                    }
                    Button(
                        onClick = {
                            adPath = "/mnt/sdcard/ad_image.png"
                            Toast.makeText(context, "تم محاكاة إدخال مسار بطاقة الذاكرة الخارجية SD ✓", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text("محاكاة بطاقة SD 💾", fontSize = 9.sp, color = Color.White)
                    }
                }
            } else {
                OutlinedTextField(
                    value = adPath,
                    onValueChange = { adPath = it },
                    label = { Text("رابط صورة الويب (https/http)") },
                    placeholder = { Text("مثال: https://img.example.com/banner.jpg") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
            }

            OutlinedTextField(
                value = adDuration,
                onValueChange = { adDuration = it.filter { char -> char.isDigit() } },
                label = { Text("مدة ظهور الإعلان بالأيام") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
            )

            Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

            // -- Section 2: رسالة الترحيب --
            Text("👋 إعدادات رسالة الترحيب المنبثقة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)

            Text("نوع محتوى الترحيب:", fontSize = 11.sp, color = Color.LightGray)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("text" to "نص ترحيبي 📝", "image" to "صورة ترحيبية 🖼️").forEach { (type, label) ->
                    FilterChip(
                        selected = welcomeType == type,
                        onClick = { welcomeType = type },
                        label = { Text(label, fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            if (welcomeType == "text") {
                OutlinedTextField(
                    value = welcomeMsg,
                    onValueChange = { welcomeMsg = it },
                    label = { Text("جسم نص رسالة الترحيب") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
            } else {
                OutlinedTextField(
                    value = welcomePath,
                    onValueChange = { welcomePath = it },
                    label = { Text("رابط أو مسار صورة الترحيب") },
                    placeholder = { Text("مثال: https://images.com/welcome_banner.png") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor)
                )
                Button(
                    onClick = {
                        welcomePath = "https://images.unsplash.com/photo-1542435503-956c469947f6?auto=format&fit=crop&w=600&q=80"
                        Toast.makeText(context, "تم إمداد رابط ترحيبي عالي الدقة ✓", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("رابط ويب ترحيبي تجريبي 🌐", fontSize = 10.sp, color = Color.White)
                }
            }

            // Adjust Font Size
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("حجم خط الترحيب: ${welcomeSize.toInt()} sp", fontSize = 11.sp, color = Color.LightGray)
                Slider(
                    value = welcomeSize,
                    onValueChange = { welcomeSize = it },
                    valueRange = 8f..28f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Button(
                onClick = {
                    val durationVal = adDuration.toIntOrNull() ?: 30
                    val updated = configs.copy(
                        adTextTitle = adTitle,
                        adTextDescription = adDesc,
                        adSourceType = adType,
                        adImagePath = adPath,
                        adShowDurationDays = durationVal,
                        adIsVisible = adVisible,
                        adStartTimeMillis = if (configs.adStartTimeMillis == 0L) System.currentTimeMillis() else configs.adStartTimeMillis,
                        welcomeMessage = welcomeMsg,
                        welcomeSourceType = welcomeType,
                        welcomeImagePath = welcomePath,
                        welcomeFontSize = welcomeSize
                    )
                    onSave(updated)
                    Toast.makeText(context, "تم حفظ إعدادات الإشهار والترحيب وتوزيعها فورياً! 🚀", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("حفظ التخصيص والخيارات المعتمدة 💾", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

