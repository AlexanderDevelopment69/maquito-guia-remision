package com.maquitop.guiaremision.utils

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.io.File
import java.io.FileOutputStream

class SignatureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
        strokeWidth = 5f
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }

    private var path = Path()
    private var bitmap: Bitmap? = null
    private var canvas: Canvas? = null
    private var lastX = 0f
    private var lastY = 0f
    private var isEmpty = true

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w > 0 && h > 0) {
            val newBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val newCanvas = Canvas(newBitmap)
            newCanvas.drawColor(Color.WHITE)
            
            // Si ya había algo dibujado, lo preservamos (opcional)
            bitmap?.let { oldBitmap ->
                newCanvas.drawBitmap(oldBitmap, 0f, 0f, null)
            }
            
            bitmap = newBitmap
            canvas = newCanvas
        }
    }

    override fun onDraw(c: Canvas) {
        bitmap?.let { c.drawBitmap(it, 0f, 0f, null) }
        c.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(true)
                path.moveTo(x, y)
                lastX = x
                lastY = y
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                path.quadTo(lastX, lastY, (x + lastX) / 2, (y + lastY) / 2)
                lastX = x
                lastY = y
                isEmpty = false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                canvas?.drawPath(path, paint)
                path.reset()
                parent.requestDisallowInterceptTouchEvent(false)
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        bitmap?.let { it.eraseColor(Color.WHITE) }
        isEmpty = true
        invalidate()
    }

    fun isEmpty(): Boolean = isEmpty

    fun saveToFile(file: File): Boolean {
        if (isEmpty) return false
        return try {
            FileOutputStream(file).use { fos ->
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getBitmap(): Bitmap? = bitmap
}
