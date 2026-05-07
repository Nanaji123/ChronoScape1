package com.example.testapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.testapplication.viewmodel.HomeViewModel

private val BG = Color(0xFF080F08)
private val Surface1 = Color(0xFF101820)
private val Surface2 = Color(0xFF1E272E)
private val ProgressBlue = Color(0xFF2196F3)
private val ProgressBlueDim = Color(0xFF1565C0)
private val TextPrimary = Color(0xFFE8F5E9)
private val TextSecondary = Color(0xFF81C784)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifePreviewPage(onBack: () -> Unit, viewModel: HomeViewModel = viewModel()) {
    Scaffold(
            topBar = {
                TopAppBar(
                        title = {
                            Text(
                                    "Daily Journey Preview",
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
            // Cinematic Progress View
            Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp).background(Surface1),
                    contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                            text = "Day ${viewModel.currentLifeDay}",
                            fontSize = 48.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ProgressBlue
                    )
                    Text(
                            text = "of ${viewModel.totalLifeDays} Days",
                            fontSize = 16.sp,
                            color = TextSecondary
                    )
                }
            }

            // Expanded Grid
            Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                        text = "Progression History",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                )

                // The Grid (No card background)
                Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val columns = 16
                    val rows = (viewModel.totalLifeDays / columns) + 1

                    for (r in 0 until rows) {
                        Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            for (c in 0 until columns) {
                                val dayIndex = r * columns + c
                                if (dayIndex < viewModel.totalLifeDays) {
                                    val isFilled = dayIndex < viewModel.currentLifeDay
                                    Box(
                                            modifier =
                                                    Modifier.size(14.dp)
                                                            .clip(RoundedCornerShape(3.dp))
                                                            .background(
                                                                    if (isFilled) {
                                                                        if (dayIndex ==
                                                                                viewModel
                                                                                        .currentLifeDay -
                                                                                        1)
                                                                                ProgressBlue
                                                                        else
                                                                                ProgressBlueDim
                                                                                        .copy(
                                                                                                alpha =
                                                                                                        0.5f
                                                                                        )
                                                                    } else Surface2
                                                            )
                                    )
                                } else {
                                    Spacer(Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                        onClick = { setLifeWallpaper(context) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ProgressBlue)
                ) {
                    Text("Apply Life Progression", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

private fun setLifeWallpaper(context: android.content.Context) {
    val intent = android.content.Intent(android.app.WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER)
    intent.putExtra(
            android.app.WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
            android.content.ComponentName(
                    context,
                    "com.example.testapplication.wallpaper.LifeWallpaperService"
            )
    )
    context.startActivity(intent)
}
