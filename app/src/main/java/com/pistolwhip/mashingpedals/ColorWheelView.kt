package com.pistolwhip.mashingpedals

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.SweepGradient
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan2
import kotlin.math.min

class ColorWheelView(context: Context, private val selected: (Int) -> Unit) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var cx = 0f
    private var cy = 0f
    private var radius = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        cx = width / 2f
        cy = height / 2f
        radius = min(width, height) / 2f - 8f
        paint.shader = SweepGradient(cx, cy, intArrayOf(Color.RED, Color.MAGENTA, Color.BLUE, Color.CYAN, Color.GREEN, Color.YELLOW, Color.RED), null)
        canvas.drawCircle(cx, cy, radius, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        paint.color = Color.WHITE
        canvas.drawCircle(cx, cy, radius, paint)
        paint.style = Paint.Style.FILL
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_MOVE && event.action != MotionEvent.ACTION_UP) return true
        val angle = Math.toDegrees(atan2((event.y - cy).toDouble(), (event.x - cx).toDouble())).toFloat()
        val hue = (angle + 360f) % 360f
        selected(Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))
        return true
    }
}
