package com.example.testapplication.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.testapplication.SessionManager
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class LifeWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = LifeEngine()

    inner class LifeEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private val sessionManager = SessionManager(applicationContext)
        private var visible = false

        private val drawRunnable = Runnable { drawFrame() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) drawFrame()
            else handler.removeCallbacks(drawRunnable)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderGrid(canvas)
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas)
            }
            
            if (visible) {
                // Refresh every minute to check for date change
                handler.postDelayed(drawRunnable, 60000)
            }
        }

        private fun renderGrid(canvas: Canvas) {
            val w = canvas.width.toFloat()
            val h = canvas.height.toFloat()

            // Background
            canvas.drawColor(Color.parseColor("#080F08"))

            // Calculate current day
            val startMillis = sessionManager.getLifeStartTime()
            val startDate = LocalDate.ofEpochDay(startMillis / (1000 * 60 * 60 * 24))
            val today = LocalDate.now()
            val currentDay = ChronoUnit.DAYS.between(startDate, today).toInt() + 1
            val totalDays = 360

            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
            
            val cols = 15
            val rows = (totalDays / cols) + 1
            val size = (w * 0.8f) / cols
            val gap = size * 0.2f
            val startX = (w - (cols * size + (cols - 1) * gap)) / 2f
            val startY = h * 0.25f

            // Drawing the grid
            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val dayIdx = r * cols + c
                    if (dayIdx >= totalDays) break

                    val x = startX + c * (size + gap)
                    val y = startY + r * (size + gap)

                    val isFilled = dayIdx < currentDay
                    
                    if (isFilled) {
                        if (dayIdx == currentDay - 1) {
                            fillPaint.color = Color.parseColor("#2196F3") // Current active day
                        } else {
                            fillPaint.color = Color.parseColor("#1565C0") // Past days
                            fillPaint.alpha = 150
                        }
                    } else {
                        fillPaint.color = Color.parseColor("#1E272E") // Future days
                    }

                    canvas.drawRoundRect(x, y, x + size, y + size, 6f, 6f, fillPaint)
                }
            }

            // Header Text
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                textSize = 60f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }
            canvas.drawText("Day $currentDay", w / 2f, startY - 100f, textPaint)
            
            textPaint.textSize = 30f
            textPaint.alpha = 180
            canvas.drawText("Life Journey Progress", w / 2f, startY - 180f, textPaint)
        }
    }
}
