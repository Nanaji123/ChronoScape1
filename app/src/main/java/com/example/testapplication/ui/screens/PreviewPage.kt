package com.example.testapplication.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.viewmodel.HomeViewModel
import com.example.testapplication.wallpaper.PlantWallpaperService

// --- Colours matching the app theme ---
private val BG = Color(0xFF080F08)
private val Surface1 = Color(0xFF0E1E0E)
private val Surface2 = Color(0xFF162516)
private val Accent = Color(0xFF4CAF50)
private val AccentDim = Color(0xFF2E7D32)
private val TextPrimary = Color(0xFFE8F5E9)
private val TextSecondary = Color(0xFF81C784)
private val Border = Color(0xFF2E4A2E)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewPage(onBack: () -> Unit, viewModel: HomeViewModel = viewModel()) {
    val context = LocalContext.current
    val progress by
            animateFloatAsState(
                    targetValue = viewModel.growthLevel / 2000f,
                    animationSpec = tween(400),
                    label = "growth_progress"
            )

    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    "World Tree Preview",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        },
                        colors =
                                TopAppBarDefaults.topAppBarColors(
                                        containerColor = BG,
                                        titleContentColor = TextPrimary,
                                        navigationIconContentColor = TextPrimary
                                )
                )
            }
    ) { innerPadding ->
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .padding(innerPadding)
                                .background(BG)
                                .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Cinematic Preview Box
            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Surface1)) {
                TreePreviewCanvas(
                        growthLevel = viewModel.growthLevel,
                        modifier = Modifier.fillMaxSize()
                )

                // Overlay Badge
                Surface(
                        modifier = Modifier.padding(16.dp).align(Alignment.TopEnd),
                        color = Color.Black.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                            "LIVE PREVIEW",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                    )
                }
            }

            // 2. Controls Section
            Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Growth Progress Card
                ControlCard {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Growth Level", color = TextSecondary, fontSize = 13.sp)
                            Text(
                                    "${viewModel.growthLevel.toInt()} / 2000",
                                    color = Accent,
                                    fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                color = Accent,
                                trackColor = Surface2
                        )
                    }
                }

                // Growth Duration Control
                ControlCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Growth Speed", color = TextSecondary, fontSize = 13.sp)

                        val presets = listOf(1f, 60f, 1440f, 14400f)
                        val labels = listOf("1m", "1h", "1d", "10d")

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            presets.forEachIndexed { i, mins ->
                                val selected = viewModel.growthDurationMinutes == mins
                                Box(
                                        modifier =
                                                Modifier.weight(1f)
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(
                                                                if (selected) Accent else Surface2
                                                        )
                                                        .clickable {
                                                            viewModel.setGrowthDuration(mins)
                                                        }
                                                        .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                            labels[i],
                                            color = if (selected) BG else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ControlButton(
                            text = "💧 Water",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.waterPlant() },
                            primary = true
                    )
                    ControlButton(
                            text = "✂️ Prune",
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.shrinkGrowth() },
                            primary = false
                    )
                }

                ControlButton(
                        text = "🖼  Set as Wallpaper",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { setWallpaper(context) },
                        primary = true
                )

                // Reset
                Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    IconButton(onClick = { viewModel.resetGrowth() }) {
                        Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = TextSecondary
                        )
                    }
                    Text("Reset Tree", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun ControlCard(content: @Composable () -> Unit) {
    Box(
            modifier =
                    Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Surface1)
                            .border(1.dp, Border, RoundedCornerShape(20.dp))
                            .padding(18.dp)
    ) { content() }
}

@Composable
private fun ControlButton(text: String, modifier: Modifier, onClick: () -> Unit, primary: Boolean) {
    Button(
            onClick = onClick,
            modifier = modifier.height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors =
                    ButtonDefaults.buttonColors(
                            containerColor = if (primary) Accent else Surface2,
                            contentColor = if (primary) BG else TextPrimary
                    )
    ) { Text(text, fontWeight = FontWeight.Bold) }
}

private fun setWallpaper(context: android.content.Context) {
    val intent = Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
    intent.putExtra(
            android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            ComponentName(context, PlantWallpaperService::class.java)
    )
    context.startActivity(intent)
}
