package com.pirateradio.danmakunotification

import android.content.ComponentName
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
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
import android.app.NotificationManager
import android.content.Context
import android.service.notification.NotificationListenerService
import androidx.annotation.RequiresApi
import androidx.compose.material3.IconButton
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pirateradio.danmakunotification.ui.theme.DanmakuNotificationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 设置窗口属性
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // 配置状态栏和导航栏
        setupSystemBars(window)
        setContent {
            DanmakuNotificationTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {
                    composable("main") { MainScreen(navController) }
                    composable("app_selection") { AppSelectionScreen(navController, LocalContext.current) }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onResume() {
        super.onResume()
        // 在恢复时检查并重新绑定 NotificationListenerService
        checkAndRebindNotificationListener()
    }

    private fun setupSystemBars(window: Window) {
        // 获取 WindowInsetsController
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        // 根据主题设置状态栏文字/图标颜色
        controller.isAppearanceLightStatusBars = !isDarkTheme()
        // 设置导航栏透明
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        // 根据主题设置导航栏图标颜色
        controller.isAppearanceLightNavigationBars = !isDarkTheme()
        // 移除导航栏背景（API 29+）
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }

    private fun isDarkTheme(): Boolean {
        // 检查系统是否为深色主题
        return resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK ==
                android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    private fun checkAndRebindNotificationListener() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val componentName = ComponentName(this, NotificationListener::class.java)
        val isEnabled = notificationManager.isNotificationListenerAccessGranted(componentName)
        if (isEnabled) {
            // 如果权限已授予但服务未运行，尝试重新绑定
            NotificationListenerService.requestRebind(componentName)
        } else {
            // 提示用户启用通知权限
            Toast.makeText(this, "请启用通知权限以使用弹幕通知", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun MainScreen(navController: androidx.navigation.NavController) {
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
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(
                        onClick = {
                            val componentName = ComponentName(context, NotificationListener::class.java)
                            NotificationListenerService.requestRebind(componentName)
                            Toast.makeText(context, "已尝试重新连接通知服务", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "重新连接通知服务",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        navController.navigate("app_selection")
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
                    .fillMaxSize()
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