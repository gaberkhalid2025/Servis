package com.example.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreSim
import com.example.data.PendingProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(onSuccessDismiss: () -> Unit) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form states
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var mainCategorySelected by remember { mutableStateOf("") }
    var subCategoryText by remember { mutableStateOf("") }
    var workAddress by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var gpsCoords by remember { mutableStateOf("") }
    
    // Photo states
    var personalPhotoUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&q=80&w=250") }
    var personalPhotoSelected by remember { mutableStateOf(false) }
    var idPhotoUrl by remember { mutableStateOf("") }
    var idPhotoSelected by remember { mutableStateOf(false) }

    // Validation trigger states
    var formSubmittedOnce by remember { mutableStateOf(false) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    // Backend configurations
    val categories by FirestoreSim.categories.collectAsState()
    val isAr = FirestoreSim.currentLang.collectAsState().value == "ar"

    val isFormValid = fullName.isNotBlank() && 
                      phone.isNotBlank() && 
                      mainCategorySelected.isNotBlank() && 
                      workAddress.isNotBlank() && 
                      region.isNotBlank() && 
                      personalPhotoSelected

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("join_service_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0x99111827)) // Soft translucent charcoal
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.Badge,
                contentDescription = "Badge icon",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isAr) "استمارة انضمام مقدمي الخدمات الجديد" else "New service provider Registration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isAr) "يرجى تعبئة الحقول كاملة للانضمام إلى دليل الكوادر اليمنية ودعم تواصلك مع الزبائن" else "Fill the credentials to secure verification inside the Yemeni directory.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.3f))

            // Full Name input
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text(if (isAr) "الاسم الثلاثي الكامل (مطلوب)" else "Full Triple Name (Required)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_name_input"),
                isError = formSubmittedOnce && fullName.isBlank(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            if (formSubmittedOnce && fullName.isBlank()) {
                Text(
                    text = if (isAr) "الاسم الثلاثي مطلوب" else "Triple Name is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number Input
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(if (isAr) "رقم الهاتف الفعال / واتساب (مطلوب)" else "Active Phone / WhatsApp (Required)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_phone_input"),
                isError = formSubmittedOnce && phone.isBlank(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            if (formSubmittedOnce && phone.isBlank()) {
                Text(
                    text = if (isAr) "رقم الهاتف مطلوب للتواصل" else "Phone number is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Category selection dropdown (FIXING ELECTRICIAN SELECTION BUG)
            Text(
                text = if (isAr) "القسم والخدمة الرئيسية (مطلوب)" else "Department & Specialty (Required)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 4.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = if (mainCategorySelected.isNotEmpty()) {
                        val matchingCat = categories.find { it.id == mainCategorySelected }
                        (if (isAr) matchingCat?.name else matchingCat?.nameEn) ?: mainCategorySelected
                    } else "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text(if (isAr) "اختر القسم المناسب لمهنتك..." else "Select category...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { categoryDropdownExpanded = true }
                        .testTag("join_category_dropdown"),
                    isError = formSubmittedOnce && mainCategorySelected.isBlank(),
                    trailingIcon = {
                        Icon(
                            imageVector = if (categoryDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { categoryDropdownExpanded = !categoryDropdownExpanded }
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.LightGray
                    )
                )

                DropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color(0xFF1E293B))
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(if (isAr) cat.name else cat.nameEn, color = Color.White) },
                            onClick = {
                                mainCategorySelected = cat.id
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            if (formSubmittedOnce && mainCategorySelected.isBlank()) {
                Text(
                    text = if (isAr) "يرجى اختيار القسم الرئيسي لخدمتك" else "Category selection is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-category customization input (Custom services)
            OutlinedTextField(
                value = subCategoryText,
                onValueChange = { subCategoryText = it },
                label = { Text(if (isAr) "التخصص الفرعي الدقيق (مثال: صيانة غسالات/كاميرات)" else "Sub-specialty / Detailed skills") },
                placeholder = { Text(if (isAr) "حدد الخدمة المحددة التي تسطع بتقديمها" else "e.g. Washing machine engineer") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_subcat_input"),
                leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Workplace Location / Address
            OutlinedTextField(
                value = workAddress,
                onValueChange = { workAddress = it },
                label = { Text(if (isAr) "عنوان مركز أو مكتب العمل الحالي (مطلوب)" else "Workplace Address (Required)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_address_input"),
                isError = formSubmittedOnce && workAddress.isBlank(),
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            if (formSubmittedOnce && workAddress.isBlank()) {
                Text(
                    text = if (isAr) "عنوان العمل مطلوب" else "Work address is required",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Residential Neighborhood / Directorate
            OutlinedTextField(
                value = region,
                onValueChange = { region = it },
                label = { Text(if (isAr) "منطقة السكن الحالية / الدائرة السكنية (مطلوب)" else "Neighborhood / District (Required)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_region_input"),
                isError = formSubmittedOnce && region.isBlank(),
                leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )
            if (formSubmittedOnce && region.isBlank()) {
                Text(
                    text = if (isAr) "المنطقة السكنية مطلوبة لمعرفة الأمان والقرابة" else "Neighborhood is required for proximity routing",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // GPS Coordinates (Optional)
            OutlinedTextField(
                value = gpsCoords,
                onValueChange = { gpsCoords = it },
                label = { Text(if (isAr) "إحداثيات تحديد موقع الـ GPS (اختياري)" else "GPS Coordinates (Optional)") },
                placeholder = { Text("e.g. 15.348,44.204") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_gps_input"),
                trailingIcon = {
                    IconButton(onClick = {
                        val lat = 15.30 + (Math.random() * 0.1)
                        val lng = 44.18 + (Math.random() * 0.1)
                        gpsCoords = String.format("%.4f,%.4f", lat, lng)
                        Toast.makeText(context, "تم تحديد الإحداثيات والالتقاط التلقائي! 📍", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.MyLocation, contentDescription = "Get GPS coordinates", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Personal Photo Picker Section
            Text(
                text = if (isAr) "تحميل الصورة الشخصية للملف (مطلوب)" else "Profile Photo Attachment (Required)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
            )

            Button(
                onClick = {
                    personalPhotoSelected = true
                    Toast.makeText(context, "تم اختيار الصورة الشخصية بنجاح! 📸", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_upload_photo_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (personalPhotoSelected) Icons.Default.CheckCircle else Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = if (personalPhotoSelected) Color.Green else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (personalPhotoSelected) (if (isAr) "تم إرفاق الصورة الشخصية" else "Profile photo attached") 
                               else (if (isAr) "التقاط أو تصفح الصورة الشخصية" else "Capture / Browse personal photo"),
                        color = Color.White
                    )
                }
            }
            if (formSubmittedOnce && !personalPhotoSelected) {
                Text(
                    text = if (isAr) "تحميل صورتك الشخصية أمر إجباري لمصداقية الحساب" else "Profile photo is mandatory",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 12.dp, top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // National Identity File Section
            Text(
                text = if (isAr) "صورة بطاقة الهوية الشخصية (اختياري)" else "National ID Card Photo (Optional)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
            )

            Button(
                onClick = {
                    idPhotoSelected = true
                    idPhotoUrl = "simulated_id_card_url"
                    Toast.makeText(context, "تم تصوير الهوية وإرفاقها بأمان! 💳", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("join_upload_id_button"),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (idPhotoSelected) Icons.Default.CheckCircle else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (idPhotoSelected) Color.Green else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = if (idPhotoSelected) (if (isAr) "تم رفع بطاقة الهوية" else "ID card uploaded") 
                               else (if (isAr) "رفع صورة البطاقة الشخصية" else "Upload ID card"),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Register Submit Button
            Button(
                onClick = {
                    formSubmittedOnce = true
                    if (isFormValid) {
                        val pending = PendingProvider(
                            id = "pend_${System.currentTimeMillis()}",
                            name = fullName,
                            phone = phone,
                            mainCategoryId = mainCategorySelected,
                            subCategoryId = subCategoryText.ifBlank { "كامل المهنة" },
                            city = workAddress,
                            region = region,
                            gpsCoordinates = gpsCoords.ifBlank { "15.348,44.204" },
                            profilePhotoUri = personalPhotoUrl,
                            idCardPhotoUri = idPhotoUrl,
                            submitTime = System.currentTimeMillis(),
                            status = "pending"
                        )
                        FirestoreSim.addPendingRegistration(pending, context)
                        
                        // Show Success Popup state in UI
                        Toast.makeText(context, "تم إرسال طلب انضمامك بنجاح! سيقوم المشرف العام بمراجعته فوراً. 🎉", Toast.LENGTH_LONG).show()
                        
                        // Award User sharing points
                        FirestoreSim.awardLoyaltyPoints(25)
                        
                        onSuccessDismiss()
                    } else {
                        Toast.makeText(context, "الرجاء إدخال كافة البيانات المطلوبة المحددة بالخط الأحمر!", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("join_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isAr) "تقديم طلب الانضمام للمراجعة الفورية 🌟" else "Request Immediate Verification 🌟",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black
                )
            }
        }
    }
}
