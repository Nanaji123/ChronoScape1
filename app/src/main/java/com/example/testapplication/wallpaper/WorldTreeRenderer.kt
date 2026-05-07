package com.example.testapplication.wallpaper

import android.graphics.*
import kotlin.math.*
import kotlin.random.Random

class WorldTreeRenderer {

    // ── Paints ──────────────────────────────────────────────────────────
    private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val groundFillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val groundGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val trunkFillPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3E2006")
                style = Paint.Style.FILL
            }
    private val trunkEdgePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5C3010")
                style = Paint.Style.STROKE
                strokeWidth = 4f
            }
    private val barkPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#1A0E07")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
            }
    private val branchPaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5C3010")
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val vinePaint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
    private val flowerPetalPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    // ── Seeds ───────────────────────────────────────────────────────────
    private val vineSeeds = (0..10).map { Random.nextFloat() }
    private val starSeeds = (0..60).map { Pair(Random.nextFloat(), Random.nextFloat()) }

    fun draw(
            canvas: Canvas,
            w: Float,
            h: Float,
            growth: Float,
            windTime: Float,
            dayNightTime: Float = 0f
    ) {
        val cx = w / 2f
        val groundY = h * 0.90f
        val treeH = (growth / 2000f * h * 0.65f).coerceAtLeast(10f)
        val windSway = sin(windTime.toDouble()).toFloat() * 12f * (growth / 2000f)
        val dayPhase = sin(dayNightTime.toDouble()).toFloat()
        val seasonProgress = (growth / 500f) % 4f

        // 1. Environment
        drawSky(canvas, w, h, groundY, dayPhase, seasonProgress)
        if (dayPhase < 0f) drawStars(canvas, w, h * 0.8f, windTime)
        drawSunMoon(canvas, cx, w, h, dayPhase, windTime)

        // 2. Ground
        drawGround(canvas, w, h, groundY, seasonProgress)

        // 3. Tree Core
        if (growth > 40f) {
            val topX = cx + windSway
            val topY = groundY - treeH
            val baseHalf = (14f + growth / 2000f * 65f).coerceAtMost(80f)
            val topHalf = baseHalf * 0.35f

            drawTrunk(canvas, cx, groundY, topX, topY, baseHalf, topHalf, windSway, treeH)

            val leaves = getSeasonColors(seasonProgress)
            drawCanopy(
                    canvas,
                    cx,
                    groundY,
                    topX,
                    topY,
                    treeH,
                    growth,
                    windTime,
                    windSway,
                    baseHalf,
                    topHalf,
                    leaves
            )
        }

        // 4. Ecosystem detail
        if (growth > 150f) {
            drawGroundVines(canvas, cx, w, groundY, growth, windTime)
        }
    }

    private fun drawSky(
            canvas: Canvas,
            w: Float,
            h: Float,
            groundY: Float,
            phase: Float,
            season: Float
    ) {
        val t = (phase + 1f) / 2f
        val topNight = Color.parseColor("#060D1A")
        val topDay = Color.parseColor("#4FC3F7")
        val botNight = Color.parseColor("#0E2E1C")
        val botDay = Color.parseColor("#80D8FF")

        val top = blendColors(topNight, topDay, t)
        val bot = blendColors(botNight, botDay, t)

        skyPaint.shader =
                LinearGradient(
                        0f,
                        0f,
                        0f,
                        groundY,
                        intArrayOf(top, bot),
                        null,
                        Shader.TileMode.CLAMP
                )
        canvas.drawRect(0f, 0f, w, groundY, skyPaint)
    }

    private fun drawStars(canvas: Canvas, w: Float, h: Float, wind: Float) {
        repeat(starSeeds.size) { i ->
            val (sx, sy) = starSeeds[i]
            val twinkle = (sin(wind.toDouble() + i).toFloat() + 1f) * 0.5f
            leafPaint.color = Color.WHITE
            leafPaint.alpha = (100 + twinkle * 155).toInt()
            canvas.drawCircle(sx * w, sy * h, 1.2f, leafPaint)
        }
    }

    private fun drawSunMoon(
            canvas: Canvas,
            cx: Float,
            w: Float,
            h: Float,
            phase: Float,
            wind: Float
    ) {
        val path = abs(phase)
        val py = h * 0.15f + (1f - path) * h * 0.45f
        val px = (phase + 1f) / 2f * w
        if (phase >= 0f) {
            leafPaint.color = Color.parseColor("#FFD600")
            canvas.drawCircle(px, py, 35f, leafPaint)
            // Rays
            branchPaint.color = Color.parseColor("#FFD600")
            branchPaint.alpha = (phase * 150).toInt()
            repeat(8) { i ->
                val ang = (i / 8f) * 6.28f + wind * 0.2f
                canvas.drawLine(
                        px + cos(ang) * 40f,
                        py + sin(ang) * 40f,
                        px + cos(ang) * 55f,
                        py + sin(ang) * 55f,
                        branchPaint
                )
            }
        } else {
            leafPaint.color = Color.parseColor("#ECEFF1")
            canvas.drawCircle(px, py, 28f, leafPaint)
        }
    }

    private fun drawGround(canvas: Canvas, w: Float, h: Float, groundY: Float, season: Float) {
        groundFillPaint.color = Color.parseColor("#1A0E07")
        canvas.drawRect(0f, groundY, w, h, groundFillPaint)
        val glowCol =
                if (season > 2.5f) Color.parseColor("#78909C") else Color.parseColor("#388E3C")
        groundGlowPaint.shader =
                LinearGradient(
                        0f,
                        groundY,
                        0f,
                        groundY + 60f,
                        intArrayOf(glowCol, Color.TRANSPARENT),
                        null,
                        Shader.TileMode.CLAMP
                )
        canvas.drawRect(0f, groundY, w, groundY + 60f, groundGlowPaint)
    }

    private fun drawTrunk(
            canvas: Canvas,
            cx: Float,
            gy: Float,
            tx: Float,
            ty: Float,
            bh: Float,
            th: Float,
            sway: Float,
            treeH: Float
    ) {
        val path = Path()
        val midY = gy - treeH * 0.45f
        val lCtrlX = cx - bh * 1.05f + sway * 0.2f
        val rCtrlX = cx + bh * 1.05f + sway * 0.2f
        path.moveTo(cx - bh, gy)
        path.quadTo(lCtrlX, midY, tx - th, ty)
        path.lineTo(tx + th, ty)
        path.quadTo(rCtrlX, midY, cx + bh, gy)
        path.close()
        canvas.drawPath(path, trunkFillPaint)
        canvas.drawPath(path, trunkEdgePaint)
        // Bark
        barkPaint.alpha = 70
        repeat(5) { i ->
            val bP = (i + 1) / 6f
            val bx = cx - bh + bh * 2 * bP
            val tx2 = tx - th + th * 2 * bP
            val mX = cx + (tx - cx) * 0.5f + (bP - 0.5f) * bh * 1.8f
            val p = Path()
            p.moveTo(bx, gy)
            p.quadTo(mX, gy - treeH * 0.5f, tx2, ty)
            canvas.drawPath(p, barkPaint)
        }
    }

    private fun drawCanopy(
            canvas: Canvas,
            cx: Float,
            gy: Float,
            tx: Float,
            ty: Float,
            treeH: Float,
            growth: Float,
            wt: Float,
            sway: Float,
            bh: Float,
            th: Float,
            colors: List<Int>
    ) {
        // Main branches
        val branchPoints = listOf(0.25f, 0.45f, 0.65f, 0.82f, 0.94f)
        branchPoints.forEachIndexed { i, f ->
            val by = gy - treeH * f
            val bxMid = cx + (tx - cx) * f
            val bw = bh + (th - bh) * f
            val blen = treeH * (0.35f - i * 0.03f).coerceAtLeast(0.15f)
            val bt = bw * 0.4f

            drawBR(
                    canvas,
                    bxMid - bw,
                    by,
                    -(Math.PI * 0.65f).toFloat(),
                    blen,
                    bt,
                    0,
                    3,
                    growth,
                    wt,
                    sway,
                    colors
            )
            drawBR(
                    canvas,
                    bxMid + bw,
                    by,
                    -(Math.PI * 0.35f).toFloat(),
                    blen,
                    bt,
                    0,
                    3,
                    growth,
                    wt,
                    sway,
                    colors
            )
            if (f > 0.5f) {
                drawBR(
                        canvas,
                        bxMid,
                        by,
                        -(Math.PI * 0.5f).toFloat(),
                        blen * 0.6f,
                        bt * 0.6f,
                        0,
                        3,
                        growth,
                        wt,
                        sway,
                        colors
                )
            }
        }
        // Top cap
        repeat(6) { i ->
            val ang = -(Math.PI * (0.22f + i * 0.11f)).toFloat()
            drawBR(canvas, tx, ty, ang, treeH * 0.2f, th * 0.4f, 0, 4, growth, wt, sway, colors)
        }
    }

    private fun drawBR(
            canvas: Canvas,
            x: Float,
            y: Float,
            ang: Float,
            len: Float,
            thick: Float,
            dep: Int,
            max: Int,
            growth: Float,
            wt: Float,
            sway: Float,
            colors: List<Int>
    ) {
        if (dep > max || len < 4f) return
        val dGrowth = ((growth - (200f + dep * 250f)) / 200f).coerceIn(0f, 1f)
        if (dGrowth <= 0f) return

        val phase = wt.toDouble() + x * 0.01 + dep
        val bSway = sin(phase).toFloat() * sway * (dep + 1) / (max + 1)
        val curLen = len * dGrowth
        val ex = x + cos(ang + bSway / 25f) * curLen
        val ey = y + sin(ang + bSway / 25f) * curLen

        branchPaint.strokeWidth = thick.coerceAtLeast(1.5f)
        canvas.drawLine(x, y, ex, ey, branchPaint)

        if (dGrowth > 0.15f) {
            val alpha = ((dGrowth - 0.15f) / 0.85f * 255f).toInt()
            drawLeafCluster(canvas, ex, ey, len * 0.42f, alpha, bSway, dep, colors)
        }

        if (dep < max) {
            val nl = len * 0.62f
            val nt = thick * 0.58f
            drawBR(canvas, ex, ey, ang - 0.44f, nl, nt, dep + 1, max, growth, wt, sway, colors)
            drawBR(canvas, ex, ey, ang + 0.44f, nl, nt, dep + 1, max, growth, wt, sway, colors)
        }
    }

    private fun drawLeafCluster(
            canvas: Canvas,
            cx: Float,
            cy: Float,
            radius: Float,
            alpha: Int,
            sway: Float,
            dep: Int,
            colors: List<Int>
    ) {
        val count = (8 - dep).coerceAtLeast(4)
        repeat(count) { i ->
            val a = (i / count.toFloat()) * 6.28f + sway / 12f
            val r = radius * (0.5f + (i % 2) * 0.15f)
            val d = radius * 0.65f
            drawLeafShape(
                    canvas,
                    cx + cos(a) * d,
                    cy + sin(a) * d,
                    r,
                    a * 57f,
                    colors[i % colors.size],
                    alpha
            )
        }
        leafPaint.color = colors.last()
        leafPaint.alpha = alpha
        canvas.drawCircle(cx, cy, radius * 0.3f, leafPaint)
    }

    private fun drawLeafShape(
            canvas: Canvas,
            x: Float,
            y: Float,
            sz: Float,
            ang: Float,
            col: Int,
            alpha: Int
    ) {
        leafPaint.color = col
        leafPaint.alpha = alpha
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(ang)
        val p = Path()
        p.moveTo(0f, -sz)
        p.cubicTo(sz * 0.6f, -sz * 0.5f, sz * 0.6f, sz * 0.4f, 0f, sz * 0.45f)
        p.cubicTo(-sz * 0.6f, sz * 0.4f, -sz * 0.6f, -sz * 0.5f, 0f, -sz)
        canvas.drawPath(p, leafPaint)
        canvas.restore()
    }

    private fun drawGroundVines(
            canvas: Canvas,
            cx: Float,
            w: Float,
            gy: Float,
            growth: Float,
            wt: Float
    ) {
        repeat(4) { i ->
            val dir = if (i % 2 == 0) -1f else 1f
            val reach = w * 0.4f * ((growth - 200f - i * 80f) / 600f).coerceIn(0f, 1f)
            if (reach <= 0f) return@repeat
            val path = Path()
            path.moveTo(cx, gy)
            for (s in 0..25) {
                val t = s / 25f
                val lx = cx + dir * reach * t
                val ho = sin(t * 8f + vineSeeds[i] * 5f).toFloat() * 12f
                path.lineTo(lx, gy - ho)
            }
            vinePaint.color = Color.parseColor("#2E7D32")
            vinePaint.strokeWidth = 3f
            canvas.drawPath(path, vinePaint)
        }
    }

    private fun getSeasonColors(progress: Float): List<Int> {
        val s = progress.toInt() % 4
        return when (s) {
            1 -> listOf("#1B5E20", "#2E7D32", "#1A4A10").map { Color.parseColor(it) } // Summer
            2 -> listOf("#E65100", "#F57F17", "#BF360C").map { Color.parseColor(it) } // Autumn
            3 -> listOf("#B0BEC5", "#78909C", "#ECEFF1").map { Color.parseColor(it) } // Winter
            else -> listOf("#43A047", "#66BB6A", "#81C784").map { Color.parseColor(it) } // Spring
        }
    }

    private fun blendColors(c1: Int, c2: Int, r: Float): Int {
        val ir = 1f - r
        val a = (Color.alpha(c1) * ir + Color.alpha(c2) * r).toInt()
        val re = (Color.red(c1) * ir + Color.red(c2) * r).toInt()
        val g = (Color.green(c1) * ir + Color.green(c2) * r).toInt()
        val b = (Color.blue(c1) * ir + Color.blue(c2) * r).toInt()
        return Color.argb(a, re, g, b)
    }
}
