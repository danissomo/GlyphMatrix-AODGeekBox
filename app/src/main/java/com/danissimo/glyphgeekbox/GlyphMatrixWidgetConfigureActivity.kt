package com.danissimo.glyphgeekbox

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.danissimo.glyphgeekbox.demos.GlyphMatrixService
import com.danissimo.glyphgeekbox.ui.theme.NothingAndroidSDKDemoTheme
import com.danissimo.glyphgeekbox.utils.SettingsManager
import com.danissimo.glyphgeekbox.utils.generate_all_circle_points
import com.nothing.ketchum.Common
import kotlin.math.log10

class GlyphMatrixWidgetConfigureActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setResult(RESULT_CANCELED)

        val intent = intent
        val extras = intent.extras
        if (extras != null) {
            appWidgetId = extras.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        enableEdgeToEdge()
        setContent {
            NothingAndroidSDKDemoTheme {
                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        CenterAlignedTopAppBar(title = { Text("Widget Settings") })
                    }
                ) { innerPadding ->
                    ConfigureScreen(
                        modifier = Modifier.padding(innerPadding),
                        onConfirm = { spacing, radius, bgColor ->
                            val context = this@GlyphMatrixWidgetConfigureActivity
                            SettingsManager.saveWidgetSpacing(context, appWidgetId, spacing)
                            SettingsManager.saveWidgetRadius(context, appWidgetId, radius)
                            SettingsManager.saveWidgetBackground(context, appWidgetId, bgColor)

                            val updateIntent = Intent("com.danissimo.glyphgeekbox.ACTION_UPDATE_WIDGET")
                            context.sendBroadcast(updateIntent)

                            val resultValue = Intent().apply {
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            }
                            setResult(Activity.RESULT_OK, resultValue)
                            finish()
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun WidgetPreview(spacing: Float, radius: Float, bgColor: Color) {
        val frame = GlyphMatrixService.lastFrame ?: IntArray(Common.getDeviceMatrixLength() * Common.getDeviceMatrixLength()) { 0 }
        val size = Common.getDeviceMatrixLength()
        val points = remember(size) { generate_all_circle_points(size) }
        val maxVal = remember(frame) { frame.maxOrNull()?.toFloat()?.coerceAtLeast(1f) ?: 1f }

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(bgColor, CircleShape)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = drawContext.size.width / size
                
                for ((x, y) in points) {
                    val index = y * size + x
                    val intensity = if (index < frame.size) frame[index] else 0
                    val alpha = if (intensity > 0) {
                        log10(9f * intensity.toFloat() / maxVal + 1f).coerceIn(0f, 1f)
                    } else 0f
                    
                    val left = x * cellSize + spacing
                    val top = y * cellSize + spacing
                    val itemSize = cellSize - 2 * spacing

                    if (itemSize > 0) {
                        drawRoundRect(
                            color = Color.White.copy(1f, alpha, alpha, alpha),
                            topLeft = Offset(left, top),
                            size = Size(itemSize, itemSize),
                            cornerRadius = CornerRadius(radius, radius)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ConfigureScreen(modifier: Modifier = Modifier, onConfirm: (Int, Int, Int) -> Unit) {
        val context = this@GlyphMatrixWidgetConfigureActivity
        
        var spacing by remember { 
            mutableFloatStateOf(SettingsManager.getWidgetSpacing(context, appWidgetId).toFloat()) 
        }
        var radius by remember { 
            mutableFloatStateOf(SettingsManager.getWidgetRadius(context, appWidgetId).toFloat()) 
        }
        var selectedColor by remember { 
            mutableStateOf(Color(SettingsManager.getWidgetBackground(context, appWidgetId))) 
        }

        val colors = listOf(
            Color(0xFF000000),
            Color(0xFF2B2B2B),
            Color(0xFF444444),
            Color(0xFFFFFFFF),
            Color(0xFFFF0000),
            Color(0xFF00FF00),
            Color(0xFF0000FF),
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text("Preview", style = MaterialTheme.typography.titleMedium)
            
            WidgetPreview(spacing = spacing, radius = radius, bgColor = selectedColor)

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Spacing: ${spacing.toInt()}px", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = spacing,
                    onValueChange = { spacing = it },
                    valueRange = 0f..10f,
                    steps = 9
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Corner Radius: ${radius.toInt()}px", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 0f..10f,
                    steps = 9
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Background Color", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(color, RoundedCornerShape(20.dp))
                                .let {
                                    if (selectedColor == color) {
                                        it.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                                            .padding(2.dp)
                                            .background(color, RoundedCornerShape(20.dp))
                                    } else it
                                }
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }

            Button(
                onClick = { onConfirm(spacing.toInt(), radius.toInt(), selectedColor.toArgb()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Settings")
            }
        }
    }
}
