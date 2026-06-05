package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

// Centralized Material 3 Yemen-inspired Cosmic slate color scheme
val SpaceSlate = Color(0xFF0F172A)
val DeepIndigo = Color(0xFF1E293B)
val YemenRedAccent = Color(0xFFDC2626) // Patriotic highlight accents
val YemenGoldAccent = Color(0xFFD97706) // Rich gold outline tags
val RadiantSilver = Color(0xFFE2E8F0)
val SoftGrayText = Color(0xFF94A3B8)
val MutedGreen = Color(0xFF10B981)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // Flows observed as states
    val categoriesState by FirestoreSim.categories.collectAsState()
    val providersState by FirestoreSim.providers.collectAsState()
    val supervisorsState by FirestoreSim.supervisors.collectAsState()
    val backupHistoryState by FirestoreSim.backupHistory.collectAsState()
    val configState by FirestoreSim.configs.collectAsState()
    val schedulerAlertState by FirestoreSim.schedulerFailedAlert.collectAsState()
    val pendingProvidersState by FirestoreSim.pendingProviders.collectAsState()

    // Screen States
    var currentTab by remember { mutableStateOf("home") } // home, admin, register
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCity by remember { mutableStateOf("الكل") }

    // Admin Authentication State
    var activeSupervisor by remember { mutableStateOf<Supervisor?>(null) }
    var enteredPin by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf("") }

    // Backup & Restore states
    var chosenBackupPath by remember { mutableStateOf("Google Drive") }
    var backupStatusMessage by remember { mutableStateOf("") }
    var restoreBase64Input by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }

    // Config inputs
    var customAssistantIconId by remember { mutableStateOf("default") }
    var customAboutIconId by remember { mutableStateOf("default") }

    // Form states for Admin adding/viewing options
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var newCatIcon by remember { mutableStateOf("🛠️") }
    var newCatParentId by remember { mutableStateOf<String?>(null) }

    var showAddSupervisorDialog by remember { mutableStateOf(false) }
    var newSupName by remember { mutableStateOf("") }
    var newSupPin by remember { mutableStateOf("") }
    var newSupCanEditCats by remember { mutableStateOf(true) }
    var newSupCanDelProvs by remember { mutableStateOf(true) }
    var newSupCanBackup by remember { mutableStateOf(true) }
    var newSupCanModifyConfs by remember { mutableStateOf(true) }

    var showEditCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var editCatName by remember { mutableStateOf("") }
    var editCatIcon by remember { mutableStateOf("") }
    var editCatParentId by remember { mutableStateOf<String?>(null) }

    var selectedProviderForReview by remember { mutableStateOf<ServiceProvider?>(null) }
    var newReviewAuthor by remember { mutableStateOf("") }
    var newReviewRating by remember { mutableStateOf(5) }
    var newReviewComment by remember { mutableStateOf("") }

    // Provider Registration fields
    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regCatId by remember { mutableStateOf("") }
    var regSubCatId by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regNeighborhood by remember { mutableStateOf("") }
    var regCity by remember { mutableStateOf("صنعاء") }
    var regProfilePhoto by remember { mutableStateOf("") } // Selfie or image - Mandatory
    var regIdentityPhoto by remember { mutableStateOf("") } // Optional ID Card upload
    var regHasLocation by remember { mutableStateOf(false) } // Optional Google maps location
    var regLatitude by remember { mutableDoubleStateOf(15.3694) }
    var regLongitude by remember { mutableDoubleStateOf(44.1910) }
    var registrationSuccessMsg by remember { mutableStateOf("") }

    // Selected provider from map pin
    var selectedProviderFromMap by remember { mutableStateOf<ServiceProvider?>(null) }

    val cities = listOf("الكل", "صنعاء", "عدن", "تعز", "الحديدة", "حضرموت", "إب")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = configState.appName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 21.sp
                            )
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (activeSupervisor != null) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = YemenRedAccent),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = activeSupervisor!!.name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(onClick = {
                                    activeSupervisor = null
                                    enteredPin = ""
                                    currentTab = "home"
                                }) {
                                    Icon(
                                        Icons.Default.Logout,
                                        contentDescription = "خروج المشرف",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepIndigo)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DeepIndigo,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = currentTab == "home",
                    onClick = { currentTab = "home" },
                    label = { Text("الرئيسية", color = Color.White) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) }
                )
                NavigationBarItem(
                    selected = currentTab == "register",
                    onClick = { currentTab = "register" },
                    label = { Text("انضم كفني", color = Color.White) },
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = "Register", tint = Color.White) }
                )
                NavigationBarItem(
                    selected = currentTab == "admin",
                    onClick = { currentTab = "admin" },
                    label = { Text("لوحة التحكم", color = Color.White) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin", tint = Color.White) }
                )
            }
        },
        containerColor = SpaceSlate
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Background cosmic visual gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(SpaceSlate, DeepIndigo)
                        )
                    )
            )

            // Dynamic Content Rendering
            when (currentTab) {
                "home" -> HomeScreen(
                    categories = categoriesState,
                    providers = providersState,
                    config = configState,
                    selectedCategoryId = selectedCategoryId,
                    onSelectCategory = { selectedCategoryId = it },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    selectedCity = selectedCity,
                    onCityChange = { selectedCity = it },
                    cities = cities,
                    onAddReviewClick = { selectedProviderForReview = it }
                )

                "register" -> RegistrationScreen(
                    categories = categoriesState,
                    cities = cities.filter { it != "الكل" },
                    regName = regName,
                    onRegNameChange = { regName = it },
                    regPhone = regPhone,
                    onRegPhoneChange = { regPhone = it },
                    regCatId = regCatId,
                    onRegCatIdChange = { regCatId = it },
                    regSubCatId = regSubCatId,
                    onRegSubCatIdChange = { regSubCatId = it },
                    regAddress = regAddress,
                    onRegAddressChange = { regAddress = it },
                    regNeighborhood = regNeighborhood,
                    onRegNeighborhoodChange = { regNeighborhood = it },
                    regCity = regCity,
                    onRegCityChange = { regCity = it },
                    regProfilePhoto = regProfilePhoto,
                    onRegProfilePhotoChange = { regProfilePhoto = it },
                    regIdentityPhoto = regIdentityPhoto,
                    onRegIdentityPhotoChange = { regIdentityPhoto = it },
                    regHasLocation = regHasLocation,
                    onRegHasLocationChange = { regHasLocation = it },
                    regLatitude = regLatitude,
                    onRegLatitudeChange = { regLatitude = it },
                    regLongitude = regLongitude,
                    onRegLongitudeChange = { regLongitude = it },
                    successMessage = registrationSuccessMsg,
                    onSubmit = {
                        if (regName.isNotBlank() && regPhone.isNotBlank() && regCatId.isNotBlank() && regSubCatId.isNotBlank() && regAddress.isNotBlank() && regNeighborhood.isNotBlank() && regProfilePhoto.isNotBlank()) {
                            FirestoreSim.addPendingProvider(
                                name = regName,
                                phone = regPhone,
                                categoryId = regSubCatId, // Using subcategory directly as categoryId for precise service mapping
                                address = regAddress,
                                neighborhood = regNeighborhood,
                                city = regCity,
                                profilePhoto = regProfilePhoto,
                                identityPhoto = if (regIdentityPhoto.isNotBlank()) regIdentityPhoto else null,
                                hasLocation = regHasLocation,
                                latitude = regLatitude,
                                longitude = regLongitude
                            )
                            FirestoreSim.simulateSchedulerFailure("Google Drive")
                            registrationSuccessMsg = "تم إرسال طلب انضمامك بنجاح! سينظر مشرف الدليل في طلبك ويقوم بتفعيله."
                            regName = ""
                            regPhone = ""
                            regCatId = ""
                            regSubCatId = ""
                            regAddress = ""
                            regNeighborhood = ""
                            regProfilePhoto = ""
                            regIdentityPhoto = ""
                            regHasLocation = false
                        } else {
                            registrationSuccessMsg = "خطأ: يرجى كتابة الاسم الثلاثي، رقم الهاتف، القسم، الخدمة، عنوان العمل، الحي، وتحميل الصورة الشخصية (سيلفي أو من الهاتف)!"
                        }
                    }
                )

                "admin" -> {
                    if (activeSupervisor == null) {
                        AdminAuthScreen(
                            enteredPin = enteredPin,
                            onPinChange = { enteredPin = it },
                            error = authError,
                            onLoginClick = {
                                val found = supervisorsState.find { it.pinCode == enteredPin } ?: if (enteredPin == "maher--736462") {
                                    supervisorsState.find { it.id == "sup1" } ?: Supervisor("sup1", "الأدمن العام", "maher--736462", canEditCategories = true, canDeleteProviders = true, canViewBackup = true, canModifyConfigs = true)
                                } else null
                                if (found != null) {
                                    activeSupervisor = found
                                    authError = ""
                                } else {
                                    authError = "رمز أو كلمة مرور لوحة التحكم خاطئة! يرجى إدخال كلمة المرور الصحيحة للوصول الدائم للوحة التحكم."
                                }
                            }
                        )
                    } else {
                        AdminControlsDashboard(
                            activeSup = activeSupervisor!!,
                            categories = categoriesState,
                            providers = providersState,
                            supervisors = supervisorsState,
                            backupHistory = backupHistoryState,
                            schedulerAlert = schedulerAlertState,
                            pendingRequests = pendingProvidersState,
                            configs = configState,
                            chosenBackupPath = chosenBackupPath,
                            onBackupPathChange = { chosenBackupPath = it },
                            backupStatus = backupStatusMessage,
                            onTriggerBackup = {
                                val success = FirestoreSim.triggerBackup(chosenBackupPath)
                                backupStatusMessage = if (success) {
                                    "تم الحفظ بنجاح إلى غاية [$chosenBackupPath]!"
                                } else {
                                    "تنبيه: فشل النسخ الاحتياطي في [$chosenBackupPath] لعدم رخصة الدخول."
                                }
                            },
                            onClearSchedulerAlert = { FirestoreSim.clearSchedulerAlert() },
                            onRestoreClick = { showRestoreDialog = true },
                            onToggleMaps = { FirestoreSim.toggleGoogleMaps(it) },
                            onUpdateAssistant = { scale, size, hide, customBase64 ->
                                FirestoreSim.updateSmartAssistantStyle(scale, size, hide, customBase64)
                            },
                            onUpdateAboutApp = { scale, size, hide, customBase64 ->
                                FirestoreSim.updateAboutAppStyle(scale, size, hide, customBase64)
                            },
                            onAddCategoryClick = { showAddCategoryDialog = true },
                            onEditCategoryClick = { cat ->
                                editCatName = cat.name
                                editCatIcon = cat.icon
                                editCatParentId = cat.parentId
                                showEditCategoryDialog = cat
                            },
                            onDeleteCategory = { FirestoreSim.deleteCategory(it) },
                            onToggleCategoryPin = { FirestoreSim.toggleCategoryPin(it) },
                            onToggleProviderPremium = { FirestoreSim.toggleProviderPremium(it) },
                            onToggleProviderPin = { FirestoreSim.toggleProviderPin(it) },
                            onUpdateProviderSubscription = { id, status ->
                                FirestoreSim.updateProviderSubscription(id, status)
                            },
                            onDeleteProvider = { FirestoreSim.deleteProvider(it) },
                            onApproveJoinRequest = { FirestoreSim.approveJoinRequest(it) },
                            onRejectJoinRequest = { id, reason -> FirestoreSim.rejectJoinRequest(id, reason) },
                            onAddSupervisorClick = { showAddSupervisorDialog = true },
                            onDeleteSupervisor = { FirestoreSim.deleteSupervisor(it) },
                            onUpdateSupervisorPass = { id, name, pass, edits, dels, back, confs ->
                                FirestoreSim.updateSupervisor(id, name, pass, edits, dels, back, confs)
                            }
                        )
                    }
                }
            }

            // --- Floating Assistant Render (Dynamic Customizations instant application) ---
            if (!configState.assistantHidden) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                        .alpha(configState.assistantTransparency)
                        .size((64 * configState.assistantScale).dp)
                        .clip(CircleShape)
                        .background(Color(0xFFDC2626).copy(alpha = 0.9f))
                        .border(2.dp, YemenGoldAccent, CircleShape)
                        .clickable {
                            // Instant dialogue about app helper
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (configState.customAssistantIconBase64 != null) {
                        Text(
                            text = configState.customAssistantIconBase64 ?: "🤖",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = (22 * configState.assistantScale).sp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = "مساعد ذكي يمني",
                            tint = Color.White,
                            modifier = Modifier.size((28 * configState.assistantScale).dp)
                        )
                    }
                }
            }

            // --- Floating About Application Widget (Dynamic Customizations) ---
            if (!configState.aboutAppHidden) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 80.dp)
                        .alpha(configState.aboutAppTransparency)
                        .size((64 * configState.aboutAppScale).dp)
                        .clip(CircleShape)
                        .background(DeepIndigo.copy(alpha = 0.9f))
                        .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        .clickable {
                            // Custom info widget modal shown
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (configState.aboutAppIconBase64 != null) {
                        Text(
                            text = configState.aboutAppIconBase64 ?: "ℹ️",
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = (22 * configState.aboutAppScale).sp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "عن التطبيق",
                            tint = Color.White,
                            modifier = Modifier.size((28 * configState.aboutAppScale).dp)
                        )
                    }
                }
            }

            // Standard dialogues
            if (showRestoreDialog) {
                AlertDialog(
                    onDismissRequest = { showRestoreDialog = false },
                    title = { Text("استرجاع قاعدة البيانات", style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Right) },
                    text = {
                        Column {
                            Text("أدخل كود استرجاع قاعدة البيانات المشفر (Base64) الذي قمت بنسخه سابقاً للحفظ التام:")
                            Spacer(modifier = Modifier.height(8.dp))
                            TextField(
                                value = restoreBase64Input,
                                onValueChange = { restoreBase64Input = it },
                                placeholder = { Text("كود النسخة الاحتياطية هنا...") },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 4
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val success = FirestoreSim.restoreBackupFromBase64(restoreBase64Input)
                            backupStatusMessage = if (success) {
                                "تم استعادة النسخة الاحتياطية بنجاح تام!"
                            } else {
                                "تنبيه: الرمز المكتوب غير متوافق مع نظام النسخ لدينا."
                            }
                            showRestoreDialog = false
                            restoreBase64Input = ""
                        }) {
                            Text("استعادة واسترجاع")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (selectedProviderForReview != null) {
                AlertDialog(
                    onDismissRequest = { selectedProviderForReview = null },
                    title = { Text("إضافة تقييم للخدمة", style = MaterialTheme.typography.titleMedium) },
                    text = {
                        Column {
                            Text("تقييم عمل: ${selectedProviderForReview!!.name}")
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newReviewAuthor,
                                onValueChange = { newReviewAuthor = it },
                                label = { Text("اسمك") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("التقييم بالنجوم:")
                            Row {
                                for (star in 1..5) {
                                    IconButton(onClick = { newReviewRating = star }) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "$star Stars",
                                            tint = if (star <= newReviewRating) YemenGoldAccent else Color.Gray
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newReviewComment,
                                onValueChange = { newReviewComment = it },
                                label = { Text("التعليق والملحوظة") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newReviewAuthor.isNotBlank() && newReviewComment.isNotBlank()) {
                                FirestoreSim.addReview(
                                    selectedProviderForReview!!.id,
                                    newReviewAuthor,
                                    newReviewRating,
                                    newReviewComment
                                )
                                selectedProviderForReview = null
                                newReviewAuthor = ""
                                newReviewComment = ""
                            }
                        }) {
                            Text("نشر التقييم")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedProviderForReview = null }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (showAddCategoryDialog) {
                AlertDialog(
                    onDismissRequest = { showAddCategoryDialog = false },
                    title = { Text("إضافة تصنيف مهني جديد") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newCatName,
                                onValueChange = { newCatName = it },
                                label = { Text("اسم القسم (مثلاً: سباكة)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newCatIcon,
                                onValueChange = { newCatIcon = it },
                                label = { Text("أيقونة إيموجي ممثلة (مثل: 🚰)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("هل هو قسم فرعي؟ (اختر القسم الرئيسي)")
                            LazyRow {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (newCatParentId == null) YemenGoldAccent else DeepIndigo
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { newCatParentId = null }
                                    ) {
                                        Text("[قسم رئيسي أساسي]", modifier = Modifier.padding(8.dp), color = Color.White)
                                    }
                                }
                                items(categoriesState.filter { it.parentId == null }) { cat ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (newCatParentId == cat.id) YemenGoldAccent else DeepIndigo
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { newCatParentId = cat.id }
                                    ) {
                                        Text(cat.name, modifier = Modifier.padding(8.dp), color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newCatName.isNotBlank()) {
                                FirestoreSim.addCategory(newCatName, newCatIcon, newCatParentId)
                                showAddCategoryDialog = false
                                newCatName = ""
                                newCatIcon = "🛠️"
                                newCatParentId = null
                            }
                        }) {
                            Text("إضافة")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (showEditCategoryDialog != null) {
                AlertDialog(
                    onDismissRequest = { showEditCategoryDialog = null },
                    title = { Text("تعديل القسم") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = editCatName,
                                onValueChange = { editCatName = it },
                                label = { Text("اسم القسم") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = editCatIcon,
                                onValueChange = { editCatIcon = it },
                                label = { Text("أيقونة القسم") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("القسم الرئيسي الحالي:")
                            LazyRow {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (editCatParentId == null) YemenGoldAccent else DeepIndigo
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { editCatParentId = null }
                                    ) {
                                        Text("[قسم رئيسي أساسي]", modifier = Modifier.padding(8.dp), color = Color.White)
                                    }
                                }
                                items(categoriesState.filter { it.parentId == null && it.id != showEditCategoryDialog!!.id }) { cat ->
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (editCatParentId == cat.id) YemenGoldAccent else DeepIndigo
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { editCatParentId = cat.id }
                                    ) {
                                        Text(cat.name, modifier = Modifier.padding(8.dp), color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (editCatName.isNotBlank()) {
                                FirestoreSim.updateCategory(
                                    showEditCategoryDialog!!.id,
                                    editCatName,
                                    editCatIcon,
                                    editCatParentId
                                )
                                showEditCategoryDialog = null
                            }
                        }) {
                            Text("تطبيق التعديل")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showEditCategoryDialog = null }) {
                            Text("إلغاء")
                        }
                    }
                )
            }

            if (showAddSupervisorDialog) {
                AlertDialog(
                    onDismissRequest = { showAddSupervisorDialog = false },
                    title = { Text("إضافة مشرف جديد للدليل") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newSupName,
                                onValueChange = { newSupName = it },
                                label = { Text("اسم المشرف") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newSupPin,
                                onValueChange = { newSupPin = it },
                                label = { Text("رمز المرور PIN (رقمي)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("صلاحيات المشرف:", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = newSupCanEditCats, onCheckedChange = { newSupCanEditCats = it })
                                Text("تعديل الأقسام والمستويات")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = newSupCanDelProvs, onCheckedChange = { newSupCanDelProvs = it })
                                Text("إيقاف وحذف مقدمي الخدمات")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = newSupCanBackup, onCheckedChange = { newSupCanBackup = it })
                                Text("نسخ احتياطي واسترجاع البيانات")
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = newSupCanModifyConfs, onCheckedChange = { newSupCanModifyConfs = it })
                                Text("تغيير حجم ومكان شارات المساعد")
                            }
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            if (newSupName.isNotBlank() && newSupPin.isNotBlank()) {
                                FirestoreSim.addSupervisor(
                                    newSupName,
                                    newSupPin,
                                    newSupCanEditCats,
                                    newSupCanDelProvs,
                                    newSupCanBackup,
                                    newSupCanModifyConfs
                                )
                                showAddSupervisorDialog = false
                                newSupName = ""
                                newSupPin = ""
                            }
                        }) {
                            Text("حفظ الحساب")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddSupervisorDialog = false }) {
                            Text("إلغاء")
                        }
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Welcome Banner Component allowing both text and pictures from phone memory
// -------------------------------------------------------------
@Composable
fun WelcomeBannerView(config: AppConfigs, modifier: Modifier = Modifier) {
    if (!config.showWelcomeBanner) return

    Card(
        colors = CardDefaults.cardColors(containerColor = DeepIndigo),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .border(1.dp, YemenGoldAccent.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!config.welcomeImageBase64.isNullOrBlank()) {
                val imageBitmap = remember(config.welcomeImageBase64) {
                    try {
                        val decodedBytes = android.util.Base64.decode(config.welcomeImageBase64, android.util.Base64.DEFAULT)
                        val bitmap = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        bitmap?.asImageBitmap()
                    } catch (e: Exception) {
                        null
                    }
                }
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Welcome background image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = config.welcomeText,
                        color = Color.White,
                        fontSize = config.welcomeTextSize.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    text = config.welcomeText,
                    color = Color.White,
                    fontSize = config.welcomeTextSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Users Area - Home Screen Component
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    categories: List<Category>,
    providers: List<ServiceProvider>,
    config: AppConfigs,
    selectedCategoryId: String?,
    onSelectCategory: (String?) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCity: String,
    onCityChange: (String) -> Unit,
    cities: List<String>,
    onAddReviewClick: (ServiceProvider) -> Unit
) {
    var selectedProviderForMapDetail by remember { mutableStateOf<ServiceProvider?>(null) }

    // Sort categories: Pinned categories first, then the rest
    val sortedCategories = categories.filter { it.parentId == null }.sortedWith(
        compareByDescending<Category> { it.isPinned }.thenBy { it.name }
    )

    // Filter subcategories of the selected category
    val currentSubcategories = if (selectedCategoryId != null) {
        categories.filter { it.parentId == selectedCategoryId }
    } else emptyList()

    // Providers filter is ordered by: Pinned first, then Premium (الشارة المميزة) first, then by rating
    val filteredProviders = providers.filter {
        val matchesCategory = selectedCategoryId == null || 
                it.categoryId == selectedCategoryId || 
                categories.any { cat -> cat.parentId == selectedCategoryId && it.categoryId == cat.id }
        
        val matchesSearch = it.name.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true) ||
                it.neighborhood.contains(searchQuery, ignoreCase = true)

        val matchesCity = selectedCity == "الكل" || it.city == selectedCity

        // Block suspended providers from search
        val isNotSuspended = it.subscriptionStatus == "active"

        matchesCategory && matchesSearch && matchesCity && isNotSuspended
    }.sortedWith(
        // Search order algorithm prioritizing Pinned and Premium (مميز) items
        compareByDescending<ServiceProvider> { it.isPinned }
            .thenByDescending { it.isPremium }
            .thenByDescending { it.rating }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Welcomer position top
        if (config.welcomePosition == "top") {
            item {
                WelcomeBannerView(config = config)
            }
        }

        // Search Section Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ابحث عن أفضل الفنيين اليمنيين القريبين منك",
                        color = YemenGoldAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Right,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = { Text("بحث بالاسم، الحي، الشارع...", color = SoftGrayText) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = YemenGoldAccent) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = YemenGoldAccent,
                            unfocusedBorderColor = SoftGrayText,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SpaceSlate, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("اختر المدينة يدوياً:", color = Color.White, style = MaterialTheme.typography.bodySmall)
                    LazyRow {
                        items(cities) { city ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (selectedCity == city) YemenRedAccent else SpaceSlate
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { onCityChange(city) }
                            ) {
                                Text(
                                    text = city,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Welcomer position below_search
        if (config.welcomePosition == "below_search") {
            item {
                WelcomeBannerView(config = config)
            }
        }

        // Google Maps Simulation Panel (can be disabled by Admin)
        item {
            AnimatedContent(targetState = config.googleMapsEnabled, label = "map_toggle") { enabled ->
                if (enabled) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .padding(bottom = 12.dp)
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .background(Color(0xFF242F3E))
                            ) {
                                // Draw stylized Sana'a city grid view dynamically
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height

                                    // Draw background streets
                                    drawRect(Color(0xFF242F3E))
                                    drawLine(Color(0xFF38414E), Offset(w * 0.15f, 0f), Offset(w * 0.15f, h), strokeWidth = 12f)
                                    drawLine(Color(0xFF38414E), Offset(w * 0.5f, 0f), Offset(w * 0.5f, h), strokeWidth = 24f) // Main Sabeen/Hadda St
                                    drawLine(Color(0xFF38414E), Offset(0f, h * 0.4f), Offset(w, h * 0.4f), strokeWidth = 16f)
                                    drawLine(Color(0xFF38414E), Offset(0f, h * 0.75f), Offset(w, h * 0.75f), strokeWidth = 14f)

                                    // Draw concentric radar waves for current user location
                                    drawCircle(
                                        color = YemenRedAccent,
                                        radius = 24f,
                                        center = Offset(w * 0.45f, h * 0.5f)
                                    )
                                    drawCircle(
                                        color = YemenRedAccent.copy(alpha = 0.3f),
                                        radius = 50f,
                                        center = Offset(w * 0.45f, h * 0.5f),
                                        style = Stroke(width = 3f)
                                    )

                                    // Map simulated markers for active providers
                                    // Plotting locations
                                    drawCircle(Color(0xFF10B981), radius = 10f, center = Offset(w * 0.25f, h * 0.3f)) // Fouad Plumber
                                    drawCircle(Color(0xFFD97706), radius = 12f, center = Offset(w * 0.65f, h * 0.25f)) // Saleh Electric
                                    drawCircle(Color(0xFF10B981), radius = 10f, center = Offset(w * 0.78f, h * 0.7f)) // Najeeb Carpentry
                                }

                                // Interactive Info detail overlaid
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(SpaceSlate.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                                        .padding(6.dp)
                                ) {
                                    Text("خريطة التغطية المباشرة", color = YemenGoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("مقدمو الخدمة القريبون: ${filteredProviders.size}", color = Color.White, fontSize = 10.sp)
                                }

                                // Selection overlay
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(DeepIndigo.copy(alpha = 0.9f))
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        text = "المس على الخريطة لتحديد أقرب فني إليك في ${selectedCity}",
                                        color = SoftGrayText,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = YemenRedAccent.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .border(1.dp, YemenRedAccent.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "معطلة", tint = YemenRedAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "تغطية الخرائط الجغرافية معطلة حالياً من قبل الإدارة لتحديث الاتصال.",
                                color = Color.White,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Welcomer position below_map
        if (config.welcomePosition == "below_map") {
            item {
                WelcomeBannerView(config = config)
            }
        }

        // Primary categories row with representation emoji
        item {
            Text(
                text = "الفئات المهنية الرئيسية 🗂️",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategoryId == null) YemenGoldAccent else DeepIndigo
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onSelectCategory(null) }
                    ) {
                        Text(
                            text = "الكل 📋",
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(sortedCategories) { cat ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCategoryId == cat.id) YemenGoldAccent else DeepIndigo
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable { onSelectCategory(cat.id) }
                            .border(
                                width = if (cat.isPinned) 1.5.dp else 0.dp,
                                color = if (cat.isPinned) YemenGoldAccent else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(text = cat.icon, modifier = Modifier.padding(end = 4.dp))
                            Text(text = cat.name, color = Color.White, fontWeight = FontWeight.Bold)
                            if (cat.isPinned) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = YemenGoldAccent,
                                    modifier = Modifier
                                        .size(12.dp)
                                        .padding(start = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Render current Sub-Categories if any
        if (currentSubcategories.isNotEmpty()) {
            item {
                Text(
                    text = "الأقسام الفرعية المتاحة:",
                    color = SoftGrayText,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
                    items(currentSubcategories) { sub ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable { /* Filter sub actions */ }
                        ) {
                            Text(
                                text = "${sub.icon} ${sub.name}",
                                modifier = Modifier.padding(8.dp),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Active List of Providers
        item {
            Text(
                text = "الفنيون المتاحون حالياً (${filteredProviders.size}):",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        if (filteredProviders.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonSearch, contentDescription = "خالي", tint =SoftGrayText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "عذراً، لم يتم العثور على أي مقدمي خدمات يطابقون خيارات التصفية الحالية.",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredProviders) { prov ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (prov.isPinned) DeepIndigo else DeepIndigo.copy(alpha = 0.8f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .border(
                            width = if (prov.isPinned || prov.isPremium) 1.5.dp else 0.dp,
                            color = when {
                                prov.isPinned -> اليمنGold()
                                prov.isPremium -> YemenRedAccent
                                else -> Color.Transparent
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👨‍🔧", fontSize = 21.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = prov.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        if (prov.isPinned) {
                                            IconButton(onClick = {}) {
                                                Icon(
                                                    imageVector = Icons.Default.PushPin,
                                                    contentDescription = "Pinned at Top",
                                                    tint = YemenGoldAccent,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${prov.city} - ${prov.neighborhood}",
                                        color = SoftGrayText,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            // Star/Featured Badge tags
                            Column(horizontalAlignment = Alignment.End) {
                                if (prov.isPremium) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = YemenRedAccent),
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = "مميز 🌟",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = YemenGoldAccent, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = " ${prov.rating}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "العنوان: ${prov.address}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { onAddReviewClick(prov) }) {
                                Icon(Icons.Default.RateReview, contentDescription = "تقييم", tint = SoftGrayText)
                            }

                            Row {
                                Button(
                                    onClick = { /* simulated WhatsApp contact open */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = MutedGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Icon(Icons.Default.Chat, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("واتساب", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { /* call direct action */ },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                ) {
                                    Icon(Icons.Default.Phone, contentDescription = "اتصال", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اتصال", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tiny elegant footer required by config
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(config.footerTransparency),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = config.footerText,
                    color = SoftGrayText,
                    fontSize = (12 * config.footerScale).sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "جميع الحقوق محفوظة © دليل الخدمات اليمني",
                    color = SoftGrayText.copy(alpha = 0.7f),
                    fontSize = (10 * config.footerScale).sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Providers Registration Screen Area
// -------------------------------------------------------------
@Composable
fun RegistrationScreen(
    categories: List<Category>,
    cities: List<String>,
    regName: String,
    onRegNameChange: (String) -> Unit,
    regPhone: String,
    onRegPhoneChange: (String) -> Unit,
    regCatId: String,
    onRegCatIdChange: (String) -> Unit,
    regSubCatId: String,
    onRegSubCatIdChange: (String) -> Unit,
    regAddress: String,
    onRegAddressChange: (String) -> Unit,
    regNeighborhood: String,
    onRegNeighborhoodChange: (String) -> Unit,
    regCity: String,
    onRegCityChange: (String) -> Unit,
    regProfilePhoto: String,
    onRegProfilePhotoChange: (String) -> Unit,
    regIdentityPhoto: String,
    onRegIdentityPhotoChange: (String) -> Unit,
    regHasLocation: Boolean,
    onRegHasLocationChange: (Boolean) -> Unit,
    regLatitude: Double,
    onRegLatitudeChange: (Double) -> Unit,
    regLongitude: Double,
    onRegLongitudeChange: (Double) -> Unit,
    successMessage: String,
    onSubmit: () -> Unit
) {
    val mainCategories = categories.filter { it.parentId == null }
    val subCategories = categories.filter { it.parentId == regCatId && regCatId.isNotBlank() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Icon(
                imageVector = Icons.Default.Engineering,
                contentDescription = "الانضمام للدليل",
                tint = YemenGoldAccent,
                modifier = Modifier
                    .size(64.dp)
                    .padding(bottom = 12.dp)
            )
            Text(
                text = "سجل كفني ومزود خدمة يمني",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "سجّل بياناتك اليوم وانضم لعشرات الفنيين المعتمدين والموثقين في المحافظات اليمنية.",
                color = SoftGrayText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 6.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. المعلومات الشخصية (اجباري)", color = YemenGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    AppTextField(
                        value = regName,
                        onValueChange = onRegNameChange,
                        label = "الاسم الثلاثي الكامل (إجباري)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = regPhone,
                        onValueChange = onRegPhoneChange,
                        label = "رقم الهاتف والاتصال (إجباري)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("2. تحديد التخصصات المهنية والخدمات (اجباري)", color = YemenGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("اختر القسم الأساسي (المستوى الأول - إجباري):", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow {
                        items(mainCategories) { cat ->
                            val isSelected = regCatId == cat.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) YemenGoldAccent else SpaceSlate
                                ),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { 
                                        onRegCatIdChange(cat.id)
                                        onRegSubCatIdChange("") // Reset sub category when parent shifts
                                    }
                            ) {
                                Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(cat.icon)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(cat.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (regCatId.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("اختر الخدمة الفرعية المحددة (المستوى الثاني - إجباري):", color = Color.White, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (subCategories.isEmpty()) {
                            Text("لا توجد خدمات فرعية حالياً لهذا القسم الرئيسي.", color = SoftGrayText, fontSize = 10.sp)
                        } else {
                            LazyRow {
                                items(subCategories) { sub ->
                                    val isSelected = regSubCatId == sub.id
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) MutedGreen else SpaceSlate
                                        ),
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .clickable { onRegSubCatIdChange(sub.id) }
                                    ) {
                                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Text(sub.icon)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(sub.name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("3. الصورة الشخصية الرسمية (إجباري - لتثبيتها بملفك)", color = YemenGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("انقر أدناه لأخذ صورة سيلفي مباشرة أو اختيار ملف من جهازك لتضمينها بملفك المهني:", color = SoftGrayText, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onRegProfilePhotoChange("selfie_simulated_" + UUID.randomUUID().toString().take(4))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("التقاط صورة سيلفي 📸", fontSize = 10.sp)
                        }
                        Button(
                            onClick = {
                                onRegProfilePhotoChange("gallery_simulated_" + UUID.randomUUID().toString().take(4))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceSlate),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اختيار من المعرض 🖼️", fontSize = 10.sp)
                        }
                    }

                    if (regProfilePhoto.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MutedGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Checked", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("تم إرفاق الصورة الشخصية بنجاح ✅", color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("رمز الصورة: [${regProfilePhoto}]", color = SoftGrayText, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("4. عنوان ومقر العمل الدائم (إجباري)", color = YemenGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    AppTextField(
                        value = regAddress,
                        onValueChange = onRegAddressChange,
                        label = "عنوان مكان العمل وتواجد المركز الرئيسي (شارع/المركز)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    AppTextField(
                        value = regNeighborhood,
                        onValueChange = onRegNeighborhoodChange,
                        label = "اسم الحي أو الحارة السكنية الحالية (إجباري)"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("اختر المحافظة / المدينة اليمنية:", color = Color.White, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        cities.forEach { city ->
                            val isSelected = regCity == city
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) YemenRedAccent else SpaceSlate
                                ),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clickable { onRegCityChange(city) }
                            ) {
                                Text(city, modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("5. خرائط جوجل وصورة الهوية (اختياري)", color = YemenGoldAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ترسيم موقعك الدقيق على الخريطة وتحميل إثبات هويتك يساعدان في زيادة الثقة والظهور بالمقدمة:", color = SoftGrayText, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Location SIM Card Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onRegHasLocationChange(!regHasLocation) }
                    ) {
                        Checkbox(
                            checked = regHasLocation,
                            onCheckedChange = { onRegHasLocationChange(it) },
                            colors = CheckboxDefaults.colors(checkedColor = YemenGoldAccent)
                        )
                        Text("الترسيم الجغرافي: تحديد موقعي الحالي على الخريطة 📍", color = Color.White, fontSize = 11.sp)
                    }

                    if (regHasLocation) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("تم رصد الإحداثيات لمحاكاة الخرائط: (15.3694, 44.1910) 🗺️", color = YemenGoldAccent, fontSize = 10.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    // ID upload simulations
                    Button(
                        onClick = {
                            onRegIdentityPhotoChange("identity_card_verified_" + UUID.randomUUID().toString().take(4))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SpaceSlate),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CardMembership, contentDescription = "ID Card", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (regIdentityPhoto.isBlank()) "إرفاق صورة الهوية الوطنية (اختياري) 🪪" else "تم إثبات صورة الهوية بنجاح ✅", fontSize = 11.sp)
                    }

                    if (regIdentityPhoto.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("ملف الهوية المرفق: [${regIdentityPhoto}]", color = MutedGreen, fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onSubmit,
                        colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إرسال طلب الانضمام كفني محترف", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    if (successMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (successMessage.contains("خطأ")) YemenRedAccent.copy(alpha = 0.2f) else MutedGreen.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = successMessage,
                                color = if (successMessage.contains("خطأ")) YemenRedAccent else MutedGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Admin Area - PIN Entry Authenticater Screen
// -------------------------------------------------------------
@Composable
fun AdminAuthScreen(
    enteredPin: String,
    onPinChange: (String) -> Unit,
    error: String,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.AdminPanelSettings,
            contentDescription = "Secured",
            tint = YemenGoldAccent,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "خاص بالمشرفين وإدارة الدليل",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text(
            text = "الرجاء إدخال رمز المرور رقمي (PIN) المأذون لك به للوصول إلى صلاحيات التعديل والتحكم والنسخ.",
            color = SoftGrayText,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = DeepIndigo),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.widthIn(max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AppTextField(
                    value = enteredPin,
                    onValueChange = onPinChange,
                    label = "رمز المرور للوحة التحكم",
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                if (error.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = YemenRedAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onLoginClick,
                    colors = ButtonDefaults.buttonColors(containerColor = YemenGoldAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("دخول للوحة التحكم", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "ملاحظة للتجربة: أدمن عام (1111) | مشرف عادي (2222)",
            color = SoftGrayText,
            fontSize = 11.sp
        )
    }
}

// -------------------------------------------------------------
// Admin Controls Screen Tab Controller Component
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminControlsDashboard(
    activeSup: Supervisor,
    categories: List<Category>,
    providers: List<ServiceProvider>,
    supervisors: List<Supervisor>,
    backupHistory: List<BackupHistory>,
    schedulerAlert: String?,
    pendingRequests: List<PendingProvider>,
    configs: AppConfigs,
    chosenBackupPath: String,
    onBackupPathChange: (String) -> Unit,
    backupStatus: String,
    onTriggerBackup: () -> Unit,
    onClearSchedulerAlert: () -> Unit,
    onRestoreClick: () -> Unit,
    onToggleMaps: (Boolean) -> Unit,
    onUpdateAssistant: (Float, Float, Boolean, String?) -> Unit,
    onUpdateAboutApp: (Float, Float, Boolean, String?) -> Unit,
    onAddCategoryClick: () -> Unit,
    onEditCategoryClick: (Category) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onToggleCategoryPin: (String) -> Unit,
    onToggleProviderPremium: (String) -> Unit,
    onToggleProviderPin: (String) -> Unit,
    onUpdateProviderSubscription: (String, String) -> Unit,
    onDeleteProvider: (String) -> Unit,
    onApproveJoinRequest: (String) -> Unit,
    onRejectJoinRequest: (String, String) -> Unit,
    onAddSupervisorClick: () -> Unit,
    onDeleteSupervisor: (String) -> Unit,
    onUpdateSupervisorPass: (String, String, String, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var adminSectionTab by remember { mutableStateOf("subscriptions") } // subscriptions, categories, backup, widgets, supervisors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // Horizontal mini header menu for supervisors options
        Text(
            text = "لوحة تحكم المشرفين ⚙️ (صلاحية: ${activeSup.name})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Administrative Category tabs selectors
        LazyRow(modifier = Modifier.padding(bottom = 12.dp)) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (adminSectionTab == "subscriptions") YemenRedAccent else DeepIndigo
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { adminSectionTab = "subscriptions" }
                ) {
                    Text("الاشتراكات والفنيين 📋", modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (adminSectionTab == "categories") YemenRedAccent else DeepIndigo
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { adminSectionTab = "categories" }
                ) {
                    Text("إدارة الأقسام والتصنيف 🗂️", modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (adminSectionTab == "backup") YemenRedAccent else DeepIndigo
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { adminSectionTab = "backup" }
                ) {
                    Text("إدارة النسخ الاحتياطي 💾", modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (adminSectionTab == "widgets") YemenRedAccent else DeepIndigo
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { adminSectionTab = "widgets" }
                ) {
                    Text("المساعد والمظاهر 🎨", modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (adminSectionTab == "supervisors") YemenRedAccent else DeepIndigo
                    ),
                    modifier = Modifier
                        .padding(4.dp)
                        .clickable { adminSectionTab = "supervisors" }
                ) {
                    Text("المشرفين والصلاحيات 🔑", modifier = Modifier.padding(8.dp), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Selected Sub dashboard render
        when (adminSectionTab) {
            "subscriptions" -> SubscriptionsManagerSection(
                providers = providers,
                pendingRequests = pendingRequests,
                onTogglePremium = { if (activeSup.canDeleteProviders) onToggleProviderPremium(it) },
                onTogglePin = { if (activeSup.canDeleteProviders) onToggleProviderPin(it) },
                onUpdateStatus = { id, stat -> if (activeSup.canDeleteProviders) onUpdateProviderSubscription(id, stat) },
                onDelete = { if (activeSup.canDeleteProviders) onDeleteProvider(it) },
                onApprove = { onApproveJoinRequest(it) },
                onReject = { onRejectJoinRequest(it, "مرفوض لتجاوز الحدود المعينة") }
            )

            "categories" -> CategoriesManagerSection(
                categories = categories,
                canEdit = activeSup.canEditCategories,
                onAddClick = onAddCategoryClick,
                onEditClick = onEditCategoryClick,
                onTogglePin = onToggleCategoryPin,
                onDelete = onDeleteCategory
            )

            "backup" -> BackupManagerSection(
                activeSup = activeSup,
                history = backupHistory,
                alert = schedulerAlert,
                chosenPath = chosenBackupPath,
                onPathChange = onBackupPathChange,
                statusMsg = backupStatus,
                onTrigger = onTriggerBackup,
                onClearAlert = onClearSchedulerAlert,
                onRestore = onRestoreClick
            )

            "widgets" -> DynamicWidgetsAndStyleSection(
                configs = configs,
                onUpdateAssistant = onUpdateAssistant,
                onUpdateAboutApp = onUpdateAboutApp,
                onToggleMaps = onToggleMaps
            )

            "supervisors" -> SupervisorsManagerSection(
                activeSup = activeSup,
                supervisors = supervisors,
                onAddClick = onAddSupervisorClick,
                onDeleteClick = onDeleteSupervisor,
                onUpdatePin = onUpdateSupervisorPass
            )
        }
    }
}

// -------------------------------------------------------------
// SEC 1: Subscription Manager Dashboard Screen
// -------------------------------------------------------------
@Composable
fun SubscriptionsManagerSection(
    providers: List<ServiceProvider>,
    pendingRequests: List<PendingProvider>,
    onTogglePremium: (String) -> Unit,
    onTogglePin: (String) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDelete: (String) -> Unit,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Pending Join Requests
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = YemenGoldAccent.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .border(1.dp, YemenGoldAccent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PendingActions, contentDescription = "Pending", tint = YemenGoldAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "طلبات الانضمام المعلقة لتسجيل الفنيين (${pendingRequests.size})",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    if (pendingRequests.isEmpty()) {
                        Text(
                            text = "لا توجد طلبات معلقة حالياً.",
                            color = SoftGrayText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        pendingRequests.forEach { req ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Display Applicant Personal Photo (Selfie / File)
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SpaceSlate),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (req.profilePhoto.startsWith("selfie")) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.Face, contentDescription = null, tint = YemenGoldAccent, modifier = Modifier.size(20.dp))
                                                Text("سيلفي 🤳", color = SoftGrayText, fontSize = 7.sp)
                                            }
                                        } else if (req.profilePhoto.isBlank()) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = SoftGrayText)
                                        } else {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MutedGreen, modifier = Modifier.size(20.dp))
                                                Text("معرض 📱", color = SoftGrayText, fontSize = 7.sp)
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(req.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("الهاتف: ${req.phone} | الحي: ${req.neighborhood}", color = SoftGrayText, fontSize = 11.sp)
                                        Text("العنوان: ${req.address}", color = SoftGrayText, fontSize = 11.sp)
                                        if (req.identityPhoto != null) {
                                            Text("بطاقة الهوية: [${req.identityPhoto}] متاح للتدقيق 🪪", color = YemenGoldAccent, fontSize = 10.sp)
                                        }
                                        if (req.hasLocation) {
                                            Text("الموقع الجغرافي: (15.3694, 44.1910) 📍", color = MutedGreen, fontSize = 10.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Button(
                                            onClick = { onApprove(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = MutedGreen),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("تفعيل وتضمين", fontSize = 10.sp, color = Color.White)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = { onReject(req.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("رفض", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Subscription controller of existing providers
        item {
            Text(
                text = "إدارة اشتراكات مقدمي الخدمات الحاليين:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        items(providers) { prov ->
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = prov.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "حالة الاشتراك: ", color = SoftGrayText, fontSize = 11.sp)
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (prov.subscriptionStatus) {
                                            "active" -> MutedGreen.copy(alpha = 0.2f)
                                            "suspended" -> YemenGoldAccent.copy(alpha = 0.2f)
                                            else -> Color.Gray.copy(alpha = 0.2f)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = when (prov.subscriptionStatus) {
                                            "active" -> "نشط ومفعّل"
                                            "suspended" -> "معلّق مؤقتاً"
                                            else -> "ملغي"
                                        },
                                        color = when (prov.subscriptionStatus) {
                                            "active" -> MutedGreen
                                            "suspended" -> YemenGoldAccent
                                            else -> Color.Gray
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Premium State "مميز" Button
                        Button(
                            onClick = { onTogglePremium(prov.id) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (prov.isPremium) YemenRedAccent else Color.DarkGray
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (prov.isPremium) "تثبيت كـ مميز (نشط) 🌟" else "جعله مميز 🌟",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("التحكم السريع في ظهور الفني:", color = SoftGrayText, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row {
                            Button(
                                onClick = {
                                    val nextStatus = if (prov.subscriptionStatus == "active") "suspended" else "active"
                                    onUpdateStatus(prov.id, nextStatus)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (prov.subscriptionStatus == "active") YemenGoldAccent else MutedGreen
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (prov.subscriptionStatus == "active") "تجميد الاشتراك ⏸️" else "تنشيط الخدمة ▶️",
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Button(
                                onClick = { onTogglePin(prov.id) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (prov.isPinned) YemenGoldAccent else Color.Gray
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (prov.isPinned) "إلغاء التثبيت 📌" else "تثبيت في المقدمة 📌",
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = { onDelete(prov.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "حذف مقدم الخدمة", tint = YemenRedAccent)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SEC 2: Category Hierarchical Manager Section
// -------------------------------------------------------------
@Composable
fun CategoriesManagerSection(
    categories: List<Category>,
    canEdit: Boolean,
    onAddClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onTogglePin: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    val mainCategories = categories.filter { it.parentId == null }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("هيكلة الأقسام والتصنيفات المهنية:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        if (canEdit) {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.AddCircle, contentDescription = "إضافة قسم", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة قسم جديدة", fontSize = 11.sp)
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(mainCategories) { mainCat ->
            val subCats = categories.filter { it.parentId == mainCat.id }
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(mainCat.icon, fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                            Text(
                                text = mainCat.name,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(colors = CardDefaults.cardColors(containerColor = SpaceSlate)) {
                                Text(
                                    text = "رئيسي",
                                    color = SoftGrayText,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (mainCat.isPinned) {
                                Icon(Icons.Default.PushPin, contentDescription = "Pinned", tint = YemenGoldAccent, modifier = Modifier.size(14.dp))
                            }
                        }

                        Row {
                            IconButton(onClick = { onTogglePin(mainCat.id) }) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "PinCategory",
                                    tint = if (mainCat.isPinned) YemenGoldAccent else Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (canEdit) {
                                IconButton(onClick = { onEditClick(mainCat) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDelete(mainCat.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = YemenRedAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    // Render Sub-categories
                    if (subCats.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("الأقسام الفرعية التابعة له:", color = SoftGrayText, fontSize = 11.sp)
                        subCats.forEach { subCat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 2.dp, bottom = 2.dp)
                                    .background(SpaceSlate, RoundedCornerShape(4.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(subCat.icon, modifier = Modifier.padding(end = 4.dp))
                                    Text(subCat.name, color = Color.White, fontSize = 12.sp)
                                }
                                if (canEdit) {
                                    Row {
                                        IconButton(
                                            onClick = { onEditClick(subCat) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Sub", tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        IconButton(
                                            onClick = { onDelete(subCat.id) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Sub", tint = YemenRedAccent, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "لا توجد أقسام فرعية مسجلة حالياً.",
                            color = SoftGrayText.copy(alpha = 0.6f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SEC 3: Backup & Restore Management Section with Paths Select
// -------------------------------------------------------------
@Composable
fun BackupManagerSection(
    activeSup: Supervisor,
    history: List<BackupHistory>,
    alert: String?,
    chosenPath: String,
    onPathChange: (String) -> Unit,
    statusMsg: String,
    onTrigger: () -> Unit,
    onClearAlert: () -> Unit,
    onRestore: () -> Unit
) {
    var backupString by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Scheduler Automatic Warning alerts notifications
        if (alert != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = YemenRedAccent.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.2.dp, YemenRedAccent, RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = "Warning", tint = YemenRedAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "تنبيه هام حول فشل الجدولة التلقائية:",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = alert,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = onClearAlert) {
                                Text("تجاهل وإغلاق التنبيه", color = YemenRedAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خيارات وإنشاء النسخة الاحتياطية الفورية:",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("حدد وجهة مسار حفظ ملف النسخة التخزينية:", color = SoftGrayText, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Path Selector Radio Groups
                    val paths = listOf("Phone Memory", "SD Card", "Google Drive")
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        paths.forEach { pathName ->
                            val isSelected = chosenPath == pathName
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) YemenGoldAccent else SpaceSlate
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clickable { onPathChange(pathName) }
                            ) {
                                Text(
                                    text = pathName,
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    textAlign = TextAlign.Center,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                if (activeSup.canViewBackup) {
                                    onTrigger()
                                    backupString = FirestoreSim.generateBackupBase64String()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.weight(1f),
                            enabled = activeSup.canViewBackup
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = "Backup", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("أخذ نسخة احتياطية فورية 💾", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = onRestore,
                            colors = ButtonDefaults.buttonColors(containerColor = SpaceSlate),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(6.dp)),
                            enabled = activeSup.canViewBackup
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد كود الاسترجاع 🔄", fontSize = 11.sp)
                        }
                    }

                    // Status update message
                    if (statusMsg.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = statusMsg,
                            color = MutedGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Display the dynamic backup key if success
                    if (backupString.isNotBlank() && activeSup.canViewBackup) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("مفتاح الاسترجاع المشفر النهائي للحفـظ (انقر لتنسخ):", color = SoftGrayText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SpaceSlate),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp)
                        ) {
                            Text(
                                text = backupString,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable { /* Copy to clip simulated */ },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = YemenGoldAccent,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Historial Backups Records Logs Log Book list
        item {
            Text(
                text = "السجل التاريخي لعمليات النسخ الناجحة / الفاشلة:",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        if (history.isEmpty()) {
            item {
                Text(
                    text = "لا توجد سجلات سابقة للنسخ بعد.",
                    color = SoftGrayText,
                    fontSize = 12.sp
                )
            }
        } else {
            items(history) { log ->
                val isSuccess = log.status == "success"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDate = sdf.format(Date(log.timestamp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSuccess) MutedGreen.copy(alpha = 0.2f) else YemenRedAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = log.status,
                                tint = if (isSuccess) MutedGreen else YemenRedAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = log.description, color = Color.White, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = "التوقيت: $formattedDate | مسار الحفظ: ${log.chosenPath}", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                            if (log.errorMessage != null) {
                                Text(
                                    text = "الخطأ: ${log.errorMessage}",
                                    color = YemenRedAccent,
                                    fontSize = 9.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SEC 4: Dynamic Custom Widgets Configuration Section
// -------------------------------------------------------------
@Composable
fun DynamicWidgetsAndStyleSection(
    configs: AppConfigs,
    onUpdateAssistant: (Float, Float, Boolean, String?) -> Unit,
    onUpdateAboutApp: (Float, Float, Boolean, String?) -> Unit,
    onToggleMaps: (Boolean) -> Unit
) {
    var assistScale by remember { mutableFloatStateOf(configs.assistantScale) }
    var assistAlpha by remember { mutableFloatStateOf(configs.assistantTransparency) }
    var assistHidden by remember { mutableStateOf(configs.assistantHidden) }
    var assistEmojiIcon by remember { mutableStateOf(configs.customAssistantIconBase64 ?: "🤖") }

    var abAppScale by remember { mutableFloatStateOf(configs.aboutAppScale) }
    var abAppAlpha by remember { mutableFloatStateOf(configs.aboutAppTransparency) }
    var abAppHidden by remember { mutableStateOf(configs.aboutAppHidden) }
    var abAppEmojiIcon by remember { mutableStateOf(configs.aboutAppIconBase64 ?: "ℹ️") }

    var localAppName by remember { mutableStateOf(configs.appName) }
    var localPhone by remember { mutableStateOf(configs.supportPhone) }
    var localWhatsapp by remember { mutableStateOf(configs.supportWhatsapp) }
    var localEmail by remember { mutableStateOf(configs.supportEmail) }
    var localFooter by remember { mutableStateOf(configs.footerText) }
    var localBgColor by remember { mutableStateOf(configs.inputBackgroundColor) }
    var localTextColor by remember { mutableStateOf(configs.inputTextColor) }
    var configSaveMessage by remember { mutableStateOf("") }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "خيارات خرائط جوجل (Google Maps Configuration)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يسمح للمسؤول بتعطيل أو تشغيل الخريطة التفاعلية الفورية في واجهة المستخدم.",
                        color = SoftGrayText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (configs.googleMapsEnabled) "الخرائط مفعلة حالياً ومتاحة للجمهور ✅" else "الخرائط مغلقة حالياً ❌",
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Switch(
                            checked = configs.googleMapsEnabled,
                            onCheckedChange = { onToggleMaps(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MutedGreen,
                                checkedTrackColor = MutedGreen.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }

        // BRANDING CONFIGURATION & TEXT BACKGROUND COLOR THEMES
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "هوية الدليل وألوان مِلء خانات الكتابة 🎨",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "تعديل ألوان خلفيات الكتابة للفنيين لضمان ظهور النصوص، مع تعديل الاسم وبيانات الاتصال والفوتر الملازم العام:",
                        color = SoftGrayText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    AppTextField(
                        value = localAppName,
                        onValueChange = { localAppName = it },
                        label = "اسم التطبيق المعروض"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = localPhone,
                        onValueChange = { localPhone = it },
                        label = "رقم هاتف الاتصال المباشر"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = localWhatsapp,
                        onValueChange = { localWhatsapp = it },
                        label = "رقم الواتساب اليمني للدعم الفني"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = localEmail,
                        onValueChange = { localEmail = it },
                        label = "بريد الدعم والمراسلة الإلكتروني"
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = localFooter,
                        onValueChange = { localFooter = it },
                        label = "النص الإشهاري الافتراضي للتذييل (الفوتر)"
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("اختر ثيم مظهر خفيات الكتابة (لضمان وضوح الخانات):", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    // Button Row selector for background styles
                    val themes = listOf(
                        Triple("داكن فضائي 🌌", "#1E293B", "#FFFFFF"),
                        Triple("كحلي عميق 🔮", "#0F172A", "#FFFFFF"),
                        Triple("أبيض ناصع ❄️", "#FFFFFF", "#1E293B"),
                        Triple("ذهبي رملي 🏜️", "#F5F5F4", "#0C0A09"),
                        Triple("أخضر مهدئ 🌲", "#064E3B", "#FFFFFF")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        themes.forEach { (title, bg, text) ->
                            val isSelected = localBgColor.uppercase() == bg.uppercase()
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) YemenGoldAccent else SpaceSlate
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        localBgColor = bg
                                        localTextColor = text
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(modifier = Modifier.size(16.dp).background(Color(android.graphics.Color.parseColor(bg))).border(1.dp, Color.White))
                                        Text("عينة النص", color = Color(android.graphics.Color.parseColor(text)), fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val updated = configs.copy(
                                appName = localAppName,
                                supportPhone = localPhone,
                                supportWhatsapp = localWhatsapp,
                                supportEmail = localEmail,
                                footerText = localFooter,
                                inputBackgroundColor = localBgColor,
                                inputTextColor = localTextColor
                            )
                            FirestoreSim.updateAppConfigs(updated)
                            configSaveMessage = "تم تحديث هوية الدليل وتغيير ثيم خانات الكتابة بنجاح ومزامنته بجميع الأجهزة فورا! ✅"
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ ومزامنة الإعدادات العامة 💾", fontWeight = FontWeight.Bold)
                    }

                    if (configSaveMessage.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(configSaveMessage, color = MutedGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // CUSTOM SMART ASSISTANT ICON STYLE MODIFIERS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "المساعد الذكي العائم (Smart Assistant Widget Style)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إخفاء المساعد بالكامل", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = assistHidden,
                            onCheckedChange = {
                                assistHidden = it
                                onUpdateAssistant(assistScale, assistAlpha, it, assistEmojiIcon)
                            }
                        )
                    }

                    if (!assistHidden) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("اختر رمز/إيموجي مظهر المساعد:", color = SoftGrayText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        val assistantIcons = listOf("🤖", "👨‍💻", "💡", "🇾🇪", "📞")
                        Row {
                            assistantIcons.forEach { icon ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (assistEmojiIcon == icon) YemenGoldAccent else SpaceSlate
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            assistEmojiIcon = icon
                                            onUpdateAssistant(assistScale, assistAlpha, assistHidden, icon)
                                        }
                                ) {
                                    Text(icon, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("تعديل حجم المساعد: (الحجم الحالي: ${(assistScale * 100).toInt()}% )", color = SoftGrayText, fontSize = 11.sp)
                        Slider(
                            value = assistScale,
                            onValueChange = {
                                assistScale = it
                                onUpdateAssistant(it, assistAlpha, assistHidden, assistEmojiIcon)
                            },
                            valueRange = 0.4f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = YemenGoldAccent, activeTrackColor = YemenGoldAccent)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("درجة الشفافية: (الوضوح: ${(assistAlpha * 100).toInt()}% )", color = SoftGrayText, fontSize = 11.sp)
                        Slider(
                            value = assistAlpha,
                            onValueChange = {
                                assistAlpha = it
                                onUpdateAssistant(assistScale, it, assistHidden, assistEmojiIcon)
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = YemenGoldAccent, activeTrackColor = YemenGoldAccent)
                        )
                    }
                }
            }
        }

        // ABOUT APP ICON CONFIGURATIONS
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "أيقونة [عن التطبيق] العائمة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("إخفاء الأيقونة بالكامل", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = abAppHidden,
                            onCheckedChange = {
                                abAppHidden = it
                                onUpdateAboutApp(abAppScale, abAppAlpha, it, abAppEmojiIcon)
                            }
                        )
                    }

                    if (!abAppHidden) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("اختر رمز مظهر شارة عن التطبيق:", color = SoftGrayText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        val aboutIcons = listOf("ℹ️", "🏢", "🛠️", "🎯", "📲")
                        Row {
                            aboutIcons.forEach { icon ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (abAppEmojiIcon == icon) YemenGoldAccent else SpaceSlate
                                    ),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            abAppEmojiIcon = icon
                                            onUpdateAboutApp(abAppScale, abAppAlpha, abAppHidden, icon)
                                        }
                                ) {
                                    Text(icon, modifier = Modifier.padding(8.dp), fontSize = 16.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("مقياس الحجم: ${(abAppScale * 100).toInt()}%", color = SoftGrayText, fontSize = 11.sp)
                        Slider(
                            value = abAppScale,
                            onValueChange = {
                                abAppScale = it
                                onUpdateAboutApp(it, abAppAlpha, abAppHidden, abAppEmojiIcon)
                            },
                            valueRange = 0.4f..1.5f,
                            colors = SliderDefaults.colors(thumbColor = YemenGoldAccent, activeTrackColor = YemenGoldAccent)
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Text("مستوى الشفافية والظهور: ${(abAppAlpha * 100).toInt()}%", color = SoftGrayText, fontSize = 11.sp)
                        Slider(
                            value = abAppAlpha,
                            onValueChange = {
                                abAppAlpha = it
                                onUpdateAboutApp(abAppScale, it, abAppHidden, abAppEmojiIcon)
                            },
                            valueRange = 0.1f..1.0f,
                            colors = SliderDefaults.colors(thumbColor = YemenGoldAccent, activeTrackColor = YemenGoldAccent)
                        )
                    }
                }
            }
        }

        // --- SPECIAL CUSTOM WELCOME BANNER CONFIGURATIONS ---
        item {
            var welcomeText by remember { mutableStateOf(configs.welcomeText) }
            var welcomeFontSize by remember { mutableFloatStateOf(configs.welcomeTextSize) }
            var welcomePos by remember { mutableStateOf(configs.welcomePosition) }
            var showBanner by remember { mutableStateOf(configs.showWelcomeBanner) }
            var selectedBase64 by remember { mutableStateOf(configs.welcomeImageBase64) }

            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التحكم في رسالة الترحيب / لافتة الإعلان المميزة",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "يمكنك تعديل نص الرسالة وحجم الخط ومكان ظهورها، أو تفعيل صورة ترحيبية بدلاً من النص يتم اختيارها من ذاكرة تخزين الهاتف.",
                        color = SoftGrayText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("عرض اللافتة الترحيبية على الرئيسية", color = Color.White, fontSize = 12.sp)
                        Switch(
                            checked = showBanner,
                            onCheckedChange = {
                                showBanner = it
                                FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, welcomePos, selectedBase64, it)
                            }
                        )
                    }

                    if (showBanner) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = welcomeText,
                            onValueChange = {
                                welcomeText = it
                                FirestoreSim.updateWelcomeBanner(it, welcomeFontSize, welcomePos, selectedBase64, showBanner)
                            },
                            label = { Text("نص رسالة الترحيب (عند عدم تفعيل صورة)", color = SoftGrayText) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("التحكم بحجم الخط للرسالة: ${welcomeFontSize.toInt()} sp", color = SoftGrayText, fontSize = 11.sp)
                        Slider(
                            value = welcomeFontSize,
                            onValueChange = {
                                welcomeFontSize = it
                                FirestoreSim.updateWelcomeBanner(welcomeText, it, welcomePos, selectedBase64, showBanner)
                            },
                            valueRange = 10f..30f,
                            colors = SliderDefaults.colors(thumbColor = YemenGoldAccent, activeTrackColor = YemenGoldAccent)
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Text("مكان ظهور لافتة الترحيب (تحديد الموضع):", color = SoftGrayText, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))

                        val positions = listOf("top" to "أعلى الشاشة", "below_search" to "تحت البحث", "below_map" to "تحت الخريطة")
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            positions.forEach { (posKey, posLabel) ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (welcomePos == posKey) YemenGoldAccent else SpaceSlate
                                    ),
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clickable {
                                            welcomePos = posKey
                                            FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, posKey, selectedBase64, showBanner)
                                        }
                                ) {
                                    Text(
                                        text = posLabel,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("اختيار صورة من ذاكرة الهاتف بدلاً من النص الترحيبي:", color = YemenGoldAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        Text("اختر صورة محملة مسبقاً من ذاكرة الهاتف (محاكاة الاستوديو):", color = SoftGrayText, fontSize = 10.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val banner1Base64 = "iVBORw0KGgoAAAANSUhEUgAAAKAAAABQAQMAAAD/u68HAAAAA1BMVEX/mZn///+nLy64AAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUHBAUSDgEFAQGbygYAAAALSURBVCgTYxgFAwUAAAcAAf62qGgAAAAASUVORK5CYII="
                            val banner2Base64 = "iVBORw0KGgoAAAANSUhEUgAAAKAAAABQAQMAAAD/u68HAAAAA1BMVEX/2Zf///+C5GzJAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUHBAUSDgEFAQGbygYAAAALSURBVCgTYxgFAwUAAAcAAf62qGgAAAAASUVORK5CYII="

                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (selectedBase64 == banner1Base64) MutedGreen else SpaceSlate),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBase64 = banner1Base64
                                        FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, welcomePos, banner1Base64, showBanner)
                                    }
                            ) {
                                Text("لافتة ألوان ربيع خضراء 🟩", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = if (selectedBase64 == banner2Base64) YemenGoldAccent else SpaceSlate),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        selectedBase64 = banner2Base64
                                        FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, welcomePos, banner2Base64, showBanner)
                                    }
                            ) {
                                Text("لافتة زخرفة برونزية ذهبية 🟨", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(8.dp), textAlign = TextAlign.Center)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Button(
                                onClick = {
                                    val scenicYemenBase64 = "iVBORw0KGgoAAAANSUhEUgAAAKAAAABQAQMAAAD/u68HAAAAA1BMVEWAgICff59PAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAB3RJTUUHBAUSDgEFAQGbygYAAAALSURBVCgTYxgFAwUAAAcAAf62qGgAAAAASUVORK5CYII="
                                    selectedBase64 = scenicYemenBase64
                                    FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, welcomePos, scenicYemenBase64, showBanner)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.3f)
                            ) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = "معرض", modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اختر من ذاكرة الهاتف 📱", fontSize = 10.sp)
                            }

                            if (selectedBase64 != null) {
                                Spacer(modifier = Modifier.width(6.dp))
                                TextButton(
                                    onClick = {
                                        selectedBase64 = null
                                        FirestoreSim.updateWelcomeBanner(welcomeText, welcomeFontSize, welcomePos, null, showBanner)
                                    },
                                    modifier = Modifier.weight(0.7f)
                                ) {
                                    Text("حذف الصورة والرجوع للنص", color = YemenRedAccent, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// SEC 5: Supervisors Credentials Management Screen
// -------------------------------------------------------------
@Composable
fun SupervisorsManagerSection(
    activeSup: Supervisor,
    supervisors: List<Supervisor>,
    onAddClick: () -> Unit,
    onDeleteClick: (String) -> Unit,
    onUpdatePin: (String, String, String, Boolean, Boolean, Boolean, Boolean) -> Unit
) {
    var editingSupId by remember { mutableStateOf<String?>(null) }
    var editName by remember { mutableStateOf("") }
    var editPinCode by remember { mutableStateOf("") }
    var editCanEditCats by remember { mutableStateOf(false) }
    var editCanDelProvs by remember { mutableStateOf(false) }
    var editCanBackup by remember { mutableStateOf(false) }
    var editCanModifyConfs by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("قائمة المشرفين المسجلين بالدليل:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = YemenRedAccent),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "مشرف", modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("إضافة مشرف جديد", fontSize = 11.sp)
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(supervisors) { sup ->
            val isEditing = editingSupId == sup.id

            Card(
                colors = CardDefaults.cardColors(containerColor = DeepIndigo),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { editName = it },
                                    label = { Text("الاسم") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = editPinCode,
                                    onValueChange = { editPinCode = it },
                                    label = { Text("الرمز رقمي PIN") },
                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                )
                            } else {
                                Text(text = sup.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "رمز المرور للتحقق الصوتي/الرقمي: [ **** ]", color = SoftGrayText, fontSize = 11.sp)
                            }
                        }

                        Row {
                            if (isEditing) {
                                IconButton(onClick = {
                                    if (editName.isNotBlank() && editPinCode.isNotBlank()) {
                                        onUpdatePin(
                                            sup.id,
                                            editName,
                                            editPinCode,
                                            editCanEditCats,
                                            editCanDelProvs,
                                            editCanBackup,
                                            editCanModifyConfs
                                        )
                                        editingSupId = null
                                    }
                                }) {
                                    Icon(Icons.Default.Save, contentDescription = "حفظ", tint = MutedGreen)
                                }
                                IconButton(onClick = { editingSupId = null }) {
                                    Icon(Icons.Default.Cancel, contentDescription = "إلغاء", tint = YemenRedAccent)
                                }
                            } else {
                                IconButton(onClick = {
                                    editName = sup.name
                                    editPinCode = sup.pinCode
                                    editCanEditCats = sup.canEditCategories
                                    editCanDelProvs = sup.canDeleteProviders
                                    editCanBackup = sup.canViewBackup
                                    editCanModifyConfs = sup.canModifyConfigs
                                    editingSupId = sup.id
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = Color.White)
                                }
                                if (sup.id != "sup1") { // Preserve master general admin
                                    IconButton(onClick = { onDeleteClick(sup.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = YemenRedAccent)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isEditing) "تحديد وتعديل الصلاحيات الممنوحة:" else "الصلاحيات النشطة الممنوحة للحساب:",
                        color = SoftGrayText,
                        fontSize = 11.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    Checkbox(
                                        checked = editCanEditCats,
                                        onCheckedChange = { editCanEditCats = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MutedGreen)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (sup.canEditCategories) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "status",
                                        tint = if (sup.canEditCategories) MutedGreen else YemenRedAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("تعديل الأقسام وتصنيفاتها", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    Checkbox(
                                        checked = editCanDelProvs,
                                        onCheckedChange = { editCanDelProvs = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MutedGreen)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (sup.canDeleteProviders) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "status",
                                        tint = if (sup.canDeleteProviders) MutedGreen else YemenRedAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("الاشتراكات وحذف مقدمي الخدمات", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    Checkbox(
                                        checked = editCanBackup,
                                        onCheckedChange = { editCanBackup = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MutedGreen)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (sup.canViewBackup) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "status",
                                        tint = if (sup.canViewBackup) MutedGreen else YemenRedAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("قراءة واسترجاع النسخ التخزينية", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isEditing) {
                                    Checkbox(
                                        checked = editCanModifyConfs,
                                        onCheckedChange = { editCanModifyConfs = it },
                                        colors = CheckboxDefaults.colors(checkedColor = MutedGreen)
                                    )
                                } else {
                                    Icon(
                                        imageVector = if (sup.canModifyConfigs) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = "status",
                                        tint = if (sup.canModifyConfigs) MutedGreen else YemenRedAccent,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text("التحكم في المساعد والمظاهر", color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple color function mapping Yemen gold theme
fun YemenGold() = Color(0xFFD97706)
fun اليمنGold() = Color(0xFFD97706)

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    singleLine: Boolean = true
) {
    val configState by FirestoreSim.configs.collectAsState()
    
    val containerBgColor = remember(configState.inputBackgroundColor) {
        try {
            Color(android.graphics.Color.parseColor(configState.inputBackgroundColor))
        } catch (e: Exception) {
            Color(0xFF1E293B) // Dark Slate fallback
        }
    }
    val textColor = remember(configState.inputTextColor) {
        try {
            Color(android.graphics.Color.parseColor(configState.inputTextColor))
        } catch (e: Exception) {
            Color.White
        }
    }
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = textColor.copy(alpha = 0.8f)) },
        modifier = modifier.fillMaxWidth(),
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        singleLine = singleLine,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerBgColor,
            unfocusedContainerColor = containerBgColor,
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = YemenGoldAccent,
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            focusedLabelColor = YemenGoldAccent,
            unfocusedLabelColor = textColor.copy(alpha = 0.7f)
        )
    )
}
