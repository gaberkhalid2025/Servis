package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.FirestoreSim

@Composable
fun JoinScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("صنعاء - شارع حدة") }

    // Uploaded paths simulator
    var selfieUri by remember { mutableStateOf("") }
    var idCardUri by remember { mutableStateOf("") }

    var isSubmitting by remember { mutableStateOf(false) }

    // Camera hardware logic simulators
    var showCameraSim by remember { mutableStateOf(false) }
    var cameraMode by remember { mutableStateOf("selfie") } // or 'id'
    
    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraSim = true
        } else {
            Toast.makeText(context, "يرجى منح صلاحية الكاميرا لالتقاط الصورة مباشرة", Toast.LENGTH_LONG).show()
        }
    }

    val regionOptions = listOf(
        "صنعاء - شارع حدة",
        "صنعاء - باب اليمن",
        "عدن - كريتر",
        "عدن - المنصورة",
        "تعز - شارع جمال",
        "الحديدة - شارع صنعاء",
        "حضرموت - المكلا"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    text = "طلب انضمام مقدم خدمة 🛠️+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            Text(
                text = "انضم إلى نخبة الفنيين والحرفيين في اليمن. بعد ملء البيانات، سيقوم المشرف العام بمراجعة طلبك وتوثيق حسابك بالشارة الزرقاء في غضون ٢٤ ساعة.",
                fontSize = 12.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Form Cards
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("البيانات الشخصية والمهنية", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم الكامل بالخط العربي الثلاثي") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("المسمى المهني (مثال: سباك فني تركيب فلاتر)") },
                        placeholder = { Text("مثال: مهندس شبكات وتمديدات طاقة") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        leadingIcon = { Icon(Icons.Default.Build, null) }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف اليمني النشط (مكالمات + واتساب)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        leadingIcon = { Icon(Icons.Default.Phone, null) }
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("نبذة مختصرة عن الخبرات والمعدات المتاحة") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Region Selector Dropdown
                    Text("منطقة التواجد والخدمات الميدانية:", fontSize = 12.sp, color = Color.LightGray, modifier = Modifier.padding(bottom = 6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        regionOptions.forEach { r ->
                            val isSelected = region == r
                            val bg = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF374151)
                            Text(
                                text = r,
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(bg)
                                    .clickable { region = r }
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Identification documents and selfie picker card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("المستندات الأمنية والتوثيق (ذاتي + هوية)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))

                    // Selfie block
                    DocumentPickerItem(
                        title = "صورة سيلفي واضحة (Selfie)",
                        description = "صورة ملتقطة لوجهك بوضوح بمكان مضاء لتوثيق الهوية لمكافحة الاحتيال.",
                        uri = selfieUri,
                        onPickLocal = { selfieUri = "selfie_mock_upload.png" },
                        onCaptureLive = {
                            cameraMode = "selfie"
                            // Check Camera Permissions
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                showCameraSim = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // ID Card block
                    DocumentPickerItem(
                        title = "البطاقة الشخصية أو جواز السفر",
                        description = "صورة ضوئية واضحة للوجه الأمامي للبطاقة الشخصية اليمنية الموثقة.",
                        uri = idCardUri,
                        onPickLocal = { idCardUri = "yemeny_id_card_mock.png" },
                        onCaptureLive = {
                            cameraMode = "id"
                            val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                showCameraSim = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    )
                }
            }

            // Error notice if documents are missing
            if (selfieUri.isEmpty() || idCardUri.isEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF3C7), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ملاحظة: يتطلب رفع ملف السيلفي وبطاقة الهوية الوطنية لتفعيل الحساب بنجاح ووضعه في الصدارة للزوار.",
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Action submit button
            Button(
                onClick = {
                    if (name.isEmpty() || title.isEmpty() || phone.isEmpty()) {
                        Toast.makeText(context, "الرجاء تعبئة البيانات الأساسية (الاسم والمسمى ورقم الهاتف)", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    isSubmitting = true
                    // Simulate uploading files delay
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        FirestoreSim.submitJoinRequest(
                            name = name,
                            title = title,
                            bio = bio,
                            phone = phone,
                            region = region,
                            selfie = if (selfieUri.isEmpty()) "selfie_default_sim.png" else selfieUri,
                            idCard = if (idCardUri.isEmpty()) "id_default_sim.png" else idCardUri
                        )
                        isSubmitting = false
                        Toast.makeText(context, "تم إرسال طلبك بنجاح! شكراً لانضمامك وسيتم مراجعته من الإدارة.", Toast.LENGTH_LONG).show()
                        onNavigateBack()
                    }, 2000)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("إرسال طلب الانضمام والتوثيق المعتمد 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }

        // Expanded Live Camera simulator screen
        if (showCameraSim) {
            CameraSimulatorView(
                mode = cameraMode,
                onClose = { showCameraSim = false },
                onCaptured = { capturedPath ->
                    if (cameraMode == "selfie") {
                        selfieUri = capturedPath
                    } else {
                        idCardUri = capturedPath
                    }
                    showCameraSim = false
                    Toast.makeText(context, "تم التقاط الصورة بنجاح وتجهيز المستند!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun DocumentPickerItem(
    title: String,
    description: String,
    uri: String,
    onPickLocal: () -> Unit,
    onCaptureLive: () -> Unit
) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
        Text(description, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Pick from library button
            Button(
                onClick = onPickLocal,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151))
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("من المعرض 🏞️", fontSize = 11.sp)
            }

            // Capture live direct button from phone camera
            Button(
                onClick = onCaptureLive,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("التقاط مباشر 📸", fontSize = 11.sp)
            }
        }

        // Display status feedback
        if (uri.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Green, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "مرفق جاهز: $uri (تم توثيقه بنجاح ✅)",
                    fontSize = 11.sp,
                    color = Color.Green
                )
            }
        }
    }
}

@Composable
fun CameraSimulatorView(
    mode: String,
    onClose: () -> Unit,
    onCaptured: (String) -> Unit
) {
    val context = LocalContext.current
    var isSnapping by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(enabled = false) {} // block click propagation
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (mode == "selfie") "كاميرا السيلفي الأمامية الذكية 🤳" else "مسح وقراءة بطاقة الهوية الوطنية 💳",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, null, tint = Color.White)
                }
            }

            // Simulated live viewport viewfinder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(24.dp)
                    .background(Color(0xFF1E1E24), RoundedCornerShape(24.dp))
                    .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (isSnapping) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    if (mode == "selfie") {
                        // Selfie focus guidelines
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Face,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "ضع وجهك داخل الدائرة المحددة للتثبيت والتصوير الخبير",
                                color = Color.Gray,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    } else {
                        // Passport / ID Scan guidelines
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(160.dp)
                                .border(1.5.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CreditCard,
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp),
                                    tint = Color.LightGray.copy(alpha = 0.6f)
                                )
                                Text(
                                    "قم بمحاذاة البطاقة الشخصية داخل المستطيل",
                                    color = Color.Gray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // Trigger tools
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF111827))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconButton(
                    onClick = {
                        isSnapping = true
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isSnapping = false
                            val randomId = (10000..99999).random()
                            val prefix = if (mode == "selfie") "camera_selfie_" else "camera_scanned_id_"
                            onCaptured("$prefix$randomId.jpg")
                        }, 1200)
                    },
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "انقر لالتقاط وحفظ الصورة مشفوعة لملف التقييم الفوري",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
