package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "المساعد الذكي 🤖",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "اغلاق",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Chat body
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        BubbleChat(message = msg)
                    }
                }

                // Preset suggestion chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val contextLocal = LocalContextCurrent()
                    listOf("أريد كهربائي صيانة", "كيف أسجل مهنتي؟", "من الموصى بهم؟").forEach { hint ->
                        Card(
                            shape = RoundedCornerShape(50),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
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
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Input field
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("اكتب استفسارك هنا...") },
                        modifier = Modifier.weight(1f),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.background,
                            unfocusedContainerColor = MaterialTheme.colorScheme.background,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                messages.add(Message(inputText, true))
                                val userQuery = inputText
                                inputText = ""
                                messages.add(Message("جاري التفكير...", false))
                                val response = getBotResponse(userQuery)
                                messages.removeAt(messages.size - 1)
                                messages.add(Message(response, false))
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "ارسال",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
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
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (isUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
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
