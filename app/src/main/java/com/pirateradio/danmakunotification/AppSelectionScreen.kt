package com.pirateradio.danmakunotification

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSelectionScreen(navController: NavController, context: Context) {
    // 状态管理
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    val enabledApps = remember { mutableStateMapOf<String, Boolean>().apply {
        loadEnabledApps(context).forEach { packageName ->
            this[packageName] = true
        }
    } }
    val scope = rememberCoroutineScope()

    // 异步加载应用列表
    LaunchedEffect(Unit) {
        scope.launch {
            isLoading = true
            apps = withContext(Dispatchers.IO) { getInstalledApps(context) }
            isLoading = false
            Log.d("AppSelectionScreen", "Loaded ${apps.size} apps")
        }
    }

    // 过滤应用列表基于搜索关键字
    val filteredApps = apps.filter { app ->
        app.name.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "选择弹幕通知应用",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // 搜索栏
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                placeholder = { Text("搜索应用") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "搜索"
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                )
            )

            // 内容区域
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                filteredApps.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isEmpty()) "未找到可用的应用" else "没有匹配的应用",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(filteredApps) { app ->
                            AppItem(
                                app = app,
                                isEnabled = enabledApps[app.packageName] ?: false,
                                onToggle = { isChecked ->
                                    enabledApps[app.packageName] = isChecked
                                    saveEnabledApps(context, enabledApps.filterValues { it }.keys)
                                }
                            )
                            Divider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: android.graphics.drawable.Drawable?
)

@Composable
fun AppItem(app: AppInfo, isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 应用图标
        app.icon?.let { drawable ->
            val bitmap = try {
                (drawable as android.graphics.drawable.BitmapDrawable).bitmap
            } catch (e: Exception) {
                null
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 16.dp)
                )
            }
        }
        // 应用名称
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        // 开关
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle
        )
    }
}

private fun getInstalledApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val apps = try {
        pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                // 仅过滤本应用
                appInfo.packageName != context.packageName
            }
            .map { appInfo ->
                AppInfo(
                    name = appInfo.loadLabel(pm).toString(),
                    packageName = appInfo.packageName,
                    icon = try {
                        pm.getApplicationIcon(appInfo.packageName)
                    } catch (e: PackageManager.NameNotFoundException) {
                        null
                    }
                )
            }
            .sortedBy { it.name }
    } catch (e: Exception) {
        Log.e("AppSelectionScreen", "Error loading apps: ${e.message}")
        emptyList()
    }
    Log.d("AppSelectionScreen", "Filtered apps: ${apps.map { it.packageName }}")
    return apps
}

private fun loadEnabledApps(context: Context): Set<String> {
    val prefs = context.getSharedPreferences("danmaku_prefs", Context.MODE_PRIVATE)
    // 默认启用的预设应用
    val predefinedApps = setOf(
        "com.tencent.mm", // 微信
        "com.tencent.mobileqq" // QQ
    )
    // 如果没有保存的应用列表，初始化为预设应用
    if (!prefs.contains("enabled_apps")) {
        prefs.edit {
            putStringSet("enabled_apps", predefinedApps)
        }
        return predefinedApps
    }
    return prefs.getStringSet("enabled_apps", emptySet()) ?: emptySet()
}

private fun saveEnabledApps(context: Context, enabledApps: Set<String>) {
    val prefs = context.getSharedPreferences("danmaku_prefs", Context.MODE_PRIVATE)
    prefs.edit {
        putStringSet("enabled_apps", enabledApps)
    }
}