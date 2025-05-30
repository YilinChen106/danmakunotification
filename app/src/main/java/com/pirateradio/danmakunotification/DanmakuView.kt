package com.pirateradio.danmakunotification

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.view.View

class DanmakuView(context: Context) : View(context) {
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.GRAY
        alpha = 128
    }
    private var icon: Bitmap? = null
    private var title: String = ""
    private var text: String = ""
    private val width = 450  // 放大 1.5 倍 (300 * 1.5)
    private val height = 90  // 放大 1.5 倍 (60 * 1.5)
    private val iconSize = 60  // 图标大小，适应高度

    fun setData(packageName: String, title: String, text: String) {
        this.title = title
        this.text = text
        try {
            // 获取原始图标
            val originalIcon = (context.packageManager.getApplicationIcon(packageName) as BitmapDrawable).bitmap
            // 缩放图标到 iconSize
            icon = Bitmap.createScaledBitmap(originalIcon, iconSize, iconSize, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 绘制背景
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radius = height / 2f
        canvas.drawRoundRect(rect, radius, radius, paint)

        // 绘制图标
        icon?.let {
            canvas.drawBitmap(it, 15f, 15f, null) // 调整边距
        }

        // 绘制文字
        paint.color = Color.WHITE
        paint.textSize = 30f  // 放大 1.5 倍 (20 * 1.5)
        canvas.drawText(title, 90f, 35f, paint)  // 调整标题位置，留出图标空间
        canvas.drawText(text, 90f, 75f, paint)  // 调整文本位置
    }
}