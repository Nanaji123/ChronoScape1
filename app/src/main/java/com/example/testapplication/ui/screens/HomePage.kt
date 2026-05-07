package com.example.testapplication.ui.screens

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.testapplication.ui.navigation.Screen
import com.example.testapplication.ui.theme.TestApplicationTheme
import com.example.testapplication.viewmodel.HomeViewModel
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
fun HomePage(
        navController: NavController,
        modifier: Modifier = Modifier,
        viewModel: HomeViewModel = viewModel()
) {
    HomePageContent(
            modifier = modifier,
            welcomeMessage = viewModel.welcomeMessage,
            currentDay = viewModel.currentLifeDay,
            totalDays = viewModel.totalLifeDays,
            onPreviewWorldTree = { navController.navigate(Screen.Preview.route) },
            onPreviewLifeProgress = { navController.navigate(Screen.LifePreview.route) }
    )
}

// ──────────────────────────────────────────────────────────────────────────────
@Composable
fun HomePageContent(
        modifier: Modifier = Modifier,
        welcomeMessage: String,
        currentDay: Int,
        totalDays: Int,
        onPreviewWorldTree: () -> Unit,
        onPreviewLifeProgress: () -> Unit
) {
    Column(
            modifier =
                    modifier.fillMaxSize()
                            .background(BG)
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // --- Header ---
        Column {
            Text(
                    text = "Discover",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
            )
            Text(
                    text = "Live Wallpapers",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    letterSpacing = 2.sp
            )
        }

        // --- Grid of Wallpapers ---
        // 1. World Tree
        WallpaperCard(
                title = "World Tree",
                description = "Grow your own ancient tree with seasonal changes.",
                onClick = onPreviewWorldTree,
                previewContent = { TreePreviewCanvas(growthLevel = 900f) }
        )

        // 2. Life Progression (Blue Theme)
        LifeProgressCard(
                currentDay = currentDay,
                totalDays = totalDays,
                onClick = onPreviewLifeProgress
        )
    }
}

