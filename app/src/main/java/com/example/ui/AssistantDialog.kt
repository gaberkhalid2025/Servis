package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AssistantDialog(
    onDismiss: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            Message("مرحباً بك في المساعد الذكي لدليلك الخدمي! كيف يمكنني مساعدتك اليوم؟ 🛠️✨", false),
            Message("يمكنني مساعدتك في العثور على أفضل الفنيين المعتمدين، أو شرح كيفية الانضمام كمزود خدمة.", false)
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(10.dp)
                .border(2.dp, Color(0xFFFFD700), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF131114)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Block: Close Button ('X') on the left, Title "المساعد الذكي (خدمات) ⚡" on the right in RTL vibe.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "اغلاق",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Row(
                        modifier = Modifier.align(Alignment.Center),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "⚡ المساعد الذكي (خدمات) ⚡",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Scrollable Chat/Information Area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Description Info Card as in the screenshot
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1F1B24)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "أنا مساعدك الذكي لمختلف الأقسام (طوارئ 🚨، عيادات ومستشفيات 🏥، كهرباء وسباكة 🛠️، تعليم 🎓، سيارات 🚗).",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 22.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Right
                            )
                            Text(
                                text = "كيف يمكنني مساعدتك اليوم؟ يمكنك كتابة استفسارك للبحث السريع.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Right
                            )
                        }
                    }

                    // Interactive chat replies when messages are exchanged
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(messages) { msg ->
                            BubbleChat(message = msg)
                        }
                    }
                }

                // Preset suggestion chips hidden as requested because answers don't show there
                /*
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("كهربائي صيانة", "كيف أسجل مهنتي؟", "من الموصى بهم؟").forEach { hint ->
                        Card(
                            shape = RoundedCornerShape(50),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFD700).copy(alpha = 0.12f)
                            ),
                            modifier = Modifier
                                .clickable {
                                    messages.add(Message(hint, true))
                                    messages.add(Message("جاري التفكير...", false))
                                    val response = getBotResponse(hint)
                                    messages.removeAt(messages.size - 1)
                                    messages.add(Message(response, false))
                                }
                        ) {
                            Text(
                                text = hint,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = Color(0xFFFFD700),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                */

                // Text input box with yellow border
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "اسأل: أين أقرب مستشفى طوارئ؟",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1F1B24),
                        unfocusedContainerColor = Color(0xFF1F1B24),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFFFD700),
                        unfocusedBorderColor = Color(0xFFFFD700).copy(alpha = 0.6f)
                    ),
                    singleLine = true
                )

                // Large action button: "إرسال الاستعلام" as in screenshot
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            messages.add(Message(inputText, true))
                            val queryText = inputText
                            inputText = ""
                            messages.add(Message("جاري التفكير...", false))
                            val response = getBotResponse(queryText)
                            messages.removeAt(messages.size - 1)
                            messages.add(Message(response, false))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD700),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "إرسال الاستعلام",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun BubbleChat(message: Message) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bgColor = if (isUser) {
        Color(0xFFFFD700) // Distinct gold color for user
    } else {
        Color(0xFF2C2C32) // Contrasting slate gray for assistant
    }
    val textColor = if (isUser) {
        Color.Black // Black text on gold background
    } else {
        Color.White // White text on dark gray background
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        contentAlignment = if (isUser) Alignment.TopEnd else Alignment.TopStart
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 2.dp,
                            bottomEnd = if (isUser) 2.dp else 16.dp
                        )
                    )
                    .background(bgColor)
                    .padding(12.dp)
                    .widthIn(max = 240.dp)
            ) {
                Text(
                    text = message.text,
                    color = textColor,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
            Text(
                text = message.time,
                fontSize = 9.sp,
                color = SoftGray,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
    }
}

data class Message(
    val text: String,
    val isUser: Boolean,
    val time: String = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
)

private fun getBotResponse(query: String): String {
    val q = query.lowercase(Locale.getDefault())
    return when {
        q.contains("كهربائي") || q.contains("كهربا") -> {
            "لدينا فنيين معتمدين مثل الفني الممتاز 'ماهر طاهر' في السبعين بصنعاء، تواصل معه على 777644670. كما يوجد كهربائيون آخرون في قائمة الصيانة المنزلية!"
        }
        q.contains("تسجيل") || q.contains("انضم") || q.contains("أسجل") || q.contains("حساب") -> {
            "لتسجيل مهنتك، اضغط على أيقونة إضافة مستخدم (👤) في الشريط العلوي، واملأ استمارة التسجيل الكاملة (الاسم، الهاتف الفعال، قسمك السكني، الصورة الشخصية وصورة بطاقة هويتك) ثم أرسل الطلب للمراجعة الفورية!"
        }
        q.contains("طبيب") || q.contains("دكتور") || q.contains("صحة") -> {
            "نعم! في قسم الصحة والرعاية، لدينا د. سارة فضل عبدالله طبيبة منزلية معتمدة في التحرير بتعز تواصل معها على 733445566!"
        }
        q.contains("مستشار") || q.contains("مساعد") || q.contains("خدمات") -> {
            "أنا المساعد الذكي لدليل الخدمات. يمكنني إرشادك لأقسام الصيانة، الرعاية الصحية، التعليم والتدريب، والنقل. يمكنك تصفية البحث حسب مدينتك لسهولة التنقل!"
        }
        q.contains("دعم") || q.contains("تواصل") || q.contains("مشكلة") -> {
            "يمكنك التواصل مع الدعم الفني بالضغط على أيقونة المعلومات (ℹ️) في الجوانب السفلية لمعرفة وسائط التواصل وواتساب الدعم المباشر ومكالمة المدير!"
        }
        q.contains("موصى") || q.contains("توصية") || q.contains("نجمة") -> {
            "مقدمو الخدمات الموصى بهم يظهرون بنجمة ذهبية ⭐ في شريط متحرك مخصص أعلى القائمة الرئيسية للتطبيق، ويتم اختيارهم من الإدارة لكفاءتهم العالية وأمانتهم!"
        }
        else -> {
            "طلبك مفهوم! يمكنك تصفح دليلنا المتكامل عن طريق النقر على الأقسام على الشاشة الرئيسية، أو كتابة اسم المهنة أو رقم هاتف المهني في شريط البحث للوصول المباشر."
        }
    }
}

// Simple placeholder color
private val SoftGray = Color(0xFF8E8E93)

@Composable
fun LocalContextCurrent() = androidx.compose.ui.platform.LocalContext.current
