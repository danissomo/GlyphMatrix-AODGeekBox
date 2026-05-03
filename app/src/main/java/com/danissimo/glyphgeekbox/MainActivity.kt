package com.danissimo.glyphgeekbox

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.danissimo.glyphgeekbox.ui.UltimateSettingsActivity
import com.danissimo.glyphgeekbox.ui.theme.NothingAndroidSDKDemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NothingAndroidSDKDemoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SetupGuideScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class MiniApp(
    val nameRes: Int,
    val descriptionRes: Int,
    val iconRes: Int,
    val onSettingsClick: (() -> Unit)? = null
)

@Composable
fun SetupGuideScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val miniApps = listOf(
        MiniApp(R.string.toy_name_animation, R.string.toy_summary_animation, R.drawable.animation_thumbnail),
        MiniApp(R.string.toy_name_bad_apple, R.string.toy_summary_bad_apple, R.drawable.badapple_thumbnail),
        MiniApp(R.string.toy_name_perlin, R.string.toy_summary_perlin, R.drawable.perlin_thumbnail),
        MiniApp(R.string.toy_name_game_of_life, R.string.toy_summary_game_of_life, R.drawable.game_of_life_thumbnail),
        MiniApp(R.string.toy_name_liquid_simulation, R.string.toy_summary_liquid_simulation, R.drawable.liquid_sim_thumbnail),
        MiniApp(R.string.toy_name_pong, R.string.toy_summary_pong, R.drawable.pong_thumbnail),
        MiniApp(R.string.toy_name_white_noise, R.string.toy_summary_white_noise, R.drawable.white_noise_thumbnail),
        MiniApp(R.string.toy_name_mandelbrot, R.string.toy_summary_mandelbrot, R.drawable.mandelbrot_thumbnail),
        MiniApp(R.string.toy_name_charge, R.string.toy_summary_charge, R.drawable.charge_thumbnail),
        MiniApp(R.string.toy_name_ultimate_key, R.string.toy_summary_ultimate_key, R.drawable.ultimate_essential_thumbnail) {
            context.startActivity(Intent(context, UltimateSettingsActivity::class.java))
        },
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.setup_guide_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        StepSection(
            title = stringResource(R.string.setup_step_1_title),
            description = stringResource(R.string.setup_step_1_desc)
        )

        AdbCommandBox(command = stringResource(R.string.setup_adb_command_1))
        AdbCommandBox(command = stringResource(R.string.setup_adb_command_2))

        StepSection(
            title = stringResource(R.string.setup_step_2_title),
            description = stringResource(R.string.setup_step_2_desc)
        )

        Button(
            onClick = {
                context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.open_accessibility_settings))
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.mini_apps_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Button(
            onClick = {
                try {
                    val intent = Intent().apply {
                        setClassName(
                            "com.nothing.thirdparty",
                            "com.nothing.thirdparty.matrix.toys.manager.AodToySelectActivity"
                        )
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.widget_label))
        }

        miniApps.forEach { app ->
            MiniAppItem(app)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Note: After setup, use Long Press on the Essential Key to switch between Glyph modes.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}

@Composable
fun StepSection(title: String, description: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AdbCommandBox(command: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        color = Color(0xFF2B2B2B)
    ) {
        Text(
            text = command,
            modifier = Modifier.padding(12.dp),
            color = Color(0xFF64FFDA),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
        )
    }
}

@Composable
fun MiniAppItem(app: MiniApp) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = app.onSettingsClick != null) { app.onSettingsClick?.invoke() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Image(
                    painter = painterResource(id = app.iconRes),
                    contentDescription = null,
                    modifier = Modifier.padding(8.dp)
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(app.nameRes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(app.descriptionRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (app.onSettingsClick != null) {
                FilledTonalIconButton(
                    onClick = app.onSettingsClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
