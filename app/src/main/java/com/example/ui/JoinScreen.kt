package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreSim
import com.example.data.PendingProvider
import com.example.ui.theme.AlertRed
import com.example.ui.theme.getSelectedTextColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Config textColor from settings
    val appColors = FirestoreSim.appColors.collectAsState()
    val writeTextColor = getSelectedTextColor(appColors.value.textColorName)

    // Data lists from FirestoreSim
    val mainCats = FirestoreSim.mainCategories.collectAsState().value
    val subCats = FirestoreSim.subCategories.collectAsState().value
    val cities = FirestoreSim.cities.collectAsState().value

    // Form inputs state
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedMainCat by remember { mutableStateOf("") }
    var selectedSubCat by remember { mutableStateOf("") }
    var addressDetails by remember { mutableStateOf("") }
    var districtArea by remember { mutableStateOf("") }
    var gpsCoordinates by remember { mutableStateOf("") }
    var avatarImageUri by remember { mutableStateOf("") }
    var idCardImageUri by remember { mutableStateOf("") }

    // Dropdown toggle states
    var mainCatExpanded by remember { mutableStateOf(false) }
    var subCatExpanded by remember { mutableStateOf(false) }
    var districtExpanded by remember { mutableStateOf(false) }

    // Error messages
    var nameError by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf(false) }
    var mainCatError by remember { mutableStateOf(false) }
    var subCatError by remember { mutableStateOf(false) }
    var addressError by remember { mutableStateOf(false) }
    var districtError by remember { mutableStateOf(false) }
    var avatarError by remember { mutableStateOf(false) }

    // Cascaded sub-categories helper list
    val filteredSubCats = remember(selectedMainCat) {
        subCats.filter { it.parentId == selectedMainCat }
    }

    // Load Yemeni districts based on selections
    val defaultDistricts = remember {
        cities.flatMap { it.districts }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App title
        Text(
            text = "استمارة تسجيل الكوادر والمهنيين",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = "انضم إلينا ليرى خدماتك الآلاف من الباحثين يومياً في منطقتك السكنية فوراً وبكل سهولة. يرجى إدخال بيانات صحيحة للمراجعة التلقائية.",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))

        // --- Field 1: الاسم الثلاثي الكامل (إجباري) ---
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                nameError = false
            },
            label = { Text("الاسم الثلاثي الكامل (إجباري)") },
            placeholder = { Text("مثال: ماهر محمد طاهر") },
            isError = nameError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = writeTextColor,
                unfocusedTextColor = writeTextColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
        if (nameError) {
            Text("يرجى إدخال الاسم الثلاثي بالكامل", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 2: رقم الهاتف والواتساب (إجباري) ---
        OutlinedTextField(
            value = phone,
            onValueChange = {
                phone = it
                phoneError = false
            },
            label = { Text("رقم الهاتف أو الواتساب الفعال (إجباري)") },
            placeholder = { Text("مثال: 777644670") },
            isError = phoneError,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = writeTextColor,
                unfocusedTextColor = writeTextColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
        if (phoneError) {
            Text("يرجى إدخال رقم هاتف يمني صحيح فعال يتألف من 9 أرقام", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 3: القسم الرئيسي المنسدل (إجباري) ---
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = mainCatExpanded,
                onExpandedChange = { mainCatExpanded = !mainCatExpanded }
            ) {
                OutlinedTextField(
                    value = mainCats.find { it.id == selectedMainCat }?.name ?: "اختر القسم والتصنيف الرئيسي...",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("القسم الرئيسي (إجباري)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mainCatExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = mainCatError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = writeTextColor,
                        unfocusedTextColor = writeTextColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                ExposedDropdownMenu(
                    expanded = mainCatExpanded,
                    onDismissRequest = { mainCatExpanded = false }
                ) {
                    mainCats.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.name, fontWeight = FontWeight.Bold) },
                            onClick = {
                                selectedMainCat = item.id
                                selectedSubCat = "" // Reset sub category dependency chain!
                                mainCatExpanded = false
                                mainCatError = false
                            }
                        )
                    }
                }
            }
        }
        if (mainCatError) {
            Text("يرجى اختيار القسم الرئيسي أولاً لتصفية المهن", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 4: الخدمة النوعية أو المهنة (إجباري) ---
        AnimatedVisibility(
            visible = selectedMainCat.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ExposedDropdownMenuBox(
                    expanded = subCatExpanded,
                    onExpandedChange = { subCatExpanded = !subCatExpanded }
                ) {
                    OutlinedTextField(
                        value = subCats.find { it.id == selectedSubCat }?.name ?: "اختر المهن أو التخصص المتاح...",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المهنة أو الخدمة الدقيقة (إجباري)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subCatExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        isError = subCatError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = writeTextColor,
                            unfocusedTextColor = writeTextColor,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Gray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = subCatExpanded,
                        onDismissRequest = { subCatExpanded = false }
                    ) {
                        filteredSubCats.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = {
                                    selectedSubCat = item.id
                                    subCatExpanded = false
                                    subCatError = false
                                }
                            )
                        }
                    }
                }
            }
        }
        if (subCatError && selectedMainCat.isNotEmpty()) {
            Text("يرجى تحديد تخصص الخدمة كشريك مهني", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 5: مكان وعنوان مركز العمل الحالي (إجباري) ---
        OutlinedTextField(
            value = addressDetails,
            onValueChange = {
                addressDetails = it
                addressError = false
            },
            label = { Text("عنوان العمل والشارع (إجباري)") },
            placeholder = { Text("مثال: شارع حدة - أمام بريد السبعين") },
            isError = addressError,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = writeTextColor,
                unfocusedTextColor = writeTextColor,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
        if (addressError) {
            Text("يرجى إدخال عنوان وتفاصيل مكتب العمل الفعلي", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 6: منطقة الدائرة السكنية الحالية (إجباري) ---
        Box(modifier = Modifier.fillMaxWidth()) {
            ExposedDropdownMenuBox(
                expanded = districtExpanded,
                onExpandedChange = { districtExpanded = !districtExpanded }
            ) {
                OutlinedTextField(
                    value = districtArea,
                    onValueChange = {
                        districtArea = it
                        districtError = false
                    },
                    label = { Text("منطقة المديرية / الدائرة السكنية (إجباري)") },
                    placeholder = { Text("مثال: مديرية السبعين") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    isError = districtError,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = writeTextColor,
                        unfocusedTextColor = writeTextColor,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Gray
                    )
                )
                ExposedDropdownMenu(
                    expanded = districtExpanded,
                    onDismissRequest = { districtExpanded = false }
                ) {
                    defaultDistricts.forEach { dist ->
                        DropdownMenuItem(
                            text = { Text(dist) },
                            onClick = {
                                districtArea = dist
                                districtExpanded = false
                                districtError = false
                            }
                        )
                    }
                }
            }
        }
        if (districtError) {
            Text("يرجى اختيار أو تدوين منطقة تواجدك السكنية", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 7: إحداثيات وموقع الخريطة GPS (اختياري) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = gpsCoordinates,
                onValueChange = { gpsCoordinates = it },
                label = { Text("إحداثيات وموقع GPS (اختياري)") },
                placeholder = { Text("مثال: 15.3184, 44.1950") },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = writeTextColor,
                    unfocusedTextColor = writeTextColor,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Gray
                ),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.PinDrop, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
            )
            Button(
                onClick = {
                    gpsCoordinates = "15.3694,44.1910" // Mock location fetch
                    Toast.makeText(context, "تم جلب موقع الخريطة GPS بنجاح!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.height(56.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("تحديد موقعي")
            }
        }

        // --- Field 8: تحميل الصورة الشخصية للملف (إجباري) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = if (avatarError) AlertRed else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("الصورة الشخصية للملف (إجباري)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(
                        text = if (avatarImageUri.isEmpty()) "لم يتم اختيار صورة بعد" else "تم تحميل: profile_image.png ✓",
                        color = if (avatarImageUri.isEmpty()) SoftWhite else MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
                Button(
                    onClick = {
                        avatarImageUri = "avatar_uploaded_${System.currentTimeMillis()}"
                        avatarError = false
                        Toast.makeText(context, "تم التقاط ورفع الصورة الشخصية للملف!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("رفع الصورة")
                }
            }
        }
        if (avatarError) {
            Text("رفع الصورة الشخصية إجباري لمطابقة الملف الفني", color = AlertRed, fontSize = 11.sp, modifier = Modifier.align(Alignment.Start))
        }

        // --- Field 9: صورة بطاقة الهوية الشخصية (اختياري) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("صورة بطاقة الهوية الشخصية (اختياري)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Text(
                        text = if (idCardImageUri.isEmpty()) "لم ترفق بطاقة الهوية العائلية" else "تم تحميل: national_id_card.png ✓",
                        color = if (idCardImageUri.isEmpty()) SoftWhite else MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                }
                OutlinedButton(
                    onClick = {
                        idCardImageUri = "id_card_uploaded_${System.currentTimeMillis()}"
                        Toast.makeText(context, "تم مسح بطاقة الهوية الوطنية بنجاح!", Toast.LENGTH_SHORT).show()
                    },
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Upload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إرفاق البطاقة", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.Gray)
            ) {
                Text("إلغاء")
            }

            Button(
                onClick = {
                    // Quick validation checks
                    if (fullName.isBlank()) nameError = true
                    if (phone.length < 6) phoneError = true
                    if (selectedMainCat.isEmpty()) mainCatError = true
                    if (selectedMainCat.isNotEmpty() && selectedSubCat.isEmpty()) subCatError = true
                    if (addressDetails.isBlank()) addressError = true
                    if (districtArea.isBlank()) districtError = true
                    if (avatarImageUri.isEmpty()) avatarError = true

                    if (!nameError && !phoneError && !mainCatError && !subCatError && !addressError && !districtError && !avatarError) {
                        // Submit request to pending_providers
                        val request = PendingProvider(
                            name = fullName,
                            phone = phone,
                            mainCategoryId = selectedMainCat,
                            subCategoryId = selectedSubCat,
                            address = addressDetails,
                            district = districtArea,
                            gpsCoordinates = gpsCoordinates.ifBlank { null },
                            avatarUrl = if (avatarImageUri.isBlank()) "avatar_male_1" else avatarImageUri,
                            idCardUrl = if (idCardImageUri.isBlank()) null else idCardImageUri
                        )

                        FirestoreSim.addPendingProvider(context, request)
                        Toast.makeText(context, "تم إرسال طلب انضمامك بنجاح! الإدارة تراجع طلبك بمتوسط 10 دقائق.", Toast.LENGTH_LONG).show()
                        onBack()
                    } else {
                        Toast.makeText(context, "يرجى تعبئة كافة الحقول الإجبارية وإصلاح الأخطاء الملونة بالأحمر", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("تقديم طلب الانضمام للخدمة", color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

private val SoftWhite = Color(0xFFC4C4C4)
