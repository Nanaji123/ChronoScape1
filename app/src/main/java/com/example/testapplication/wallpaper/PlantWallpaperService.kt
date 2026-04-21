package com.example.testapplication.wallpaper

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.testapplication.SessionManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Live Wallpaper – World Tree ecosystem. Features: day/night sky cycle, sun arc with rays, crescent
 * moon, twinkling stars, drifting clouds with rain (when growth < 60), screen-edge climbing vines,
 * ground-crawling vines, dense curved vegetation (grass / weeds / big fern plants / flowers),
 * tapered filled trunk, recursive branching with wind.
 */
class PlantWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = PlantEngine()

    inner class PlantEngine : Engine() {

        private val handler = Handler(Looper.getMainLooper())
        private var isVisible = false

        // ── Timers ────────────────────────────────────────────────────────
        private var windTime = 0f // fast  – wind animation
        private var dayNightTime = 0f // slow  – sun/moon arc
        private var cloudTime = 0f // medium – cloud drift
        private var rainTime = 0f // fast  – rain drop offset

        // ── Sky / weather paints ──────────────────────────────────────────
        private val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val sunCorePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FFD600")
                    style = Paint.Style.FILL
                }
        private val sunGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val sunRayPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FFE57F")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = 3f
                }
        private val moonPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#E8E8C0")
                    style = Paint.Style.FILL
                }
        private val moonShadowPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val starPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
        private val cloudPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#B0BEC5")
                    style = Paint.Style.FILL
                }
        private val rainCloudPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#546E7A")
                    style = Paint.Style.FILL
                }
        private val rainPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#64B5F6")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeWidth = 1.8f
                }

        // ── Ground paints ─────────────────────────────────────────────────
        private val groundFillPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A0E07") }
        private val groundGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        // ── Tree paints ───────────────────────────────────────────────────
        private val trunkFillPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#3E2006")
                    style = Paint.Style.FILL
                }
        private val trunkEdgePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#5C3010")
                    style = Paint.Style.STROKE
                    strokeJoin = Paint.Join.ROUND
                }
        private val barkPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#2A1505")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
        private val branchPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#5C3010")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
        private val twigPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#7A4520")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
        private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

        // ── Vegetation / vine paints ──────────────────────────────────────
        private val vinePaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
        private val grassPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
        private val weedPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                }
        private val flowerPetalPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
        private val flowerCenterPaint =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#FDD835")
                    style = Paint.Style.FILL
                }

        private val sessionManager by lazy { SessionManager(applicationContext) }

        // ── Pre-seeded random positions (stable per session) ──────────────
        private val grassSeeds = (0..140).map { Pair(Random.nextFloat(), Random.nextFloat()) }
        private val weedSeeds = (0..70).map { Pair(Random.nextFloat(), Random.nextFloat()) }
        private val bigPlantSeeds = (0..15).map { Pair(Random.nextFloat(), Random.nextFloat()) }
        private val flowerSeeds =
                (0..40).map { Triple(Random.nextFloat(), Random.nextFloat(), Random.nextFloat()) }
        private val vineSeeds = (0..7).map { Random.nextFloat() }
        private val starSeeds = (0..50).map { Pair(Random.nextFloat(), Random.nextFloat()) }
        private val rainDropSeeds = (0..80).map { Pair(Random.nextFloat(), Random.nextFloat()) }

        private val flowerColors =
                listOf(
                        "#FF80AB",
                        "#FF4081",
                        "#E040FB",
                        "#FFFFFF",
                        "#FF6D00",
                        "#FFD740",
                        "#40C4FF",
                        "#69F0AE",
                        "#F48FB1",
                        "#CE93D8"
                )
        private val leafColors =
                listOf(
                        Color.parseColor("#1B5E20"),
                        Color.parseColor("#2E7D32"),
                        Color.parseColor("#388E3C"),
                        Color.parseColor("#43A047"),
                        Color.parseColor("#66BB6A"),
                        Color.parseColor("#1A4A10")
                )

        // Cloud config – 3 independent clouds
        private val cloudOffsets = listOf(0.0f, 0.40f, 0.74f)
        private val cloudWidths = listOf(110f, 140f, 80f)
        private val cloudYFracs = listOf(0.07f, 0.13f, 0.05f)

        // ── Runnable ──────────────────────────────────────────────────────
        private val drawRunnable =
                object : Runnable {
                    override fun run() {
                        if (!isVisible) return
                        windTime += 0.035f
                        dayNightTime += 0.0018f // full day cycle ≈ 60 min real time
                        cloudTime += 0.006f
                        rainTime += 0.08f
                        val g = sessionManager.getGrowth()
                        if (g < 2000f) sessionManager.saveGrowth(g + 1.5f)
                        draw()
                        handler.postDelayed(this, 33)
                    }
                }

        override fun onVisibilityChanged(visible: Boolean) {
            isVisible = visible
            if (visible) handler.post(drawRunnable) else handler.removeCallbacks(drawRunnable)
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            handler.removeCallbacks(drawRunnable)
        }

        // ── Master draw ───────────────────────────────────────────────────
        private fun draw() {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas() ?: return
                val g = sessionManager.getGrowth()
                val w = canvas.width.toFloat()
                val h = canvas.height.toFloat()
                val cx = w / 2f
                // Ground at very bottom – everything grows from the floor
                val groundY = h * 0.93f

                // Day/night phase: sin > 0 = day, sin < 0 = night
                val dayPhase = sin(dayNightTime.toDouble()).toFloat()
                // Season: every 300 growth = one season cycle (Spring→Summer→Autumn→Winter)
                val seasonProgress = (g / 300f) % 4f

                drawSky(canvas, w, h, groundY, dayPhase, seasonProgress)
                if (dayPhase < 0f) drawStars(canvas, w, h * 0.70f)
                drawSunMoon(canvas, cx, w, h, dayPhase)
                drawClouds(canvas, cx, w, h, groundY, g)
                drawGround(canvas, w, h, groundY, seasonProgress)
                drawGroundVegetation(canvas, w, groundY, h, g, seasonProgress)
                drawTree(canvas, cx, groundY, w, h, g, seasonProgress)
                drawGroundVines(canvas, cx, w, groundY, g)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas)
            }
        }

        // Season palette helpers
        // season: 0=Spring 1=Summer 2=Autumn 3=Winter
        private fun seasonLeafColors(season: Int): List<Int> =
                when (season) {
                    1 ->
                            listOf( // Summer – deep greens
                                    Color.parseColor("#1B5E20"),
                                    Color.parseColor("#2E7D32"),
                                    Color.parseColor("#1A4A10"),
                                    Color.parseColor("#33691E"),
                                    Color.parseColor("#388E3C"),
                                    Color.parseColor("#1B5E20")
                            )
                    2 ->
                            listOf( // Autumn – orange/red/gold
                                    Color.parseColor("#E65100"),
                                    Color.parseColor("#BF360C"),
                                    Color.parseColor("#F57F17"),
                                    Color.parseColor("#FF6F00"),
                                    Color.parseColor("#C62828"),
                                    Color.parseColor("#E64A19")
                            )
                    3 ->
                            listOf( // Winter – grey-white, bare
                                    Color.parseColor("#B0BEC5"),
                                    Color.parseColor("#90A4AE"),
                                    Color.parseColor("#CFD8DC"),
                                    Color.parseColor("#78909C"),
                                    Color.parseColor("#ECEFF1"),
                                    Color.parseColor("#B0BEC5")
                            )
                    else ->
                            listOf( // Spring – fresh greens + pink tinge
                                    Color.parseColor("#388E3C"),
                                    Color.parseColor("#43A047"),
                                    Color.parseColor("#66BB6A"),
                                    Color.parseColor("#81C784"),
                                    Color.parseColor("#A5D6A7"),
                                    Color.parseColor("#2E7D32")
                            )
                }

        private fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
            val ir = 1.0f - ratio
            val a = (Color.alpha(color1) * ir + Color.alpha(color2) * ratio).toInt()
            val r = (Color.red(color1) * ir + Color.red(color2) * ratio).toInt()
            val g = (Color.green(color1) * ir + Color.green(color2) * ratio).toInt()
            val b = (Color.blue(color1) * ir + Color.blue(color2) * ratio).toInt()
            return Color.argb(a, r, g, b)
        }

        private fun getSmoothSeasonLeafColors(seasonProgress: Float): List<Int> {
            val s1 = seasonProgress.toInt() % 4
            val s2 = (s1 + 1) % 4
            val ratio = seasonProgress % 1f
            val p1 = seasonLeafColors(s1)
            val p2 = seasonLeafColors(s2)
            return p1.mapIndexed { index, c1 ->
                val c2 = p2.getOrNull(index) ?: p2.first()
                blendColors(c1, c2, ratio)
            }
        }

        // ── Sky ───────────────────────────────────────────────────────────
        private fun drawSky(
                canvas: Canvas,
                w: Float,
                h: Float,
                groundY: Float,
                dayPhase: Float,
                seasonProgress: Float
        ) {
            val t = (dayPhase + 1f) / 2f // 0=night → 1=full day
            val s1 = seasonProgress.toInt() % 4
            val s2 = (s1 + 1) % 4
            val sRatio = seasonProgress % 1f

            fun getHorizColor(s: Int): Triple<Float, Float, Float> =
                    when (s) {
                        2 -> Triple(lerp(28f, 55f, t), lerp(18f, 48f, t), lerp(12f, 30f, t))
                        3 -> Triple(lerp(10f, 18f, t), lerp(20f, 55f, t), lerp(38f, 72f, t))
                        else -> Triple(lerp(14f, 28f, t), lerp(22f, 75f, t), lerp(30f, 45f, t))
                    }

            val c1 = getHorizColor(s1)
            val c2 = getHorizColor(s2)
            val horizR = (c1.first * (1f - sRatio) + c2.first * sRatio).toInt()
            val horizG = (c1.second * (1f - sRatio) + c2.second * sRatio).toInt()
            val horizB = (c1.third * (1f - sRatio) + c2.third * sRatio).toInt()
            skyPaint.shader =
                    LinearGradient(
                            0f,
                            0f,
                            0f,
                            groundY,
                            intArrayOf(
                                    Color.rgb(
                                            lerp(6f, 20f, t).toInt(),
                                            lerp(8f, 35f, t).toInt(),
                                            lerp(26f, 60f, t).toInt()
                                    ),
                                    Color.rgb(horizR, horizG, horizB)
                            ),
                            null,
                            Shader.TileMode.CLAMP
                    )
            canvas.drawRect(0f, 0f, w, groundY, skyPaint)
        }

        // ── Stars (night only) ────────────────────────────────────────────
        private fun drawStars(canvas: Canvas, w: Float, h: Float) {
            repeat(starSeeds.size) { i ->
                val (sx, sy) = starSeeds[i]
                val twinkle = (sin(windTime.toDouble() * 1.8 + i).toFloat() + 1f) / 2f
                starPaint.alpha = (80 + twinkle * 175f).toInt().coerceIn(0, 255)
                canvas.drawCircle(sx * w, sy * h, 0.8f + (i % 3) * 0.6f, starPaint)
            }
        }

        // ── Sun & Moon ────────────────────────────────────────────────────
        private fun drawSunMoon(canvas: Canvas, cx: Float, w: Float, h: Float, dayPhase: Float) {
            // Horizontal arc driven by cos(dayNightTime)
            val skyX = cos(dayNightTime.toDouble()).toFloat()
            val objX = cx + skyX * w * 0.40f
            val objY = h * 0.09f - abs(skyX) * h * 0.025f // gentle vertical arc

            if (dayPhase >= 0f) {
                // ──── Sun ────
                val alpha = (dayPhase * 255f).toInt().coerceIn(40, 255)

                // Warm glow halo
                sunGlowPaint.shader =
                        RadialGradient(
                                objX,
                                objY,
                                65f,
                                intArrayOf(Color.parseColor("#FFFDE7"), Color.TRANSPARENT),
                                null,
                                Shader.TileMode.CLAMP
                        )
                sunGlowPaint.alpha = (alpha * 0.45f).toInt()
                canvas.drawCircle(objX, objY, 65f, sunGlowPaint)

                // Core disc
                sunCorePaint.alpha = alpha
                canvas.drawCircle(objX, objY, 24f, sunCorePaint)

                // Rotating rays (8 rays)
                sunRayPaint.alpha = alpha
                repeat(8) { i ->
                    val ang = (i.toFloat() / 8f) * Math.PI.toFloat() * 2f + windTime * 0.25f
                    val inner = 30f
                    val outer = 42f + sin(windTime.toDouble() * 2.2 + i).toFloat() * 5f
                    canvas.drawLine(
                            objX + cos(ang) * inner,
                            objY + sin(ang) * inner,
                            objX + cos(ang) * outer,
                            objY + sin(ang) * outer,
                            sunRayPaint
                    )
                }
            } else {
                // ──── Moon (crescent) ────
                val alpha = (-dayPhase * 255f).toInt().coerceIn(40, 255)
                moonPaint.alpha = alpha
                // Crescent: full disc then overlay with background-coloured circle offset
                val nightBg = Color.rgb(6, 8, 26)
                moonShadowPaint.color = nightBg
                moonShadowPaint.alpha = alpha
                canvas.drawCircle(objX, objY, 22f, moonPaint)
                canvas.drawCircle(objX + 9f, objY - 4f, 19f, moonShadowPaint)
            }
        }

        // ── Clouds + Rain ─────────────────────────────────────────────────
        private fun drawClouds(
                canvas: Canvas,
                cx: Float,
                w: Float,
                h: Float,
                groundY: Float,
                growth: Float
        ) {
            val isRaining = growth < 60f
            val cPaint = if (isRaining) rainCloudPaint else cloudPaint

            repeat(3) { ci ->
                val cw = cloudWidths[ci]
                val rawX =
                        w + 180f - ((cloudTime * 45f + cloudOffsets[ci] * (w + 360f)) % (w + 360f))
                val cloudX = rawX
                val cloudY = h * cloudYFracs[ci]
                if (cloudX < -cw || cloudX > w + cw) return@repeat

                drawCloudShape(canvas, cloudX, cloudY, cw, cPaint)

                if (isRaining && cloudX > -cw && cloudX < w + cw) {
                    val rainAlpha = ((1f - growth / 60f) * 210f).toInt().coerceAtMost(210)
                    rainPaint.alpha = rainAlpha
                    val dropCount = 14
                    repeat(dropCount) { di ->
                        val (dx, dy) = rainDropSeeds[di + ci * dropCount]
                        val dropX = cloudX - cw * 0.45f + dx * cw * 0.9f
                        val frac = (rainTime * 0.65f + dy + di * 0.14f) % 1f
                        val dropTop = cloudY + 20f + frac * (groundY - cloudY - 24f)
                        val dropBot = dropTop + 16f
                        if (dropBot < groundY)
                                canvas.drawLine(dropX, dropTop, dropX, dropBot, rainPaint)
                    }
                }
            }
        }

        private fun drawCloudShape(canvas: Canvas, cx: Float, cy: Float, cw: Float, paint: Paint) {
            val rx = cw / 2f
            val ry = cw * 0.22f
            val rect = RectF()
            // Central body
            rect.set(cx - rx, cy - ry, cx + rx, cy + ry * 1.2f)
            canvas.drawOval(rect, paint)
            // Top bumps
            rect.set(cx - rx * 0.75f, cy - ry * 1.8f, cx + rx * 0.1f, cy + ry * 0.2f)
            canvas.drawOval(rect, paint)
            rect.set(cx - rx * 0.15f, cy - ry * 1.6f, cx + rx * 0.75f, cy + ry * 0.1f)
            canvas.drawOval(rect, paint)
            // Side puffs
            rect.set(cx - rx * 1.15f, cy - ry * 0.5f, cx - rx * 0.35f, cy + ry * 0.9f)
            canvas.drawOval(rect, paint)
            rect.set(cx + rx * 0.35f, cy - ry * 0.4f, cx + rx * 1.15f, cy + ry * 0.9f)
            canvas.drawOval(rect, paint)
        }

        // ── Ground ────────────────────────────────────────────────────────
        private fun drawGround(
                canvas: Canvas,
                w: Float,
                h: Float,
                groundY: Float,
                seasonProgress: Float
        ) {
            val s1 = seasonProgress.toInt() % 4
            val s2 = (s1 + 1) % 4
            val sRatio = seasonProgress % 1f

            val groundColor1 =
                    if (s1 == 3) Color.parseColor("#2A2A3A") else Color.parseColor("#1A0E07")
            val groundColor2 =
                    if (s2 == 3) Color.parseColor("#2A2A3A") else Color.parseColor("#1A0E07")
            groundFillPaint.color = blendColors(groundColor1, groundColor2, sRatio)

            canvas.drawRect(0f, groundY, w, h, groundFillPaint)

            fun getGlowColor(s: Int): Int =
                    when (s) {
                        2 -> Color.parseColor("#8D6E40") // Autumn – earthy
                        3 -> Color.parseColor("#78909C") // Winter – grey
                        else -> Color.parseColor("#5C8A3C") // Spring/Summer – green
                    }

            val glowColor = blendColors(getGlowColor(s1), getGlowColor(s2), sRatio)
            groundGlowPaint.shader =
                    LinearGradient(
                            0f,
                            groundY,
                            0f,
                            groundY + 55f,
                            intArrayOf(glowColor, Color.TRANSPARENT),
                            null,
                            Shader.TileMode.CLAMP
                    )
            canvas.drawRect(0f, groundY, w, groundY + 55f, groundGlowPaint)
        }

        // Screen-edge vines removed per user request.

        // ── Ground vegetation ──────────────────────────────────────────────
        private fun drawGroundVegetation(
                canvas: Canvas,
                w: Float,
                groundY: Float,
                h: Float,
                growth: Float,
                seasonProgress: Float
        ) {
            val seasonInt = seasonProgress.toInt() % 4
            val sRatio = seasonProgress % 1f
            val treeH = (growth / 2000f * h * 0.55f).coerceAtMost(h * 0.55f)
            val vegH = (treeH * 0.22f).coerceIn(30f, 115f)
            // Winter: no vegetation (everything is bare/snow)
            // LERP the appearance of snow vs grass
            if (seasonInt == 3 || (seasonInt == 2 && sRatio > 0.8f)) {
                val winterRatio = if (seasonInt == 3) 1f else (sRatio - 0.8f) / 0.2f
                // Just draw a light snow dusting on ground
                val snowPaint =
                        Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            color = Color.parseColor("#ECEFF1")
                            style = Paint.Style.FILL
                        }
                snowPaint.alpha = (winterRatio * 255).toInt()
                repeat(40) { i ->
                    val sx = grassSeeds[i].first * w
                    val sy2 = groundY - (4f + grassSeeds[i].second * 12f)
                    canvas.drawCircle(sx, sy2, 3f + grassSeeds[i].second * 4f, snowPaint)
                }
                if (winterRatio > 0.95f) return
            }

            // ── Big fern/bush plants (background) ──
            repeat(bigPlantSeeds.size) { i ->
                val (sx, sy) = bigPlantSeeds[i]
                val x = sx * w
                val bH = vegH * (1.3f + sy * 0.7f) // taller than regular weeds
                val appear = ((growth - 100f - i * 30f) / 150f).coerceIn(0f, 1f)
                if (appear <= 0f) return@repeat

                val curH = bH * appear
                val w1 = sin(windTime.toDouble() * 0.8 + x / 50.0).toFloat() * appear * 10f
                val w2 = sin(windTime.toDouble() * 1.3 + x / 30.0 + i).toFloat() * appear * 5f

                weedPaint.color =
                        if (i % 2 == 0) Color.parseColor("#1B5E20") else Color.parseColor("#2E7D32")
                weedPaint.strokeWidth = 3f + sy * 2f

                // Fan of fronds (5 directions)
                listOf(-0.65f, -0.28f, 0f, 0.28f, 0.65f).forEach { ang ->
                    val len = curH * (0.95f - abs(ang) * 0.22f)
                    val tipW = (w1 + w2) * (1f + abs(ang))
                    val tipX = x + sin(ang) * len * 0.85f + tipW
                    val tipY = groundY - len * 0.90f
                    val mX = x + sin(ang) * len * 0.5f + tipW * 0.4f
                    val mY = groundY - len * 0.55f
                    val frond = Path()
                    frond.moveTo(x, groundY)
                    frond.cubicTo(mX, mY, (mX + tipX) / 2f, (mY + tipY) / 2f, tipX, tipY)
                    canvas.drawPath(frond, weedPaint)
                }
                if (appear > 0.5f) {
                    leafPaint.color = Color.parseColor("#43A047")
                    leafPaint.alpha = ((appear - 0.5f) / 0.5f * 200f).toInt()
                    canvas.drawCircle(
                            x + w1 * 0.7f,
                            groundY - curH * 0.93f,
                            curH * 0.09f,
                            leafPaint
                    )
                }
            }

            // ── Tall weeds ──
            repeat(weedSeeds.size) { i ->
                val (sx, sy) = weedSeeds[i]
                val x = sx * w
                val maxH = vegH * (0.80f + sy * 0.55f)
                val appear = ((growth - 60f - i * 12f) / 100f).coerceIn(0f, 1f)
                if (appear <= 0f) return@repeat

                val curH = maxH * appear
                val w1 = sin(windTime.toDouble() * 1.0 + x / 38.0).toFloat() * appear * 9f
                val w2 = sin(windTime.toDouble() * 1.4 + x / 22.0 + i * 0.5).toFloat() * appear * 4f
                val tipX = x + w1 + w2

                weedPaint.color =
                        when (i % 3) {
                            0 -> Color.parseColor("#1B5E20")
                            1 -> Color.parseColor("#2E7D32")
                            else -> Color.parseColor("#388E3C")
                        }
                weedPaint.strokeWidth = 2f + sy * 1.8f

                // Stalk curve
                val stalk = Path()
                stalk.moveTo(x, groundY)
                stalk.quadTo(x + (tipX - x) * 0.4f, groundY - curH * 0.55f, tipX, groundY - curH)
                canvas.drawPath(stalk, weedPaint)

                // Side blades (independent wind phases)
                val w3 = sin(windTime.toDouble() * 1.1 + x / 28.0 + 1.2).toFloat() * appear * 7f
                val b1 = Path()
                b1.moveTo(x, groundY)
                b1.quadTo(
                        x - 6f + w3 * 0.4f,
                        groundY - curH * 0.50f,
                        x - 10f + w3,
                        groundY - curH * 0.82f
                )
                canvas.drawPath(b1, weedPaint)

                val w4 = sin(windTime.toDouble() * 0.9 + x / 32.0 + 2.4).toFloat() * appear * 7f
                val b2 = Path()
                b2.moveTo(x, groundY)
                b2.quadTo(
                        x + 6f + w4 * 0.4f,
                        groundY - curH * 0.48f,
                        x + 10f + w4,
                        groundY - curH * 0.78f
                )
                canvas.drawPath(b2, weedPaint)
            }

            // ── Short dense grass ──
            repeat(grassSeeds.size) { i ->
                val (sx, sy) = grassSeeds[i]
                val x = sx * w
                val maxH = vegH * (0.45f + sy * 0.32f)
                val appear = ((growth - 40f - i * 6f) / 70f).coerceIn(0f, 1f)
                if (appear <= 0f) return@repeat

                val curH = maxH * appear
                val w1 = sin(windTime.toDouble() * 1.2 + x / 25.0).toFloat() * appear * 10f
                val w2 = sin(windTime.toDouble() * 0.8 + x / 18.0 + i).toFloat() * appear * 4f

                grassPaint.color =
                        if (i % 2 == 0) Color.parseColor("#43A047") else Color.parseColor("#2E7D32")
                grassPaint.strokeWidth = 1.6f + sy * 1.2f

                repeat(3) { b ->
                    val bX = (b - 1) * 4f
                    val bW = w1 + w2 * (b - 1) * 0.5f
                    val blade = Path()
                    blade.moveTo(x + bX, groundY)
                    blade.quadTo(
                            x + bX + bW * 0.45f,
                            groundY - curH * 0.52f,
                            x + bX + bW,
                            groundY - curH * (0.88f + b * 0.05f)
                    )
                    canvas.drawPath(blade, grassPaint)
                }
            }

            // ── Flowering stems ──
            repeat(flowerSeeds.size) { i ->
                val (sx, sy, sz) = flowerSeeds[i]
                val x = sx * w
                val stemH = vegH * (0.72f + sy * 0.38f)
                val appear = ((growth - 120f - i * 20f) / 90f).coerceIn(0f, 1f)
                if (appear <= 0f) return@repeat

                val curStem = stemH * appear
                val w1 = sin(windTime.toDouble() * 1.0 + x / 42.0).toFloat() * appear * 8f
                val w2 = sin(windTime.toDouble() * 1.5 + x / 20.0 + i).toFloat() * appear * 3f
                val tipX = x + w1 + w2
                val tipY = groundY - curStem

                grassPaint.color = Color.parseColor("#388E3C")
                grassPaint.strokeWidth = 1.8f
                val stem = Path()
                stem.moveTo(x, groundY)
                stem.quadTo(x + (tipX - x) * 0.5f, groundY - curStem * 0.55f, tipX, tipY)
                canvas.drawPath(stem, grassPaint)

                val col =
                        Color.parseColor(flowerColors[(i + (sz * 10).toInt()) % flowerColors.size])
                drawFlower(canvas, tipX, tipY, (5f + sy * 5f) * appear, col)
            }
        }

        // ── Tree ──────────────────────────────────────────────────────────
        private fun drawTree(
                canvas: Canvas,
                cx: Float,
                groundY: Float,
                w: Float,
                h: Float,
                growth: Float,
                seasonProgress: Float
        ) {
            val maxTH = h * 0.55f
            val trunkH = (growth / 2000f * maxTH).coerceAtMost(maxTH)
            if (trunkH < 4f) return

            val baseHalf = 28f + (growth / 2000f * 75f).coerceAtMost(75f)
            // topHalf is now 38% of base so the trunk looks like a proper column,
            // not a needle or triangle (was 12% which looked like a spike)
            val topHalf = baseHalf * 0.38f
            val windSway = sin(windTime.toDouble()).toFloat() * 7f // slightly more sway
            val trunkTopX = cx + windSway
            val trunkTopY = groundY - trunkH

            val seasonLeafs = getSmoothSeasonLeafColors(seasonProgress)

            drawTrunkFilled(
                    canvas,
                    cx,
                    groundY,
                    trunkTopX,
                    trunkTopY,
                    baseHalf,
                    topHalf,
                    windSway,
                    trunkH
            )
            drawBarkTexture(
                    canvas,
                    cx,
                    groundY,
                    trunkTopX,
                    trunkTopY,
                    baseHalf,
                    topHalf,
                    trunkH,
                    windSway
            )

            val branchAlpha = ((growth - 200f) / 150f * 255f).toInt().coerceIn(0, 255)
            if (branchAlpha <= 0) return
            branchPaint.alpha = branchAlpha
            twigPaint.alpha = branchAlpha

            // DENSE BRANCHING: Start at 0.18f, and many points towards the top to fill space.
            listOf(0.18f, 0.30f, 0.42f, 0.54f, 0.66f, 0.78f, 0.88f, 0.93f, 0.96f, 0.99f)
                    .forEachIndexed { idx, frac ->
                        val bY = groundY - trunkH * frac
                        val trunkHalfAtFrac = lerp(baseHalf, topHalf, frac)
                        val bXCenter = lerp(cx, trunkTopX, frac)
                        val bXLeft = bXCenter - trunkHalfAtFrac
                        val bXRight = bXCenter + trunkHalfAtFrac
                        // Longer branches for bushier look (0.42f instead of 0.32f)
                        val bLen = trunkH * (0.42f - idx * 0.02f).coerceAtLeast(0.15f)
                        val bThick = trunkHalfAtFrac * 0.48f
                        val gOff = idx * 160f
                        val fade = ((growth - 200f - gOff) / 180f).coerceIn(0f, 1f)
                        if (fade <= 0f) return@forEachIndexed
                        val maxD = if (idx >= 6) 4 else 3

                        // Left branch: starts from left trunk edge
                        drawBR(
                                canvas,
                                bXLeft,
                                bY,
                                -(Math.PI * 0.63f).toFloat(),
                                bLen * fade,
                                bThick,
                                0,
                                maxD,
                                growth,
                                windSway,
                                gOff,
                                idx,
                                seasonLeafs
                        )
                        // Right branch: starts from right trunk edge
                        drawBR(
                                canvas,
                                bXRight,
                                bY,
                                -(Math.PI * 0.37f).toFloat(),
                                bLen * fade,
                                bThick,
                                0,
                                maxD,
                                growth,
                                windSway,
                                gOff,
                                idx,
                                seasonLeafs
                        )
                        // Upward centre branch from mid-trunk up
                        if (idx >= 2)
                                drawBR(
                                        canvas,
                                        bXCenter,
                                        bY,
                                        -(Math.PI * 0.50f).toFloat(),
                                        bLen * 0.70f * fade,
                                        bThick * 0.65f,
                                        0,
                                        (maxD - 1).coerceAtLeast(2),
                                        growth,
                                        windSway,
                                        gOff + 60f,
                                        idx,
                                        seasonLeafs
                                )
                        // Extra wide-angle branches at base for bushy lower body
                        if (idx < 2) {
                            drawBR(
                                    canvas,
                                    bXLeft,
                                    bY,
                                    -(Math.PI * 0.75f).toFloat(),
                                    bLen * 0.80f * fade,
                                    bThick * 0.75f,
                                    0,
                                    3,
                                    growth,
                                    windSway,
                                    gOff + 30f,
                                    idx,
                                    seasonLeafs
                            )
                            drawBR(
                                    canvas,
                                    bXRight,
                                    bY,
                                    -(Math.PI * 0.25f).toFloat(),
                                    bLen * 0.80f * fade,
                                    bThick * 0.75f,
                                    0,
                                    3,
                                    growth,
                                    windSway,
                                    gOff + 30f,
                                    idx,
                                    seasonLeafs
                            )
                        }
                    }

            // ── TOP CROWN: big leaf ball + fanned tip branches ──────────────
            // Appears as soon as any branches are visible (growth > 200)
            // ── TOP CROWN: Extreme leaf density ───────────────────────────
            val crownAlpha = ((growth - 200f) / 200f * 255f).toInt().coerceIn(0, 255)
            if (crownAlpha > 0) {
                // Larger crown radius
                val crownRadius = topHalf * (2.2f + (growth / 2000f) * 3.0f)
                val crownSway = sin(windTime.toDouble() * 0.9).toFloat() * windSway * 0.6f
                val crownX = trunkTopX + crownSway
                val crownY = trunkTopY

                // 8 tip branches fanning out in all directions
                val tipLen = trunkH * 0.18f * ((growth - 200f) / 1800f).coerceIn(0f, 1f)
                val tipThick = topHalf * 0.4f
                listOf(-0.85f, -0.72f, -0.62f, -0.52f, -0.45f, -0.35f, -0.22f, -0.15f).forEach { ang
                    ->
                    val tipAngle = -(Math.PI * ang).toFloat()
                    drawBR(
                            canvas,
                            crownX,
                            crownY,
                            tipAngle,
                            tipLen,
                            tipThick,
                            0,
                            3,
                            growth,
                            windSway,
                            300f,
                            7,
                            seasonLeafs
                    )
                }

                // Multiple layers of foliage to completely hide the trunk tip
                repeat(6) { i ->
                    val angleOffset = i * (Math.PI * 2 / 6)
                    val dist = crownRadius * 0.35f
                    val bx = crownX + cos(angleOffset + windTime * 0.3).toFloat() * dist
                    val by = crownY + sin(angleOffset + windTime * 0.3).toFloat() * dist - i * 10f
                    drawLeafCluster(
                            canvas,
                            bx,
                            by,
                            crownRadius * (0.8f + (i % 2) * 0.2f),
                            crownAlpha,
                            2,
                            crownSway,
                            seasonLeafs
                    )
                }
            }
        }

        private fun drawTrunkFilled(
                canvas: Canvas,
                cx: Float,
                groundY: Float,
                topX: Float,
                topY: Float,
                baseHalf: Float,
                topHalf: Float,
                windSway: Float,
                trunkH: Float
        ) {
            // LEFT SIDE: bulge outward to the left at mid-trunk
            // RIGHT SIDE: bulge outward to the right at mid-trunk
            // This gives a natural barrel/column shape instead of a triangle.
            val midY = groundY - trunkH * 0.45f

            // Left edge control point: slightly outside the left edge of the trunk
            val leftCtrlX = cx - baseHalf * 1.08f + windSway * 0.2f
            val leftCtrlY = midY
            // Right edge control point: slightly outside the right edge
            val rightCtrlX = cx + baseHalf * 1.08f + windSway * 0.2f
            val rightCtrlY = midY

            val path = Path()
            path.moveTo(cx - baseHalf, groundY) // bottom-left root
            path.quadTo(leftCtrlX, leftCtrlY, topX - topHalf, topY) // left side curves outward
            path.lineTo(topX + topHalf, topY) // top cap
            path.quadTo(rightCtrlX, rightCtrlY, cx + baseHalf, groundY) // right side curves outward
            path.close()

            canvas.drawPath(path, trunkFillPaint)
            trunkEdgePaint.strokeWidth = 4f
            canvas.drawPath(path, trunkEdgePaint)
        }

        private fun drawBarkTexture(
                canvas: Canvas,
                cx: Float,
                groundY: Float,
                topX: Float,
                topY: Float,
                baseHalf: Float,
                topHalf: Float,
                trunkH: Float,
                windSway: Float
        ) {
            barkPaint.strokeWidth = 1.5f
            barkPaint.alpha = 80
            for (i in 0..7) {
                val t = i.toFloat() / 7f
                val bx = lerp(cx - baseHalf * 0.85f, cx + baseHalf * 0.85f, t)
                val tx = lerp(topX - topHalf * 0.85f, topX + topHalf * 0.85f, t)
                val midX = lerp(bx, tx, 0.5f) + sin(i * 1.7).toFloat() * 8f
                val p = Path()
                p.moveTo(bx, groundY - 5f)
                p.quadTo(midX, groundY - trunkH * 0.5f, tx, topY + 5f)
                canvas.drawPath(p, barkPaint)
            }
            barkPaint.alpha = 255
        }

        // Recursive branch – now takes seasonLeafColors list
        private fun drawBR(
                canvas: Canvas,
                x: Float,
                y: Float,
                angle: Float,
                length: Float,
                thickness: Float,
                depth: Int,
                maxDepth: Int,
                growth: Float,
                windSway: Float,
                growthOffset: Float,
                spawnIdx: Int,
                seasonLeafs: List<Int>
        ) {
            if (depth > maxDepth || length < 5f) return
            val dGrowth =
                    ((growth - (250f + spawnIdx * 200f + depth * 220f) - growthOffset) / 140f)
                            .coerceIn(0f, 1f)
            if (dGrowth <= 0f) return

            // Per-branch phase uses x position + depth + spawnIdx for natural variety
            val phase = windTime.toDouble() + x * 0.008 + depth * 1.1 + spawnIdx * 0.7
            val swayAmp = windSway * ((depth + 1).toFloat() / (maxDepth + 1).toFloat()) * 2.0f
            val sway = sin(phase).toFloat() * swayAmp

            val aLen = length * dGrowth
            val endX = x + cos(angle + sway / 28f) * aLen
            val endY = y + sin(angle + sway / 28f) * aLen

            val p =
                    if (depth <= 2) branchPaint.apply { strokeWidth = thickness.coerceAtLeast(3f) }
                    else twigPaint.apply { strokeWidth = thickness.coerceAtLeast(2f) }
            p.alpha = 255
            canvas.drawLine(x, y, endX, endY, p)

            if (dGrowth > 0.10f) {
                val la = ((dGrowth - 0.10f) / 0.90f * 255f).toInt().coerceIn(0, 255)
                val midX = x + cos(angle + sway / 28f) * aLen * 0.55f
                val midY2 = y + sin(angle + sway / 28f) * aLen * 0.55f
                // ALWAYS draw at least two clusters per branch for extra density
                drawLeafCluster(
                        canvas,
                        midX,
                        midY2,
                        length * 0.28f,
                        la / 2,
                        depth,
                        sway,
                        seasonLeafs
                )
                drawLeafCluster(canvas, endX, endY, length * 0.48f, la, depth, sway, seasonLeafs)
            }

            if (depth < maxDepth) {
                val nL = length * 0.60f
                val nT = thickness * 0.55f
                val sp = if (depth == 0) 0.48f else 0.40f
                drawBR(
                        canvas,
                        endX,
                        endY,
                        angle - sp - sway / 55f,
                        nL,
                        nT,
                        depth + 1,
                        maxDepth,
                        growth,
                        windSway,
                        growthOffset + depth * 25f,
                        spawnIdx,
                        seasonLeafs
                )
                drawBR(
                        canvas,
                        endX,
                        endY,
                        angle + sp + sway / 55f,
                        nL,
                        nT,
                        depth + 1,
                        maxDepth,
                        growth,
                        windSway,
                        growthOffset + depth * 25f,
                        spawnIdx,
                        seasonLeafs
                )
                if (depth < 2)
                        drawBR(
                                canvas,
                                endX,
                                endY,
                                angle - 0.05f + sway / 80f,
                                nL * 0.80f,
                                nT * 0.75f,
                                depth + 1,
                                maxDepth,
                                growth,
                                windSway,
                                growthOffset + depth * 40f,
                                spawnIdx,
                                seasonLeafs
                        )
            }
        }

        // ── Leaf cluster ─────────────────────────────────────────────────
        // Now accepts season-specific leaf color palette.
        private fun drawLeafCluster(
                canvas: Canvas,
                cx: Float,
                cy: Float,
                radius: Float,
                alpha: Int,
                depth: Int,
                sway: Float,
                seasonLeafs: List<Int> = leafColors
        ) {
            // Outer ring: many leaves spread around the tip
            val outerCount = (12 - depth * 2).coerceAtLeast(6)
            repeat(outerCount) { i ->
                val ang =
                        (i.toFloat() / outerCount) * Math.PI.toFloat() * 2f +
                                sway / 18f +
                                depth * 0.3f
                val r = radius * (0.45f + (i % 3) * 0.15f)
                val dist = radius * (0.5f + (i % 2) * 0.25f)
                drawLeafShape(
                        canvas,
                        cx + cos(ang) * dist,
                        cy + sin(ang) * dist,
                        r,
                        ang * 57.3f + 15f,
                        seasonLeafs[i % seasonLeafs.size],
                        alpha
                )
            }
            // Inner ring: smaller, denser leaves
            val innerCount = (8 - depth).coerceAtLeast(3)
            repeat(innerCount) { i ->
                val ang = (i.toFloat() / innerCount) * Math.PI.toFloat() * 2f + sway / 12f + 0.4f
                val r = radius * (0.30f + (i % 2) * 0.10f)
                val dist = radius * 0.28f
                drawLeafShape(
                        canvas,
                        cx + cos(ang) * dist,
                        cy + sin(ang) * dist,
                        r,
                        ang * 57.3f,
                        seasonLeafs[(i + 2) % seasonLeafs.size],
                        (alpha * 0.85f).toInt()
                )
            }
            // Dense centre blob (darkest shade of current season)
            leafPaint.color = seasonLeafs.last()
            leafPaint.alpha = alpha
            canvas.drawCircle(cx + sway * 0.4f, cy, radius * 0.32f, leafPaint)
        }

        // ── Ground-crawling vines ──────────────────────────────────────────
        private fun drawGroundVines(
                canvas: Canvas,
                cx: Float,
                w: Float,
                groundY: Float,
                growth: Float
        ) {
            val appear = ((growth - 100f) / 300f).coerceIn(0f, 1f)
            if (appear <= 0f) return
            repeat(5) { vi ->
                val seed = vineSeeds[vi % vineSeeds.size]
                val dir = if (vi % 2 == 0) -1f else 1f
                val isLeft = vi % 2 == 0
                val reachFrac = ((growth - 100f - vi * 80f) / 350f).coerceIn(0f, 1f)
                if (reachFrac <= 0f) return@repeat
                val reach = w * 0.40f * reachFrac

                val path = Path()
                path.moveTo(cx, groundY - 2f)
                for (s in 0..50) {
                    val t = s.toFloat() / 50
                    val x = cx + dir * reach * t
                    if ((dir > 0 && x > w) || (dir < 0 && x < 0f)) break
                    val und = sin(t * Math.PI.toFloat() * 4f + seed * 6f).toFloat() * 5f
                    val wo = sin(windTime.toDouble() + t * 8.0 + vi).toFloat() * 3f * reachFrac
                    path.lineTo(x, groundY - 2f - und - wo)
                }
                vinePaint.strokeWidth = (4f - vi * 0.5f).coerceAtLeast(1.5f)
                vinePaint.color =
                        when (vi % 3) {
                            0 -> Color.parseColor("#56A15E")
                            1 -> Color.parseColor("#388E3C")
                            else -> Color.parseColor("#81C784")
                        }
                canvas.drawPath(path, vinePaint)

                val leafCount = (reachFrac * 10).toInt()
                repeat(leafCount) { li ->
                    val t = (li + 1).toFloat() / (leafCount + 1)
                    val lx = cx + dir * reach * t
                    val und = sin(t * Math.PI.toFloat() * 4f + seed * 6f).toFloat() * 5f
                    val wo = sin(windTime.toDouble() + t * 8.0 + vi).toFloat() * 3f * reachFrac
                    val ly = groundY - 2f - und - wo
                    val ang = if (isLeft) -30f + (li % 2) * 60f else 30f - (li % 2) * 60f
                    drawLeafShape(
                            canvas,
                            lx,
                            ly,
                            9f + (li % 3) * 3f,
                            ang - 90f,
                            Color.parseColor("#66BB6A"),
                            (reachFrac * 200f).toInt().coerceAtMost(200)
                    )
                    if (li % 3 == 0)
                            drawFlower(
                                    canvas,
                                    lx,
                                    ly - 6f,
                                    4f,
                                    Color.parseColor(
                                            flowerColors[(vi * 3 + li) % flowerColors.size]
                                    )
                            )
                }
            }
        }

        // ── Leaf teardrop ─────────────────────────────────────────────────
        private fun drawLeafShape(
                canvas: Canvas,
                x: Float,
                y: Float,
                size: Float,
                angleDeg: Float,
                color: Int,
                alpha: Int = 255
        ) {
            leafPaint.color = color
            leafPaint.alpha = alpha
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angleDeg)
            val p = Path()
            p.moveTo(0f, -size)
            p.cubicTo(size * 0.55f, -size * 0.5f, size * 0.55f, size * 0.3f, 0f, size * 0.4f)
            p.cubicTo(-size * 0.55f, size * 0.3f, -size * 0.55f, -size * 0.5f, 0f, -size)
            canvas.drawPath(p, leafPaint)
            canvas.restore()
        }

        // ── Flower ────────────────────────────────────────────────────────
        private fun drawFlower(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int) {
            flowerPetalPaint.color = color
            repeat(5) { i ->
                val ang = (i.toFloat() / 5f) * Math.PI.toFloat() * 2f
                canvas.drawCircle(
                        x + cos(ang) * radius,
                        y + sin(ang) * radius,
                        radius * 0.72f,
                        flowerPetalPaint
                )
            }
            canvas.drawCircle(x, y, radius * 0.44f, flowerCenterPaint)
        }

        // ── Utility ───────────────────────────────────────────────────────
        private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    }
}
