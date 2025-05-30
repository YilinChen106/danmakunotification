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
    private val width = 300
    private val height = 60

    fun setData(packageName: String, title: String, text: String) {
        this.title = title
        this.text = text
        try {
            icon = (context.packageManager.getApplicationIcon(packageName) as BitmapDrawable).bitmap
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
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        val radius = height / 2f
        canvas.drawRoundRect(rect, radius, radius, paint)
        icon?.let {
            canvas.drawBitmap(it, 10f, 10f, null)
        }
        paint.color = Color.WHITE
        paint.textSize = 20f
        canvas.drawText(title, 50f, 25f, paint)
        canvas.drawText(text, 50f, 50f, paint)
    }
}