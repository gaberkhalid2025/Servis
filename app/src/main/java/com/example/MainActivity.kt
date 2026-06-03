package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FirestoreSim
import com.example.ui.*
import com.example.ui.theme.AlertRed
import com.example.ui.theme.DynamicAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sync and bootstrap databases
        FirestoreSim.initialize(this)

        setContent {
            val appColors = FirestoreSim.appColors.collectAsState().value
            DynamicAppTheme(themeName = appColors.themeName, textColorName = appColors.textColorName) {
                var currentScreen by remember { mutableStateOf("home") }
                var currentSessionRole by remember { mutableStateOf("none") } // "none", "moderator", "owner"

                val globalConfigs = FirestoreSim.appConfigs.collectAsState().value

                // If maintenance active AND the logged-in user isn't admin/owner, show lock screen
                if (globalConfigs.isMaintenanceActive && currentSessionRole == "none") {
                    MaintenanceLockScreen(
                        onUnlockEscape = {
                            currentScreen = "login"
                            currentSessionRole = "temp_bypass" // Give transient role to bypass loop
                        }
                    )
                } else {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            Crossfade(targetState = currentScreen) { screen ->
                                when (screen) {
                                    "home" -> {
                                        MainScreen(
                                            currentSession = if (currentSessionRole == "temp_bypass") "none" else currentSessionRole,
                                            onNavigate = { route ->
                                                if (currentSessionRole == "temp_bypass") {
                                                    // Reset temp bypass on voluntary navigate
                                                    currentSessionRole = "none"
                                                }
                                                currentScreen = route
                                            },
                                            onBackdoorUnlock = {
                                                currentScreen = "login"
                                            }
                                        )
                                    }

                                    "about" -> {
                                        AboutScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }

                                    "join" -> {
                                        JoinScreen(
                                            onBack = { currentScreen = "home" }
                                        )
                                    }

                                    "login" -> {
                                        AuthScreen(
                                            currentSession = currentSessionRole,
                                            onLoginSuccess = { role ->
                                                currentSessionRole = role
                                                currentScreen = if (role == "owner") "owner" else "admin"
                                            },
                                            onBack = {
                                                if (currentSessionRole == "temp_bypass") {
                                                    currentSessionRole = "none"
                                                }
                                                currentScreen = "home"
                                            }
                                        )
                                    }

                                    "admin" -> {
                                        AdminPanel(
                                            onLogout = {
                                                currentSessionRole = "none"
                                                currentScreen = "home"
                                            },
                                            onBackToHome = {
                                                currentScreen = "home"
                                            }
                                        )
                                    }

                                    "owner" -> {
                                        OwnerPanel(
                                            onLogout = {
                                                currentSessionRole = "none"
                                                currentScreen = "home"
                                            },
                                            onBackToHome = {
                                                currentScreen = "home"
                                            }
                                        )
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

@Composable
fun MaintenanceLockScreen(
    onUnlockEscape: () -> Unit
) {
    var unlockClickCount by remember { mutableStateOf(0) }
    var lastClickTime by remember { mutableStateOf(0L) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1216))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Escape Hatch Button: Admin can tap 5 times to enter auth and override mode
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = {
                    val currTime = System.currentTimeMillis()
                    if (currTime - lastClickTime < 1000) {
                        unlockClickCount++
                    } else {
                        unlockClickCount = 1
                    }
                    lastClickTime = currTime

                    if (unlockClickCount >= 5) {
                        unlockClickCount = 0
                        onUnlockEscape()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = Color.DarkGray
                )
            }
        }

        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = AlertRed,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "تطبيق دليلك لكل الخدمات",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B22)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "الخدمة متوقفة مؤقتاً للصيانة",
                    fontWeight = FontWeight.Bold,
                    color = AlertRed,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "عذراً يا عميلنا العزيز! تطبيق دليلك يخضع لإعدادات وتطوير دوري دوراني مستمر لترقية الخوادم وتحسين الأداء لمطابقة أحدث سمات اليمن لعام 2026.\n\nنتطلع لعودتكم الميمونة والكاملة خلال بضعة دقائق للتجريب والتصفح الفوري. شكراً لتفهمكم وصبركم المعهود!",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        CircularProgressIndicator(color = AlertRed, strokeWidth = 2.dp)
    }
}
