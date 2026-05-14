package com.danissimo.glyphgeekbox.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danissimo.glyphgeekbox.R
import com.danissimo.glyphgeekbox.ui.theme.NothingAndroidSDKDemoTheme

class AboutActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val versionName = try {
            packageManager.getPackageInfo(packageName, 0).versionName.toString()
        } catch (e: Exception) {
            "Unknown"
        }

        setContent {
            NothingAndroidSDKDemoTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text(stringResource(R.string.about_app)) },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { innerPadding ->
                    AboutScreen(versionName, Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun AboutScreen(versionName: String, modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val scrollState = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier.size(128.dp)
            )

            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.version_format, versionName),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            AboutLinkItem(
                title = stringResource(R.string.github_link),
                url = "https://github.com/danissomo/GlyphMatrix-AODGeekBox",
                icon = Icons.Default.Star
            )

            AboutLinkItem(
                title = stringResource(R.string.telegram_link),
                url = "https://t.me/dyloen",
                icon = Icons.Default.Info
            )

            AboutLinkItem(
                title = stringResource(R.string.latest_release),
                url = "https://github.com/danissomo/GlyphMatrix-AODGeekBox/releases/latest",
                icon = Icons.Default.Info
            )
        }
    }

    @Composable
    fun AboutLinkItem(title: String, url: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
        val context = LocalContext.current
        OutlinedCard(
            onClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null)
                Text(text = title, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