@Composable
fun LifeProgressCard(currentDay: Int, totalDays: Int, onClick: () -> Unit) {
    val progressBlue = androidx.compose.ui.graphics.Color(0xFF2196F3)
    val progressBlueDim = androidx.compose.ui.graphics.Color(0xFF1565C0)

    Card(
            modifier =
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Surface1),
            shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                            text = "Daily Journey",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                    )
                    Text(
                            text = "$totalDays-Day Progression",
                            fontSize = 12.sp,
                            color = TextSecondary
                    )
                }
                Text(
                        text = "Day $currentDay",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = progressBlue
                )
            }

            Spacer(Modifier.height(20.dp))

            val columns = 28
            val rows = 13

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (r in 0 until rows) {
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (c in 0 until columns) {
                            val dayIndex = r * columns + c
                            if (dayIndex < totalDays) {
                                val isFilled = dayIndex < currentDay
                                Box(
                                        modifier =
                                                Modifier.size(8.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(
                                                                if (isFilled) {
                                                                    if (dayIndex == currentDay - 1)
                                                                            progressBlue
                                                                    else
                                                                            progressBlueDim.copy(
                                                                                    alpha = 0.6f
                                                                            )
                                                                } else Surface2
                                                        )
                                )
                            } else {
                                Spacer(Modifier.size(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WallpaperCard(
        title: String,
        description: String,
        onClick: () -> Unit,
        previewContent: @Composable () -> Unit
) {
    Card(
            modifier =
                    Modifier.fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = Surface1),
            shape = RoundedCornerShape(24.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Preview Thumbnail
            Box(modifier = Modifier.width(110.dp).fillMaxHeight().background(Surface2)) {
                previewContent()
            }

            // Content
            Column(
                    modifier = Modifier.padding(16.dp).fillMaxHeight(),
                    verticalArrangement = Arrangement.Center
            ) {
                Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                        text = description,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                )
                Spacer(Modifier.height(12.dp))
                Surface(color = AccentDim.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                    Text(
                            "PREVIEW",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Accent
                    )
                }
            }
        }
    }
}

// ── Live preview canvas (mini version of the wallpaper) ───────────────────────
@Composable
fun TreePreviewCanvas(growthLevel: Float, modifier: Modifier = Modifier) {
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
                object : android.view.View(ctx) {
                    override fun onDraw(canvas: android.graphics.Canvas) {
                        super.onDraw(canvas)
                        drawScene(canvas, width.toFloat(), height.toFloat(), growthLevel, wind)
                    }
                }
            },
            update = { view -> view.invalidate() },
            modifier = modifier.fillMaxSize()
    )
}

/** Lightweight mini-draw matching the wallpaper visual style */
fun drawScene(canvas: android.graphics.Canvas, w: Float, h: Float, growth: Float, wind: Float) {
    val cx = w / 2f
    val groundY = h * 0.80f

    // Sky
    val skyShader =
            android.graphics.LinearGradient(
                    0f,
                    0f,
                    0f,
                    groundY,
                    intArrayOf(
                            android.graphics.Color.parseColor("#060D1A"),
                            android.graphics.Color.parseColor("#0A1E35"),
                            android.graphics.Color.parseColor("#0E2E1C")
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    android.graphics.Shader.TileMode.CLAMP
            )
    val skyPaint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                shader = skyShader
            }
    canvas.drawRect(0f, 0f, w, groundY, skyPaint)

    // Ground
    val gndPaint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#1A0E07")
            }
    canvas.drawRect(0f, groundY, w, h, gndPaint)
    val glowShader =
            android.graphics.LinearGradient(
                    0f,
                    groundY,
                    0f,
                    groundY + 40f,
                    intArrayOf(
                            android.graphics.Color.parseColor("#5C8A3C"),
                            android.graphics.Color.TRANSPARENT
                    ),
                    null,
                    android.graphics.Shader.TileMode.CLAMP
            )
    val glowPaint =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                shader = glowShader
            }
    canvas.drawRect(0f, groundY, w, groundY + 40f, glowPaint)

    // Grass (mini)
    val maxTH = h * 0.78f
    val treeH = (growth / 2000f * maxTH).coerceAtMost(maxTH)
    val vegH = (treeH * 0.25f).coerceAtLeast(15f)

    val grassP =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#2E7D32")
                style = android.graphics.Paint.Style.STROKE
                strokeCap = android.graphics.Paint.Cap.ROUND
                strokeWidth = 1.5f
            }
    repeat(40) { i ->
        val x = (i.toFloat() / 40f) * w + 3f
        val gH = vegH * (0.6f + (i % 5) * 0.08f) * ((growth / 200f).coerceAtMost(1f))
        val wOff = kotlin.math.sin(wind.toDouble() + x / 30.0).toFloat() * 4f
        canvas.drawLine(x, groundY, x + wOff, groundY - gH, grassP)
    }

    // Trunk
    if (treeH < 4f) return
    val baseHalf = (12f + growth / 2000f * 30f).coerceAtMost(35f)
    val topHalf = baseHalf * 0.12f
    val windSway = kotlin.math.sin(wind.toDouble()).toFloat() * 3f
    val topX = cx + windSway
    val topY = groundY - treeH

    val trunkPath = android.graphics.Path()
    val ctrl = groundY - treeH * 0.45f
    val ctrlX = cx + windSway * 0.5f
    trunkPath.moveTo(cx - baseHalf, groundY)
    trunkPath.quadTo(ctrlX - baseHalf * 0.3f, ctrl, topX - topHalf, topY)
    trunkPath.lineTo(topX + topHalf, topY)
    trunkPath.quadTo(ctrlX + baseHalf * 0.8f, ctrl, cx + baseHalf, groundY)
    trunkPath.close()

    val trunkFill =
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = android.graphics.Color.parseColor("#3E2006")
                style = android.graphics.Paint.Style.FILL
            }
    canvas.drawPath(trunkPath, trunkFill)

    // Branches (simplified recursive, max depth 3)
    if (growth > 300f) {
        val bAlpha = ((growth - 300f) / 200f * 255f).toInt().coerceIn(0, 255)
        val brP =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#5C3010")
                    style = android.graphics.Paint.Style.STROKE
                    strokeCap = android.graphics.Paint.Cap.ROUND
                    alpha = bAlpha
                }
        val leafP =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#2E7D32")
                    style = android.graphics.Paint.Style.FILL
                }

        fun drawBranch(x: Float, y: Float, ang: Float, len: Float, thick: Float, dep: Int) {
            if (dep > 3 || len < 4f) return
            val dT = ((growth - 300f - dep * 220f) / 140f).coerceIn(0f, 1f)
            if (dT <= 0f) return
            val sw = kotlin.math.sin(wind.toDouble() + dep * 0.8).toFloat() * windSway * (dep / 3f)
            val ex = x + kotlin.math.cos(ang + sw / 25f) * len * dT
            val ey = y + kotlin.math.sin(ang + sw / 25f) * len * dT
            brP.strokeWidth = thick
            canvas.drawLine(x, y, ex, ey, brP)
            if (dep >= 2 && dT > 0.5f) {
                leafP.alpha = ((dT - 0.5f) / 0.5f * 200f).toInt()
                canvas.drawCircle(ex, ey, len * 0.28f, leafP)
            }
            drawBranch(ex, ey, ang - 0.46f, len * 0.60f, thick * 0.55f, dep + 1)
            drawBranch(ex, ey, ang + 0.46f, len * 0.60f, thick * 0.55f, dep + 1)
        }

        fun lerp2(a: Float, b: Float, t: Float) = a + (b - a) * t

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
        val vPath = android.graphics.Path()
        vPath.moveTo(0f, h)
        var yy = h
        while (yy > endY && yy > 0f) {
            val off = kotlin.math.sin(yy / 60.0).toFloat() * 20f
            val wOff = kotlin.math.sin(wind.toDouble() + yy / 90.0).toFloat() * 3f
            vPath.lineTo(kotlin.math.abs(off) + 6f + wOff, yy)
            yy -= 8f
        }
        val vP =
                android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.parseColor("#56A15E")
                    style = android.graphics.Paint.Style.STROKE
                    strokeWidth = 3f
                    strokeCap = android.graphics.Paint.Cap.ROUND
                }
        canvas.drawPath(vPath, vP)
    }
}

private fun lerp2(a: Float, b: Float, t: Float) = a + (b - a) * t

// ── Preview ───────────────────────────────────────────────────────────────────
@Preview(showBackground = true, backgroundColor = 0xFF080F08)
@Composable
fun HomePagePreview() {
    TestApplicationTheme {
        HomePageContent(
            welcomeMessage = "World Tree",
            currentDay = 124,
            totalDays = 360,
            onPreviewWorldTree = {},
            onPreviewLifeProgress = {}
        )
    }
}
