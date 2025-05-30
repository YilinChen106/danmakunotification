package com.pirateradio.danmakunotification

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.WindowManager
import android.view.Gravity
import android.graphics.PixelFormat
import android.animation.ObjectAnimator

class NotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val title = sbn.notification.extras.getString("android.title") ?: ""
        val text = sbn.notification.extras.getString("android.text") ?: ""
        showDanmaku(packageName, title, text)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        // 可选
    }

    private fun showDanmaku(packageName: String, title: String, text: String) {
        val danmakuView = DanmakuView(this)
        danmakuView.setData(packageName, title, text)
        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            x = 0
            y = 100
        }
        wm.addView(danmakuView, params)
        ObjectAnimator.ofFloat(danmakuView, "translationX", 0f, 1000f).apply {
            duration = 5000
            start()
        }
    }
}