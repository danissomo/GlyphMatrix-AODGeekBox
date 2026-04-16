package com.danissimo.glyphgeekbox

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
    val iconRes: Int
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
        MiniApp(R.string.toy_name_ultimate_key, R.string.toy_summary_ultimate_key, R.drawable.ultimate_essential_thumbnail),
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
                    val intent = Intent().apply {// Указываем пакет приложения и полное имя класса Activity
                        setClassName(
                            "com.nothing.thirdparty", // Замените на реальный пакет, если он другой
                            "com.nothing.thirdparty.matrix.toys.manager.AodToySelectActivity"
                        )
                        // Если нужно передать флаги для открытия в новом окне
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Если Activity не найдена или доступ запрещен (например, export=false)
                    e.printStackTrace()
                    // Можно отправить пользователя в общие настройки как запасной вариант
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
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
    }
}
