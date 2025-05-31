package com.pirateradio.danmakunotification

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.view.Gravity
import android.view.WindowManager
import android.graphics.PixelFormat
import android.util.DisplayMetrics
import kotlin.random.Random

class NotificationListener : NotificationListenerService() {
    private val TAG = "NotificationListener"

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "NotificationListenerService connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "NotificationListenerService disconnected")
        // 断开时尝试重新绑定
        requestRebind(ComponentName(this, NotificationListener::class.java))
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification posted: ${sbn.packageName}")
        if (sbn.packageName == "android") return
        // 检查应用是否启用弹幕通知
        val enabledApps = loadEnabledApps()
        if (!enabledApps.contains(sbn.packageName)) {
            Log.d(TAG, "Skipping notification for ${sbn.packageName} (not enabled)")
            return
        }
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString("android.title") ?: ""
        val text = sbn.notification.extras.getString("android.text") ?: ""
        showDanmaku(packageName, title, text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.d(TAG, "Notification removed: ${sbn.packageName}")
    }

    private fun showDanmaku(packageName: String, title: String, text: String) {
        try {
            val danmakuView = DanmakuView(this)
            danmakuView.setData(packageName, title, text)

            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val metrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val windowMetrics = wm.currentWindowMetrics
                metrics.widthPixels = windowMetrics.bounds.width()
                metrics.heightPixels = windowMetrics.bounds.height()
            } else {
                @Suppress("DEPRECATION")
                wm.defaultDisplay.getMetrics(metrics)
            }
            val screenWidth = metrics.widthPixels
            val screenHeight = metrics.heightPixels

            // 上半屏 4 条轨道
            val trackHeight = (screenHeight / 2) / 4
            val randomTrack = Random.nextInt(0, 4)
            val randomY = randomTrack * trackHeight + 20

            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                x = 0
                y = randomY
            }

            wm.addView(danmakuView, params)

            // 动画：从右到左
            ObjectAnimator.ofFloat(danmakuView, "translationX", 0f, -screenWidth.toFloat()).apply {
                duration = 5000
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        try {
                            wm.removeView(danmakuView)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                })
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e(TAG, "Error showing danmaku: ${e.message}")
        }
    }

    private fun loadEnabledApps(): Set<String> {
        val prefs = getSharedPreferences("danmaku_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("enabled_apps", emptySet()) ?: emptySet()
    }
}