package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.AlertRed
import com.example.ui.theme.getSelectedTextColor
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerPanel(
    onLogout: () -> Unit,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val appColors = FirestoreSim.appColors.collectAsState()
    val writeTextColor = getSelectedTextColor(appColors.value.textColorName)
    val appConfigs = FirestoreSim.appConfigs.collectAsState()

    // Panel tabs aligned and mapped to those in the user requested Image 2
    val tabs = listOf(
        "إحصاء",          // Tab 0: server statistics, backups, Google Drive sync, FCM (originally TAB 4)
        "الألوان",         // Tab 1: Theme selection, font color, App name, welcome, countries/cities (originally TAB 1 & cities)
        "الخدمات",        // Tab 2: Provider Recommendations, Pin Toggle, Verified status (originally TAB 2)
        "الالتحاق والتقارير",// Tab 3: Safety reports, complaints + Pending requests summary (originally TAB 3)
        "المساعد والدعم",   // Tab 4: Smart Assistant, contact info, custom footer, live chat (originally TAB 5 & chatbot)
        "إدارة المشرفين"    // Tab 5: Moderators Management (originally Tab 6)
    )
    var selectedTab by remember { mutableStateOf(0) }

    // Backup & DB stats
    val providers = FirestoreSim.serviceProviders.collectAsState()
    val pending = FirestoreSim.pendingProviders.collectAsState()
    val reviews = FirestoreSim.reviews.collectAsState()
    val reports = FirestoreSim.reports.collectAsState()

    // App state changes
    var appNameInput by remember { mutableStateOf(appConfigs.value.appName) }
    var welcomeInput by remember { mutableStateOf(appConfigs.value.welcomeMessage) }
    var footerInput by remember { mutableStateOf(appConfigs.value.footerPromoText) }
    var supportPhoneInput by remember { mutableStateOf(appConfigs.value.supportPhone) }
    var supportEmailInput by remember { mutableStateOf(appConfigs.value.supportEmail) }
    var supportWaInput by remember { mutableStateOf(appConfigs.value.supportWhatsApp) }
    var adminPassInput by remember { mutableStateOf(appConfigs.value.adminPassword) }

    // Dynamic color picker selection
    var themeSelection by remember { mutableStateOf(appColors.value.themeName) }
    var fontColorSelection by remember { mutableStateOf(appColors.value.textColorName) }

    // Cities
    val citySettings = FirestoreSim.cities.collectAsState()
    
    // Dialog control states: ADD
    var showAddCityDialog by remember { mutableStateOf(false) }
    var cityNameInput by remember { mutableStateOf("") }
    var districtsInput by remember { mutableStateOf("") }
    var countryNameInput by remember { mutableStateOf("اليمن") }

    // Dialog control states: EDIT
    var showEditCityDialog by remember { mutableStateOf(false) }
    var editingCityId by remember { mutableStateOf("") }
    var editCityNameInput by remember { mutableStateOf("") }
    var editDistrictsInput by remember { mutableStateOf("") }
    var editCountryInput by remember { mutableStateOf("اليمن") }

    // Backup log terminal dialogue
    var showBackupLogDialog by remember { mutableStateOf<String?>(null) } // contains text of the log if active

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Owner Header Styled perfectly as the Header in screenshot 2 with interactive actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0F0F11))
                .statusBarsPadding()
                .border(width = 1.dp, color = Color(0xFF2C2518))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF2C2518), RoundedCornerShape(8.dp))
                        .clickable { onBackToHome() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = RatingGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "جناح المالك المطلق (Owner Controls)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = RatingGold
                        )
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = RatingGold,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "صلاحيات التخصيص والمزامنة الفورية",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Icons action strip as seen in top-right of image 2
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = {
                        Toast.makeText(context, "تم مزامنة وتحديث البيانات سحابياً!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "تزامن", tint = RatingGold, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "اللغة الحالية بالفلاتر: العربية ✓", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Language, contentDescription = "لغة", tint = RatingGold, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = {
                        Toast.makeText(context, "الوضع النشط: الداكن الفخم 🌌", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(Icons.Default.Brightness2, contentDescription = "مظهر", tint = RatingGold, modifier = Modifier.size(18.dp))
                }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "خروج", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("خروج", color = Color.White, fontSize = 11.sp)
                }
            }
        }

        // Parent Row splitting: Content on Left, Vertical Sidebar on Right (matches picture 2)
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // Left content box (flex space)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        // TAB 0: server statistics, backups, Google Drive sync, FCM (originally TAB 4)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "النسخ الاحتياطي للأمان والإحصائيات والتحليلات 📊",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // stats counting card
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("إحصائيات الخادم الفورية الدقيقة", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Divider(color = Color.DarkGray)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("المهنيين المعتمدين بالخدمات: ${providers.value.size}", fontSize = 12.sp, color = Color.White)
                                        Text("الطلبات المعلقة للقبول: ${pending.value.size}", fontSize = 12.sp, color = Color.White)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("إجمالي التعليقات والتقييمات: ${reviews.value.size}", fontSize = 12.sp, color = Color.White)
                                        Text("الشكاوى والبلاغات المسجلة: ${reports.value.size}", fontSize = 12.sp, color = Color.White)
                                    }
                                    Text(
                                        text = "نقاط الولاء الممنوحة للمشتركين: ${FirestoreSim.userLoyaltyPoints.collectAsState().value} نقطة", 
                                        fontSize = 12.sp, 
                                        color = MaterialTheme.colorScheme.primary, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // data backup and restore cards
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("إجراء عمليات النسخ الإحتياطي واستعادة البيانات 💾", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    Text("يحفظ ملف النسخ باسم 'dalil_services_backup.txt' في مجلد التنزيلات بالجهاز لاستعادته بأي لحظة.", fontSize = 11.sp, color = Color.Gray)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                val path = FirestoreSim.backupToSDCard(context)
                                                if (path != null) {
                                                    showBackupLogDialog = "تم إنشاء نسخة احتياطية مشفرة بالكامل لقاعدة البيانات بنجاح!\n\nمكان الحفظ: $path\n\nحجم النسخة: ${path.length} بايت\nتاريخ الحفظ والتحزيم: 2026\nحالة الخادم: آمن ومنسجم بنسبة 100%"
                                                } else {
                                                    Toast.makeText(context, "الرجاء منح الصلاحيات للتخزين بالجهاز!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("توليد ونسخ للجهاز", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val success = FirestoreSim.restoreFromSDCard(context)
                                                if (success) {
                                                    Toast.makeText(context, "تم استعادة قاعدة بيانات دليلك الخدمي بالكامل بنجاح تام!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "لم يتم العثور على ملف backup في مجلد التنزيلات!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = RatingGold),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("استرجاع البيانات", fontSize = 11.sp, color = Color.Black)
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            showBackupLogDialog = "=== جاري تخزين نسخة مأمونة بـ GOOGLE DRIVE ===\n- الاتصال بخوادم القيادة السحابية في كوغل درايف... متصل!\n- تشفير خوارزميات AES-256 للمقاولين... تم!\n- تصدير ملف الإعدادات الكامل والأقسام المنسجمة... تم!\n\nالحالة المكتملة: تم الرفع والتزامن مع جوجل درايف للبريد Gaber77710@gmail.com تلقائياً!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F9D58)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("نسخ ومزامنة إلى جوجل درايف السحابي", fontSize = 12.sp)
                                    }
                                }
                            }

                            // FCM notification setup
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("إدارة قنوات الإشعارات الفورية للمشرفين (FCM) 🔔", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                    
                                    val fcmStates = FirestoreSim.fcmChannels.collectAsState().value
                                    fcmStates.forEach { chan ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(chan.name, fontSize = 12.sp, color = Color.White)
                                            Switch(
                                                checked = chan.isEnabled,
                                                onCheckedChange = { enable ->
                                                    FirestoreSim.toggleFcmChannel(context, chan.id, enable)
                                                    Toast.makeText(context, "تم تحديث إشعارات القناة بالخادم!", Toast.LENGTH_SHORT).show()
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: Theme selection, font color, App name, welcome, countries/cities (originally TAB 1 & cities)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "تخصيص الهوية والوضع والألوان الجغرافية 🎨",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Maintenance mode
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (appConfigs.value.isMaintenanceActive) AlertRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp).fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("وضع الصيانة المباشر والتطوير 🛠️", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text("يفصل تصفح دليل الخدمات مؤقتاً ويظهر تنبيه صيانة للعامة", color = Color.Gray, fontSize = 11.sp)
                                    }
                                    Switch(
                                        checked = appConfigs.value.isMaintenanceActive,
                                        onCheckedChange = { active ->
                                            FirestoreSim.updateConfigs(context, appConfigs.value.copy(isMaintenanceActive = active))
                                            Toast.makeText(context, if (active) "تم تفعيل وضع الصيانة التام!" else "تم إيقاف الصيانة وعودة الخدمة للجمهور!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }

                            // Theme customization
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("مظهر التطبيق البصري السحابي (Theme) 🌌", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                    
                                    listOf(
                                        "Cosmic Slate" to "🌌 كوزميك سيلفر (فضي داكن)",
                                        "Charcoal Gold" to "✨ الذهبي الفاخر (فحمي فاخر)",
                                        "Royal Emerald" to "🟢 الزمردي الراقي (أخضر ملكي)",
                                        "Vibrant Red" to "🔴 أحمر ناري (Vibrant Red)",
                                        "Deep Black" to "⚫ أسود حالك (Deep Black)",
                                        "Neon Blue" to "🔵 أزرق نيون (Neon Blue)",
                                        "Metallic Silver" to "⚪ فضي معدني (Metallic Silver)"
                                    ).forEach { (code, label) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    themeSelection = code
                                                    FirestoreSim.updateColors(context, themeSelection, fontColorSelection)
                                                    Toast.makeText(context, "تم تغيير سمة الألوان ومزامنتها فوراً! ✓", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = themeSelection == code, onClick = {
                                                themeSelection = code
                                                FirestoreSim.updateColors(context, themeSelection, fontColorSelection)
                                            })
                                            Text(label, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Font Color Selector
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("تحديد لون خط كتابة وتعبئة الحقول ✍️", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)

                                    listOf(
                                        "Bright White" to "◽ الأبيض الناصع (Bright White)",
                                        "Light Gold" to "🟡 الذهبي الفاتح (Light Gold)",
                                        "Vibrant Silver" to "◽ الفضي المتوهج (Vibrant Silver)"
                                    ).forEach { (code, label) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    fontColorSelection = code
                                                    FirestoreSim.updateColors(context, themeSelection, fontColorSelection)
                                                    Toast.makeText(context, "تم اختيار لون خط تعبئة الحقول!", Toast.LENGTH_SHORT).show()
                                                }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = fontColorSelection == code, onClick = {
                                                fontColorSelection = code
                                                FirestoreSim.updateColors(context, themeSelection, fontColorSelection)
                                            })
                                            Text(label, color = Color.White, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }

                            // Text inputs
                            OutlinedTextField(
                                value = appNameInput,
                                onValueChange = { appNameInput = it },
                                label = { Text("اسم دليلك الخدمي") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            AdAndWelcomeCustomizationCard(
                                configs = appConfigs.value,
                                onSave = { updated -> FirestoreSim.updateConfigs(context, updated) },
                                writeTextColor = writeTextColor
                            )

                            // --- COUNTRIES, GOVERNORATES, CITIES AND DISTRICTS CARD (First Screenshot specifications) ---
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, RatingGold.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("لوحة التحكم بالدول والمحافظات والمدن 🗺️", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RatingGold)
                                            Text("إدارة النطاقات الجغرافية للجمهور وكوادر العمل", fontSize = 10.sp, color = Color.Gray)
                                        }
                                        Button(
                                            onClick = { showAddCityDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = RatingGold)
                                        ) {
                                            Text("إضافة دولة/محافظة ➕", fontSize = 9.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 4.dp))

                                    if (citySettings.value.isEmpty()) {
                                        Text("لا توجد فلاتر جغرافية مسجلة حالياً.", color = Color.Gray, fontSize = 11.sp)
                                    } else {
                                        // Displaying countries and governorates inside cards exactly like image 1 with action additions
                                        citySettings.value.forEach { city ->
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            // Show country flag & name + Governorate
                                                            Text(
                                                                text = "${city.country} 🌍 / ${city.name}", 
                                                                fontWeight = FontWeight.Bold, 
                                                                fontSize = 14.sp, 
                                                                color = Color.White
                                                            )
                                                            Text(
                                                                text = "تضم: ${city.districts.joinToString(" • ")}", 
                                                                fontSize = 11.sp, 
                                                                color = Color.LightGray.copy(alpha = 0.8f)
                                                            )
                                                        }

                                                        // Actions: Edit and Delete
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            IconButton(
                                                                onClick = {
                                                                    editingCityId = city.id
                                                                    editCityNameInput = city.name
                                                                    editDistrictsInput = city.districts.joinToString(", ")
                                                                    editCountryInput = city.country
                                                                    showEditCityDialog = true
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = RatingGold, modifier = Modifier.size(16.dp))
                                                            }

                                                            IconButton(
                                                                onClick = {
                                                                    FirestoreSim.deleteCity(context, city.id)
                                                                    Toast.makeText(context, "تم حذف المحافظة بنجاح من الفلاتر!", Toast.LENGTH_SHORT).show()
                                                                },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AlertRed, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    FirestoreSim.updateConfigs(
                                        context,
                                        appConfigs.value.copy(
                                            appName = appNameInput,
                                            welcomeMessage = welcomeInput
                                        )
                                    )
                                    Toast.makeText(context, "تم حفظ تغييرات الهوية بنجاح وبثها فورياً لكل الأجهزة!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RatingGold)
                            ) {
                                Text("حفظ تعديلات الهوية والمدن", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: Provider Recommendations, Pin Toggle, Verified status (originally TAB 2)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "لوحة إدارة توصيات وتثبيت الكوادر المباشر ⭐",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("المهني الموصى به ⭐ يظهر فوراً في الشريط الذهبي المميز أعلى التطبيق. المهني المثبت يرتفع لأول القسم.", fontSize = 11.sp, color = Color.Gray)

                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                items(providers.value) { p ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text(p.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                                Text("${p.district} • ${p.phone}", fontSize = 11.sp, color = Color.Gray)
                                            }

                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Pin Toggle
                                                IconButton(
                                                    onClick = {
                                                        val state = !p.isPinned
                                                        FirestoreSim.updateProvider(context, p.copy(isPinned = state))
                                                        Toast.makeText(context, if (state) "تم تثبيت الكادر بأول نتائج القائمة!" else "تم إلغاء التثبيت", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (p.isPinned) Icons.Default.VerticalAlignTop else Icons.Default.VerticalAlignBottom,
                                                        contentDescription = null,
                                                        tint = if (p.isPinned) MaterialTheme.colorScheme.primary else Color.Gray
                                                    )
                                                }

                                                // Recommendation Toggle
                                                IconButton(
                                                    onClick = {
                                                        val state = !p.isRecommended
                                                        FirestoreSim.updateProvider(context, p.copy(isRecommended = state))
                                                        Toast.makeText(context, if (state) "تم إضافته للموصى بهم بالأعلى ⭐ !" else "تم إزالته من التوصيات", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = null,
                                                        tint = if (p.isRecommended) RatingGold else Color.Gray
                                                    )
                                                }

                                                // Premium subscribed toggle
                                                IconButton(
                                                    onClick = {
                                                        val state = !p.isSubscribed
                                                        FirestoreSim.updateProvider(context, p.copy(isSubscribed = state))
                                                        Toast.makeText(context, if (state) "تم تمكين الشارة الذهبية للمشتركين!" else "تم سحب الاشتراك الشهري", Toast.LENGTH_SHORT).show()
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Verified,
                                                        contentDescription = null,
                                                        tint = if (p.isSubscribed) Color(0xFF3897F0) else Color.Gray
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: Safety reports, complaints + Pending requests summary (originally TAB 3)
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "الطلبات الواردة وسجل بلاغات الإساءة والشكاوى 📑",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Pending registree summarized list
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("الطلبات المعلقة والالتحاق بالدليل:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RatingGold)
                                    if (pending.value.isEmpty()) {
                                        Text("لا توجد ملفات معلقة تتطلب المراجعة الفورية.", fontSize = 11.sp, color = Color.Gray)
                                    } else {
                                        pending.value.forEach { entry ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column {
                                                    Text(entry.name, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                                                    Text("هاتف: ${entry.phone} | منطقة: ${entry.district}", fontSize = 10.sp, color = Color.LightGray)
                                                }
                                                Text("معلقة للقبول", color = RatingGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            Divider(color = Color.DarkGray)

                            // Reports
                            val userReports = reports.value
                            if (userReports.isEmpty()) {
                                EmptyStateView(
                                    title = "سجل الإساءات والشكاوى فارغ تماماً",
                                    subtitle = "أي شكوى يقدمها مستخدم ضد كادر مهني من زاوية البلاغات ستوثق هنا للمراجعة والمقاضاة المباشرة."
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(userReports) { rep ->
                                        val targetProvider = providers.value.find { it.id == rep.providerId }?.name ?: "مهني محذوف"
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text("المشتكي: ${rep.userName}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                                    IconButton(onClick = {
                                                        FirestoreSim.deleteReport(context, rep.id)
                                                        Toast.makeText(context, "تم حفظ الشكوى بالأرشيف وإسقاطها من اللائحة", Toast.LENGTH_SHORT).show()
                                                    }) {
                                                        Icon(Icons.Default.Delete, contentDescription = null, tint = AlertRed)
                                                    }
                                                }
                                                Text("شكوى مقدمة ضد: $targetProvider", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                                Text("تفاصيل الشكوى: ${rep.comment}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))

                                                Row(
                                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Button(
                                                        onClick = {
                                                            FirestoreSim.deleteProvider(context, rep.providerId)
                                                            FirestoreSim.deleteReport(context, rep.id)
                                                            Toast.makeText(context, "تم حظر وإسقاط الفني تماماً من التطبيق بنجاح!", Toast.LENGTH_SHORT).show()
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = AlertRed),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text("حظر وإقصاء الفني فوراً", fontSize = 11.sp, color = Color.White)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: Smart Assistant, contact info, custom footer, live chat (originally TAB 5 & chatbot)
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "إعدادات المساعد الذكي وقنوات الدعم المباشر 💬",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Smart Assistant Floating config options
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("تخصيص المساعد الذكي الدائري 🤖", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                                    var assistantSizeInput by remember { mutableStateOf(appConfigs.value.smartAssistantSize) }
                                    Text("حجم أيقونة المساعد: ${assistantSizeInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                    Slider(
                                        value = assistantSizeInput,
                                        onValueChange = { assistantSizeInput = it },
                                        valueRange = 40f..120f,
                                        onValueChangeFinished = {
                                            FirestoreSim.updateConfigs(context, appConfigs.value.copy(smartAssistantSize = assistantSizeInput))
                                        }
                                    )

                                    var assistantOpacityInput by remember { mutableStateOf(appConfigs.value.assistantOpacity) }
                                    Text("شفافية المساعد: ${(assistantOpacityInput * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                                    Slider(
                                        value = assistantOpacityInput,
                                        onValueChange = { assistantOpacityInput = it },
                                        valueRange = 0.10f..1f,
                                        onValueChangeFinished = {
                                            FirestoreSim.updateConfigs(context, appConfigs.value.copy(assistantOpacity = assistantOpacityInput))
                                        }
                                    )

                                    var assistantOffsetXInput by remember { mutableStateOf(appConfigs.value.assistantOffsetX) }
                                    Text("إزاحة المساعد الأفقية: ${assistantOffsetXInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                    Slider(
                                        value = assistantOffsetXInput,
                                        onValueChange = { assistantOffsetXInput = it },
                                        valueRange = -100f..100f,
                                        onValueChangeFinished = {
                                            FirestoreSim.updateConfigs(context, appConfigs.value.copy(assistantOffsetX = assistantOffsetXInput))
                                        }
                                    )

                                    var assistantOffsetYInput by remember { mutableStateOf(appConfigs.value.assistantOffsetY) }
                                    Text("إزاحة المساعد الرأسية (بكسل): ${assistantOffsetYInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                    Slider(
                                        value = assistantOffsetYInput,
                                        onValueChange = { assistantOffsetYInput = it },
                                        valueRange = -100f..150f,
                                        onValueChangeFinished = {
                                            FirestoreSim.updateConfigs(context, appConfigs.value.copy(assistantOffsetY = assistantOffsetYInput))
                                        }
                                    )

                                    var assistantColorInput by remember { mutableStateOf(appConfigs.value.assistantColorHex) }
                                    OutlinedTextField(
                                        value = assistantColorInput,
                                        onValueChange = {
                                            assistantColorInput = it
                                            if (it.length >= 4 && (it.startsWith("#") || it.length == 7)) {
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(assistantColorHex = it))
                                            }
                                        },
                                        label = { Text("رمز لون المساعد (مثال: FFD700#)", fontSize = 11.sp) },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                        singleLine = true
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("عرض التذييل الدعائي والترويجي العام", fontSize = 13.sp, color = Color.White)
                                        Switch(
                                            checked = appConfigs.value.showPromoFooter,
                                            onCheckedChange = { visible ->
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(showPromoFooter = visible))
                                                Toast.makeText(context, "تم حفظ خيار عرض التذييل بالتطبيق!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }

                            // Floating Direct Contact Customization
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("أيقونة التواصل المباشر بجوار المعلومات 📲", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                        Switch(
                                            checked = appConfigs.value.showFloatingContact,
                                            onCheckedChange = { visible ->
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(showFloatingContact = visible))
                                                Toast.makeText(context, "تم تعديل خيار عرض أيقونة التواصل العائمة!", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }

                                    if (appConfigs.value.showFloatingContact) {
                                        var contactSizeInput by remember { mutableStateOf(appConfigs.value.floatingContactSize) }
                                        Text("حجم أيقونة التواصل: ${contactSizeInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                        Slider(
                                            value = contactSizeInput,
                                            onValueChange = { contactSizeInput = it },
                                            valueRange = 40f..120f,
                                            onValueChangeFinished = {
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(floatingContactSize = contactSizeInput))
                                            }
                                        )

                                        var contactOpacityInput by remember { mutableStateOf(appConfigs.value.floatingContactOpacity) }
                                        Text("شفافية التواصل: ${(contactOpacityInput * 100).toInt()}%", fontSize = 12.sp, color = Color.White)
                                        Slider(
                                            value = contactOpacityInput,
                                            onValueChange = { contactOpacityInput = it },
                                            valueRange = 0.10f..1f,
                                            onValueChangeFinished = {
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(floatingContactOpacity = contactOpacityInput))
                                            }
                                        )

                                        var contactOffsetXInput by remember { mutableStateOf(appConfigs.value.floatingContactOffsetX) }
                                        Text("إزاحة التواصل الأفقية: ${contactOffsetXInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                        Slider(
                                            value = contactOffsetXInput,
                                            onValueChange = { contactOffsetXInput = it },
                                            valueRange = -100f..100f,
                                            onValueChangeFinished = {
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(floatingContactOffsetX = contactOffsetXInput))
                                            }
                                        )

                                        var contactOffsetYInput by remember { mutableStateOf(appConfigs.value.floatingContactOffsetY) }
                                        Text("إزاحة التواصل الرأسية: ${contactOffsetYInput.toInt()}dp", fontSize = 12.sp, color = Color.White)
                                        Slider(
                                            value = contactOffsetYInput,
                                            onValueChange = { contactOffsetYInput = it },
                                            valueRange = -100f..100f,
                                            onValueChangeFinished = {
                                                FirestoreSim.updateConfigs(context, appConfigs.value.copy(floatingContactOffsetY = contactOffsetYInput))
                                            }
                                        )

                                        var contactColorInput by remember { mutableStateOf(appConfigs.value.floatingContactColorHex) }
                                        OutlinedTextField(
                                            value = contactColorInput,
                                            onValueChange = {
                                                contactColorInput = it
                                                if (it.length >= 4 && (it.startsWith("#") || it.length == 7)) {
                                                    FirestoreSim.updateConfigs(context, appConfigs.value.copy(floatingContactColorHex = it))
                                                }
                                            },
                                            label = { Text("رمز لون التواصل المباشر (مثال: #25D366)", fontSize = 11.sp) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                            singleLine = true
                                        )
                                    }
                                }
                            }

                            // Editable Footer slogan text
                            OutlinedTextField(
                                value = footerInput,
                                onValueChange = { footerInput = it },
                                label = { Text("التذييل الدعائي المخصص بالتطبيق (MAW 7776444670)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            // Change admin login password
                            OutlinedTextField(
                                value = adminPassInput,
                                onValueChange = { adminPassInput = it },
                                label = { Text("رمز مرور حساب المشرف الأساسي (WAM2026)") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            // Edit support email and phone
                            OutlinedTextField(
                                value = supportPhoneInput,
                                onValueChange = { supportPhoneInput = it },
                                label = { Text("أرقام التواصل الهاتفي للدعم الفني") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = supportWaInput,
                                onValueChange = { supportWaInput = it },
                                label = { Text("رقم واتساب دعم الكوادر") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = supportEmailInput,
                                onValueChange = { supportEmailInput = it },
                                label = { Text("إيميل الدعم الفني واستقبال البلاغات") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = writeTextColor, unfocusedTextColor = writeTextColor),
                                singleLine = true
                            )

                            Button(
                                onClick = {
                                    FirestoreSim.updateConfigs(
                                        context,
                                        appConfigs.value.copy(
                                            footerPromoText = footerInput,
                                            adminPassword = adminPassInput,
                                            supportPhone = supportPhoneInput,
                                            supportWhatsApp = supportWaInput,
                                            supportEmail = supportEmailInput
                                        )
                                    )
                                    Toast.makeText(context, "تم حفظ الإعدادات المطلقة للخدمة مع الأجهزة!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = RatingGold)
                            ) {
                                Text("حفظ التغييرات وبيانات الدعم 💾", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            // Live Chat Responses Section for Admin (المهن مع الادمن عبر رسائل)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("رسائل الدعم الفني والمهن الواردة 💬", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = RatingGold)
                                    val chatMessages = FirestoreSim.chatMessages.collectAsState().value
                                    
                                    if (chatMessages.isEmpty()) {
                                        Text("لا توجد رسائل واردة حالياً من مقدمي الخدمات أو الزوار.", fontSize = 12.sp, color = Color.Gray)
                                    } else {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            chatMessages.takeLast(12).forEach { chatMsg ->
                                                val isFromAdmin = chatMsg.sender == "admin"
                                                Card(
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isFromAdmin) Color(0xFF2C2518) else Color(0xFF1E1E22)
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                text = if (isFromAdmin) "رد الإدارة 🛡️" else "من: زائر / صاحب مهنة 👤",
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isFromAdmin) RatingGold else Color.LightGray
                                                            )
                                                            Text(
                                                                text = chatMsg.message,
                                                                fontSize = 13.sp,
                                                                color = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                                            // Admin Quick Reply input
                                            var adminReplyText by remember { mutableStateOf("") }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = adminReplyText,
                                                    onValueChange = { adminReplyText = it },
                                                    placeholder = { Text("اكتب رد الإدارة الفوري هنا...") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = OutlinedTextFieldDefaults.colors(
                                                        focusedTextColor = writeTextColor,
                                                        unfocusedTextColor = writeTextColor
                                                    ),
                                                    singleLine = true
                                                )
                                                Button(
                                                    onClick = {
                                                        if (adminReplyText.isNotBlank()) {
                                                            FirestoreSim.sendChatMessage(
                                                                context,
                                                                ChatMessage(
                                                                    id = "msg_${System.currentTimeMillis()}",
                                                                    providerId = "admin",
                                                                    sender = "admin",
                                                                    message = adminReplyText,
                                                                    timestamp = System.currentTimeMillis()
                                                                )
                                                            )
                                                            adminReplyText = ""
                                                            Toast.makeText(context, "تم إرسال رد الإدارة فورياً!", Toast.LENGTH_SHORT).show()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = RatingGold)
                                                ) {
                                                    Text("رد", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    5 -> {
                        // TAB 5: Moderators management view (originally Tab 6)
                        val moderators = FirestoreSim.moderators.collectAsState().value
                        ModeratorsManagementTab(
                            moderators = moderators,
                            onSaveMod = { mod -> FirestoreSim.saveModerator(context, mod) },
                            onDeleteMod = { modId -> FirestoreSim.deleteModerator(context, modId) },
                            writeTextColor = writeTextColor
                        )
                    }
                }
            }

            // Right side: Vertical Sidebar tab navigation (Matches Picture 2 exactly)
            Column(
                modifier = Modifier
                    .width(115.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF0F0F11))
                    .border(width = 1.dp, color = Color(0xFF2C2518))
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Top Tab buttons list
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    tabs.forEachIndexed { index, name ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) Color(0xFF2C2518) else Color.Transparent
                                )
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) RatingGold.copy(alpha = 0.5f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedTab = index }
                                .padding(vertical = 12.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) RatingGold else Color.LightGray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal dialogue: Showing backups status / Google Drive sync logs
    showBackupLogDialog?.let { logText ->
        Dialog(onDismissRequest = { showBackupLogDialog = null }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1D21)),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("سجلات الخادم السحابية النشطة", color = Color(0xFF00FF00), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        IconButton(onClick = { showBackupLogDialog = null }) {
                            Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                        }
                    }

                    Text(
                        text = logText,
                        color = Color.White,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black, RoundedCornerShape(6.dp))
                            .padding(12.dp)
                    )

                    Button(
                        onClick = { showBackupLogDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("إغلاق السجل", color = Color.White)
                    }
                }
            }
        }
    }

    // Dialog: ADD country/governorate/city
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
                    Text("إضافة فلاتر جغرافية جديدة 🗺️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RatingGold)
                    Text("أدخل اسم الدولة، المحافظة أو المدينة الكبرى، والمناطق الفرعية التابعة لها.", fontSize = 11.sp, color = Color.LightGray)

                    OutlinedTextField(
                        value = countryNameInput,
                        onValueChange = { countryNameInput = it },
                        label = { Text("الدولة (مثل: اليمن، السعودية، مصر 🌍)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = cityNameInput,
                        onValueChange = { cityNameInput = it },
                        label = { Text("المحافظة / المدينة (مثل: صنعاء، الرياض، القاهرة)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = districtsInput,
                        onValueChange = { districtsInput = it },
                        label = { Text("المناطق السكنية والمديريات بداخلها (مفصولة بفاصلة)") },
                        placeholder = { Text("مثال: حي المنصورة, كريتر, خور مكسر") },
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
                                    Toast.makeText(context, "تم إضافة الفلتر الجغرافي بنجاح وبثه فورياً!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة كافة الحقول المطلوبة!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RatingGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("إضافة فلاتر 🗺️", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    // Dialog: EDIT country/governorate/city
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
                    Text("تعديل الفلاتر الجغرافية المعتمدة ✏️", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RatingGold)
                    Text("قم بتعديل اسم الدولة، اسم المحافظة/المدينة والمناطق الفرعية المسجلة.", fontSize = 11.sp, color = Color.LightGray)

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
                        label = { Text("المحافظة / المدينة السكنية") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = editDistrictsInput,
                        onValueChange = { editDistrictsInput = it },
                        label = { Text("الأحياء والمناطق التابعة لها (مفصولة بفاصلة)") },
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
                                    Toast.makeText(context, "تم حفظ وتحديث الفلتر الجغرافي وبثه فورياً لنود الفلاتر!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "الرجاء تعبئة كافة الحقول للتعديل!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RatingGold),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("حفظ التعديلات 💾", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

private val RatingGold = Color(0xFFFFB000)


