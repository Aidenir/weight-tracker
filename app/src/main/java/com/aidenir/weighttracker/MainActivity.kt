package com.aidenir.weighttracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aidenir.weighttracker.ui.WeightViewModel
import com.aidenir.weighttracker.ui.components.LiquidGlassScaffold
import com.aidenir.weighttracker.ui.screens.GoalScreen
import com.aidenir.weighttracker.ui.screens.HistoryScreen
import com.aidenir.weighttracker.ui.screens.HomeScreen
import com.aidenir.weighttracker.ui.screens.SettingsScreen
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.WeightTrackerTheme
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

class MainActivity : ComponentActivity() {

    private val viewModel: WeightViewModel by viewModels()

    private val notificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.scheduleReminderNow()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_WeightTracker)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            WeightTrackerTheme {
                AppRoot(viewModel)
            }
        }
    }
}

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Today", Icons.Rounded.Home),
    History("History", Icons.Rounded.BarChart),
    Goal("Goal", Icons.Rounded.Flag),
    Settings("Settings", Icons.Rounded.Settings)
}

@Composable
private fun AppRoot(viewModel: WeightViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var current by remember { mutableStateOf(Tab.Home) }

    LiquidGlassScaffold { backdrop ->
        Box(Modifier.fillMaxSize()) {
            when (current) {
                Tab.Home -> HomeScreen(
                    backdrop = backdrop,
                    state = state,
                    onSave = viewModel::saveToday
                )
                Tab.History -> HistoryScreen(
                    backdrop = backdrop,
                    state = state,
                    onDelete = viewModel::delete
                )
                Tab.Goal -> GoalScreen(
                    backdrop = backdrop,
                    state = state,
                    onSave = viewModel::setGoal,
                    onMilestonesChange = viewModel::setMilestones
                )
                Tab.Settings -> SettingsScreen(
                    backdrop = backdrop,
                    state = state,
                    onUnitChange = viewModel::setUnit,
                    onReminderChange = viewModel::setReminder,
                    onHomeAssistantChange = viewModel::setHomeAssistant,
                    onProfileChange = viewModel::setProfile
                )
            }

            GlassBottomBar(
                backdrop = backdrop,
                current = current,
                onTab = { current = it },
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.BottomCenter)
            )
        }
    }
}

@Composable
private fun GlassBottomBar(
    backdrop: Backdrop,
    current: Tab,
    onTab: (Tab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navPadding = WindowInsets.navigationBars.asPaddingValues()
    Box(
        modifier = modifier
            .padding(bottom = navPadding.calculateBottomPadding() + 12.dp, start = 20.dp, end = 20.dp)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(26.dp) },
                effects = {
                    vibrancy()
                    blur(22.dp.toPx())
                    lens(16.dp.toPx(), 28.dp.toPx())
                },
                onDrawSurface = { drawRect(Color.White.copy(alpha = 0.07f)) }
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            windowInsets = WindowInsets(0),
            tonalElevation = 0.dp
        ) {
            Tab.entries.forEach { tab ->
                NavigationBarItem(
                    selected = current == tab,
                    onClick = { onTab(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TextPrimary,
                        selectedTextColor = TextPrimary,
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted,
                        indicatorColor = Color.White.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}
