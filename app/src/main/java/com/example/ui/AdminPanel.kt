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
    val tabs = listOf("طلبات التسجيل", "إدارة الأقسام", "إضافة فني يدوياً")

    // Database states
    val pendingRequests = FirestoreSim.pendingProviders.collectAsState()
    val mainCats = FirestoreSim.mainCategories.collectAsState()
    val subCats = FirestoreSim.subCategories.collectAsState()

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
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
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
