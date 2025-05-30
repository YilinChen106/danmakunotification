package com.pirateradio.danmakunotification

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Window
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.pirateradio.danmakunotification.ui.theme.DanmakuNotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set up window properties
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Configure status and navigation bar
        setupSystemBars(window)
        setContent {
            DanmakuNotificationTheme {
                MainScreen()
            }
        }
    }

    private fun setupSystemBars(window: Window) {
        // Get the WindowInsetsController
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        // Set status bar text/icons appearance based on theme
        controller.isAppearanceLightStatusBars = !isDarkTheme()
        // Set navigation bar to transparent
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        // Ensure navigation bar icons are visible based on theme
        controller.isAppearanceLightNavigationBars = !isDarkTheme()
        // Remove any scrim or background from navigation bar (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun isDarkTheme(): Boolean {
        // Simple check for dark theme based on system settings
        return resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val onlyLandscape = remember { mutableStateOf(false) }
    val autoDnd = remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp)
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // 标题区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()

                    .padding(vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "弹幕通知",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .height(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.DarkGray)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "1.0",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }

            // 权限设置
            Text(
                text = "权限设置",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "“弹幕通知”需要必要的权限才能正常工作。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            // 通知权限
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开启通知权限",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                        context.startActivity(intent)
                    }
                ) {
                    Text("去开启")
                }
            }

            // 悬浮窗权限
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开启悬浮窗权限",
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                        context.startActivity(intent)
                    }
                ) {
                    Text("去开启")
                }
            }

            // 偏好设置
            Text(
                text = "偏好设置",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 应用选择
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "打开应用选择页面", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "开启弹幕通知的应用",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // 仅横屏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "仅在横屏下开启弹幕通知",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = onlyLandscape.value,
                    onCheckedChange = { onlyLandscape.value = it }
                )
            }

            // 免打扰
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "横屏下自动开启免打扰",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Switch(
                        checked = autoDnd.value,
                        onCheckedChange = { autoDnd.value = it }
                    )
                }
                Text(
                    text = "弹幕通知仍旧生效。请注意，若其他应用有相似的功能，可能会产生冲突。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // 关于
            Text(
                text = "关于",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(top = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        Toast.makeText(context, "打开关于页面", Toast.LENGTH_SHORT).show()
                    }
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "关于本应用",
                    style = MaterialTheme.typography.bodyLarge
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Navigate",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}