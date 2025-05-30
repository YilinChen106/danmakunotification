package com.pirateradio.danmakunotification

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.Gravity
import android.view.WindowManager
import android.graphics.PixelFormat

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString("android.title") ?: ""
        val text = sbn.notification.extras.getString("android.text") ?: ""
        showDanmaku(packageName, title, text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    private fun showDanmaku(packageName: String, title: String, text: String) {
        try {
            val danmakuView = DanmakuView(this)
            danmakuView.setData(packageName, title, text)

            // 获取屏幕宽度
            val wm = getSystemService(WINDOW_SERVICE) as WindowManager
            val screenWidth = resources.displayMetrics.widthPixels

            // 动态选择 WindowManager 类型，兼容 API 24
            val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                windowType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END // 使用 Gravity.END 替代 RIGHT
                x = 0  // 初始位置贴右边
                y = 100  // 距离顶部 100px
            }

            wm.addView(danmakuView, params)

            // 动画：从右到左横跨屏幕
            ObjectAnimator.ofFloat(danmakuView, "translationX", 0f, -(screenWidth + 450f)).apply {
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
        }
    }
}