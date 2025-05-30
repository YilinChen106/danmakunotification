package com.pirateradio.danmakunotification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.util.DisplayMetrics
import android.view.View
import androidx.core.graphics.scale
import kotlin.math.max

class DanmakuView(context: Context) : View(context) {
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        alpha = 128
        textSize = 30f
    }
    private var icon: Bitmap? = null
    private var title: String = ""
    private var text: String = ""
    private val iconSize = 60
    private val height = 90
    private var contentWidth: Int = 450 // 可见内容宽度
    private var screenWidth: Int
    private var contentRect: RectF? = null // 可见弹幕区域
    private val transparentRect: RectF // 透明占位区域

    init {
        // 获取屏幕宽度
        val metrics = context.resources.displayMetrics
        screenWidth = metrics.widthPixels
        transparentRect = RectF(0f, 0f, screenWidth.toFloat(), height.toFloat())
    }

    fun setData(packageName: String, title: String, text: String) {
        this.title = title.take(40)
        this.text = text.take(40)
        try {
            val originalIcon = (context.packageManager.getApplicationIcon(packageName) as BitmapDrawable).bitmap
            icon = originalIcon.scale(iconSize, iconSize, filter = true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        // 计算可见内容宽度
        val titleWidth = paint.measureText(this.title)
        val textWidth = paint.measureText(this.text)
        val maxTextWidth = max(titleWidth, textWidth)
        contentWidth = (iconSize + 30 + maxTextWidth + 20).toInt()
        contentWidth = max(contentWidth, 200) // 最小 200px
        contentRect = RectF(0f, 0f, contentWidth.toFloat(), height.toFloat())
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 视图宽度为屏幕宽度，高度固定
        setMeasuredDimension(screenWidth, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制透明占位矩形
        paint.color = Color.TRANSPARENT
        paint.alpha = 0
        canvas.drawRect(transparentRect, paint)

        // 绘制可见弹幕
        contentRect?.let {
            paint.color = Color.GRAY
            paint.alpha = 128
            val radius = height / 2f
            canvas.drawRoundRect(it, radius, radius, paint)
        }

        // 绘制图标
        icon?.let {
            canvas.drawBitmap(it, 15f, 15f, null)
        }

        // 绘制文字
        paint.color = Color.WHITE
        paint.textSize = 30f
        paint.alpha = 255
        canvas.drawText(title, 90f, 35f, paint)
        canvas.drawText(text, 90f, 75f, paint)
    }
}