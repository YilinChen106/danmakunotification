package com.pirateradio.danmakunotification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class PackageReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                Log.d("PackageReceiver", "Package changed: ${intent.data?.schemeSpecificPart}")
                // 更新缓存
                val apps = getInstalledApps(context)
                saveAppsToCache(context, apps)
            }
        }
    }
}