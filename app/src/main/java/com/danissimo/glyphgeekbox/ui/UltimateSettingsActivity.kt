package com.danissimo.glyphgeekbox.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.danissimo.glyphgeekbox.ui.theme.NothingAndroidSDKDemoTheme
import com.danissimo.glyphgeekbox.utils.SettingsManager
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyColumnState

class UltimateSettingsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NothingAndroidSDKDemoTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = { Text("Ultimate Key Cycle") },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                ) { innerPadding ->
                    SettingsScreen(Modifier.padding(innerPadding))
                }
            }
        }
    }

    @Composable
    fun SettingsScreen(modifier: Modifier = Modifier) {
        val context = this@UltimateSettingsActivity
        
        // Load initial order and use mutableStateListOf for efficient reordering
        val initialOrder = remember { SettingsManager.getAnimationOrder(context) }
        val order = remember { mutableStateListOf<String>().apply { addAll(initialOrder) } }
        var enabled by remember { mutableStateOf(SettingsManager.getEnabledAnimations(context)) }

        val lazyListState = rememberLazyListState()
        val reorderableLazyColumnState = rememberReorderableLazyColumnState(lazyListState) { from, to ->
            order.apply {
                add(to.index, removeAt(from.index))
            }
            SettingsManager.saveAnimationOrder(context, order.toList())
        }

        Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
            Text(
                "Sequence & Visibility",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Hold and drag the handle (≡) to reorder. Toggle switches to enable/disable modes in the cycle.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyColumn(
                state = lazyListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(order, key = { it }) { animName ->
                    val isAnimEnabled = enabled.contains(animName)

                    ReorderableItem(reorderableLazyColumnState, key = animName) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp, label = "elevation")
                        
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(elevation, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isAnimEnabled) MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            tonalElevation = if (isAnimEnabled) 2.dp else 0.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Drag Handle
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Reorder",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .draggableHandle()
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = animName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAnimEnabled) MaterialTheme.colorScheme.onSurface 
                                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = if (isAnimEnabled) "Included in cycle" else "Hidden",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isAnimEnabled) MaterialTheme.colorScheme.primary 
                                                else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = isAnimEnabled,
                                    onCheckedChange = {
                                        val newEnabled = enabled.toMutableSet()
                                        if (newEnabled.contains(animName)) newEnabled.remove(animName)
                                        else newEnabled.add(animName)
                                        enabled = newEnabled
                                        SettingsManager.saveEnabledAnimations(context, enabled)
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Tip: Long-press on the handle icon (≡) and move the mode up or down to change its position in the cycle.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}
