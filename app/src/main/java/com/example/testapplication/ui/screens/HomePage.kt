package com.example.testapplication.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.ui.theme.TestApplicationTheme
import com.example.testapplication.viewmodel.HomeViewModel
import com.example.testapplication.wallpaper.PlantWallpaperService
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

// ── Colour tokens ──────────────────────────────────────────────────────────────
private val BG = androidx.compose.ui.graphics.Color(0xFF080F08)
private val Surface1 = androidx.compose.ui.graphics.Color(0xFF0E1E0E)
private val Surface2 = androidx.compose.ui.graphics.Color(0xFF162516)
private val Accent = androidx.compose.ui.graphics.Color(0xFF4CAF50)
private val AccentDim = androidx.compose.ui.graphics.Color(0xFF2E7D32)
private val TextPrimary = androidx.compose.ui.graphics.Color(0xFFE8F5E9)
private val TextSecondary = androidx.compose.ui.graphics.Color(0xFF81C784)
private val Border = androidx.compose.ui.graphics.Color(0xFF2E4A2E)

// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun HomePage(modifier: Modifier = Modifier, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    HomePageContent(
            modifier = modifier,
            welcomeMessage = viewModel.welcomeMessage,
            growthLevel = viewModel.growthLevel,
            growthSpeed = viewModel.growthSpeed,
            onGrowthSpeedChange = { viewModel.growthSpeed = it },
            onWaterPlant = { viewModel.waterPlant() },
            onFastGrow = { viewModel.fastGrow() },
            onReset = { viewModel.resetGrowth() },
            onSetWallpaper = { setWallpaper(context) }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun HomePageContent(
        modifier: Modifier = Modifier,
        welcomeMessage: String,
        growthLevel: Float,
        growthSpeed: Float,
        onGrowthSpeedChange: (Float) -> Unit,
        onWaterPlant: () -> Unit,
        onFastGrow: () -> Unit,
        onReset: () -> Unit,
        onSetWallpaper: () -> Unit
) {
    val progress by
            animateFloatAsState(
                    targetValue = growthLevel / 2000f,
                    animationSpec = tween(400),
                    label = "growth_progress"
            )

    Box(modifier = modifier.fillMaxSize().background(BG)) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 20.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────────
            Text(
                    text = "🌳  $welcomeMessage",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
            )
            Text(
                    text = "Live Wallpaper",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    letterSpacing = 3.sp
            )

            // ── Preview canvas ─────────────────────────────────────────────
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(24.dp))
                                    .border(1.5.dp, Border, RoundedCornerShape(24.dp))
            ) {
                TreePreviewCanvas(growthLevel = growthLevel)
                // Overlay label
                Box(
                        modifier =
                                Modifier.align(Alignment.TopStart)
                                        .padding(12.dp)
                                        .background(
                                                Surface1.copy(alpha = 0.75f),
                                                RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) { Text("PREVIEW", fontSize = 10.sp, color = TextSecondary, letterSpacing = 2.sp) }
            }

            // ── Growth progress card ───────────────────────────────────────
            Card2 {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Growth Level", color = TextSecondary, fontSize = 13.sp)
                        Text(
                                "${growthLevel.toInt()} / 2000",
                                color = Accent,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                        )
                    }
                    LinearProgressIndicator(
                            progress = { progress },
                            modifier =
                                    Modifier.fillMaxWidth()
                                            .height(10.dp)
                                            .clip(RoundedCornerShape(5.dp)),
                            color = Accent,
                            trackColor = Surface2
                    )
                    val stage =
                            when {
                                growthLevel < 300 -> "🌱 Seedling"
                                growthLevel < 700 -> "🌿 Sapling"
                                growthLevel < 1200 -> "🌲 Young Tree"
                                growthLevel < 1800 -> "🌳 Mature Tree"
                                else -> "✨ Ancient World Tree"
                            }
                    Text(
                            stage,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Growth speed control ───────────────────────────────────────
            Card2 {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Growth Speed", color = TextSecondary, fontSize = 13.sp)
                        Box(
                                modifier =
                                        Modifier.background(AccentDim, RoundedCornerShape(6.dp))
                                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                    "${String.format("%.1f", growthSpeed)}×",
                                    color = TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Slider(
                            value = growthSpeed,
                            onValueChange = onGrowthSpeedChange,
                            valueRange = 0.5f..3f,
                            steps = 4,
                            modifier = Modifier.fillMaxWidth(),
                            colors =
                                    SliderDefaults.colors(
                                            thumbColor = Accent,
                                            activeTrackColor = Accent,
                                            inactiveTrackColor = Surface2
                                    )
                    )
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Slow", color = TextSecondary, fontSize = 11.sp)
                        Text("Fast", color = TextSecondary, fontSize = 11.sp)
                    }
                }
            }

            // ── Action buttons ─────────────────────────────────────────────
            Card2 {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Water + Fast-grow row
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GreenButton(
                                text = "💧 Water",
                                modifier = Modifier.weight(1f),
                                onClick = onWaterPlant,
                                primary = true
                        )
                        GreenButton(
                                text = "⚡ Boost",
                                modifier = Modifier.weight(1f),
                                onClick = onFastGrow,
                                primary = false
                        )
                    }
                    // Set wallpaper full-width
                    GreenButton(
                            text = "🖼  Set as Live Wallpaper",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onSetWallpaper,
                            primary = true
                    )
                    // Reset row
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                                onClick = onReset,
                                modifier =
                                        Modifier.size(36.dp)
                                                .background(Surface2, CircleShape)
                                                .border(1.dp, Border, CircleShape)
                        ) {
                            Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Reset Growth",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text("Reset growth to seedling", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ── Small helper composables ───────────────────────────────────────────────────
@Composable
private fun Card2(content: @Composable () -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Brush.verticalGradient(listOf(Surface1, Surface2)))
                            .border(1.dp, Border, RoundedCornerShape(20.dp))
                            .padding(18.dp)
    ) { content() }
}

@Composable
private fun GreenButton(
        text: String,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
        primary: Boolean
) {
    Button(
            onClick = onClick,
            modifier = modifier.height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = if (primary) Accent else Surface2,
                            contentColor = if (primary) BG else TextPrimary
                    )
    ) { Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }
}

// ── Live preview canvas (mini version of the wallpaper) ───────────────────────
@Composable
private fun TreePreviewCanvas(growthLevel: Float) {
    var localWind by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        while (true) {
            localWind += 0.04f
            delay(33)
        }
    }
    val wind = localWind

    AndroidView(
            factory = { ctx ->
                object : View(ctx) {
                    override fun onDraw(canvas: Canvas) {
                        super.onDraw(canvas)
                        drawScene(canvas, width.toFloat(), height.toFloat(), growthLevel, wind)
                    }
                }
            },
            update = { view -> view.invalidate() },
            modifier = Modifier.fillMaxSize()
    )
}

/** Lightweight mini-draw matching the wallpaper visual style */
private fun drawScene(canvas: Canvas, w: Float, h: Float, growth: Float, wind: Float) {
    val cx = w / 2f
    val groundY = h * 0.80f

    // Sky
    val skyShader =
            LinearGradient(
                    0f,
                    0f,
                    0f,
                    groundY,
                    intArrayOf(
                            Color.parseColor("#060D1A"),
                            Color.parseColor("#0A1E35"),
                            Color.parseColor("#0E2E1C")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
            )
    val skyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = skyShader }
    canvas.drawRect(0f, 0f, w, groundY, skyPaint)

    // Ground
    val gndPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#1A0E07") }
    canvas.drawRect(0f, groundY, w, h, gndPaint)
    val glowShader =
            LinearGradient(
                    0f,
                    groundY,
                    0f,
                    groundY + 40f,
                    intArrayOf(Color.parseColor("#5C8A3C"), Color.TRANSPARENT),
                    null,
                    Shader.TileMode.CLAMP
            )
    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { shader = glowShader }
    canvas.drawRect(0f, groundY, w, groundY + 40f, glowPaint)

    // Grass (mini)
    val maxTH = h * 0.78f
    val treeH = (growth / 2000f * maxTH).coerceAtMost(maxTH)
    val vegH = (treeH * 0.25f).coerceAtLeast(15f)

    val grassP =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2E7D32")
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 1.5f
            }
    repeat(40) { i ->
        val x = (i.toFloat() / 40f) * w + 3f
        val gH = vegH * (0.6f + (i % 5) * 0.08f) * ((growth / 200f).coerceAtMost(1f))
        val wOff = sin(wind.toDouble() + x / 30.0).toFloat() * 4f
        canvas.drawLine(x, groundY, x + wOff, groundY - gH, grassP)
    }

    // Trunk
    if (treeH < 4f) return
    val baseHalf = (12f + growth / 2000f * 30f).coerceAtMost(35f)
    val topHalf = baseHalf * 0.12f
    val windSway = sin(wind.toDouble()).toFloat() * 3f
    val topX = cx + windSway
    val topY = groundY - treeH

    val trunkPath = Path()
    val ctrl = groundY - treeH * 0.45f
    val ctrlX = cx + windSway * 0.5f
    trunkPath.moveTo(cx - baseHalf, groundY)
    trunkPath.quadTo(ctrlX - baseHalf * 0.3f, ctrl, topX - topHalf, topY)
    trunkPath.lineTo(topX + topHalf, topY)
    trunkPath.quadTo(ctrlX + baseHalf * 0.8f, ctrl, cx + baseHalf, groundY)
    trunkPath.close()

    val trunkFill =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3E2006")
                style = Paint.Style.FILL
            }
    canvas.drawPath(trunkPath, trunkFill)

    // Branches (simplified recursive, max depth 3)
    if (growth > 300f) {
        val bAlpha = ((growth - 300f) / 200f * 255f).toInt().coerceIn(0, 255)
        val brP =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#5C3010")
                    style = Paint.Style.STROKE
                    strokeCap = Paint.Cap.ROUND
                    alpha = bAlpha
                }
        val leafP =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#2E7D32")
                    style = Paint.Style.FILL
                }

        fun drawBranch(x: Float, y: Float, ang: Float, len: Float, thick: Float, dep: Int) {
            if (dep > 3 || len < 4f) return
            val dT = ((growth - 300f - dep * 220f) / 140f).coerceIn(0f, 1f)
            if (dT <= 0f) return
            val sw = sin(wind.toDouble() + dep * 0.8).toFloat() * windSway * (dep / 3f)
            val ex = x + cos(ang + sw / 25f) * len * dT
            val ey = y + sin(ang + sw / 25f) * len * dT
            brP.strokeWidth = thick
            canvas.drawLine(x, y, ex, ey, brP)
            if (dep >= 2 && dT > 0.5f) {
                leafP.alpha = ((dT - 0.5f) / 0.5f * 200f).toInt()
                canvas.drawCircle(ex, ey, len * 0.28f, leafP)
            }
            drawBranch(ex, ey, ang - 0.46f, len * 0.60f, thick * 0.55f, dep + 1)
            drawBranch(ex, ey, ang + 0.46f, len * 0.60f, thick * 0.55f, dep + 1)
        }

        listOf(0.38f, 0.58f, 0.75f, 0.90f).forEachIndexed { idx, frac ->
            val bY = groundY - treeH * frac
            val bX = lerp2(cx, topX, frac)
            val bLen = treeH * (0.28f - idx * 0.03f)
            val bT = lerp2(baseHalf * 2f, topHalf * 2f, frac) * 0.22f
            val gf = ((growth - 250f - idx * 200f) / 200f).coerceIn(0f, 1f)
            if (gf > 0f) {
                drawBranch(bX, bY, -(Math.PI * 0.62f).toFloat(), bLen * gf, bT, 0)
                drawBranch(bX, bY, -(Math.PI * 0.38f).toFloat(), bLen * gf, bT, 0)
            }
        }
    }

    // Screen-edge vine (left only in preview)
    if (growth > 200f) {
        val climbFrac = ((growth - 200f) / 700f).coerceIn(0f, 1f)
        val endY = h - h * climbFrac
        val vPath = Path()
        vPath.moveTo(0f, h)
        var yy = h
        while (yy > endY && yy > 0f) {
            val off = sin(yy / 60.0).toFloat() * 20f
            val wOff = sin(wind.toDouble() + yy / 90.0).toFloat() * 3f
            vPath.lineTo(abs(off) + 6f + wOff, yy)
            yy -= 8f
        }
        val vP =
                Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#56A15E")
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    strokeCap = Paint.Cap.ROUND
                }
        canvas.drawPath(vPath, vP)
    }
}

private fun lerp2(a: Float, b: Float, t: Float) = a + (b - a) * t

// ── Wallpaper launcher ────────────────────────────────────────────────────────
private fun setWallpaper(context: Context) {
    val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
    intent.putExtra(
            WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, PlantWallpaperService::class.java)
    )
    context.startActivity(intent)
}

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF080F08)
@Composable
fun HomePagePreview() {
    TestApplicationTheme {
        HomePageContent(
                welcomeMessage = "World Tree",
                growthLevel = 900f,
                growthSpeed = 1f,
                onGrowthSpeedChange = {},
                onWaterPlant = {},
                onFastGrow = {},
                onReset = {},
                onSetWallpaper = {}
        )
    }
}
