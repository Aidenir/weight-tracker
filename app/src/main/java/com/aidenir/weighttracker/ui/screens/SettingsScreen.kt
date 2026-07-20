package com.aidenir.weighttracker.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aidenir.weighttracker.data.HomeAssistantConfig
import com.aidenir.weighttracker.data.ReminderConfig
import com.aidenir.weighttracker.data.WeightUnit
import com.aidenir.weighttracker.ui.WeightUiState
import com.aidenir.weighttracker.ui.components.GlassCard
import com.aidenir.weighttracker.ui.components.bottomActionClearance
import com.aidenir.weighttracker.ui.theme.BrandPrimary
import com.aidenir.weighttracker.ui.theme.TextMuted
import com.aidenir.weighttracker.ui.theme.TextPrimary
import com.aidenir.weighttracker.ui.theme.TextSecondary
import com.kyant.backdrop.Backdrop

@Composable
fun SettingsScreen(
    backdrop: Backdrop,
    state: WeightUiState,
    onUnitChange: (WeightUnit) -> Unit,
    onReminderChange: (ReminderConfig) -> Unit,
    onHomeAssistantChange: (HomeAssistantConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = state.settings ?: return
    val context = LocalContext.current
    val reminder = settings.reminder
    val topInset = WindowInsets.systemBars.asPaddingValues().calculateTopPadding()
    val bottomClearance = bottomActionClearance()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = topInset + 8.dp,
                bottom = bottomClearance
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Settings",
            color = TextSecondary,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold
        )

        // Home Assistant — configured once
        HomeAssistantCard(
            backdrop = backdrop,
            config = settings.homeAssistant,
            onSave = onHomeAssistantChange
        )

        // Units — occasional
        GlassCard(backdrop = backdrop, contentPadding = 20.dp) {
            Column {
                Text("Units", color = TextMuted, style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(8.dp))
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = listOf(WeightUnit.KG to "Kilograms", WeightUnit.LB to "Pounds")
                    options.forEachIndexed { index, (unit, label) ->
                        SegmentedButton(
                            selected = settings.unit == unit,
                            onClick = { onUnitChange(unit) },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = Color.White.copy(alpha = 0.14f),
                                inactiveContainerColor = Color.Transparent,
                                activeContentColor = TextPrimary,
                                inactiveContentColor = TextSecondary
                            )
                        ) { Text(label) }
                    }
                }
            }
        }

        // Reminder — the one you'll toggle often; pin it at the bottom of the
        // list so it sits under your thumb.
        GlassCard(backdrop = backdrop, contentPadding = 20.dp) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = reminder.enabled) {
                            TimePickerDialog(
                                context,
                                { _, h, m -> onReminderChange(reminder.copy(hour = h, minute = m)) },
                                reminder.hour,
                                reminder.minute,
                                true
                            ).show()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text("Time", color = TextSecondary, modifier = Modifier.weight(1f))
                    Text(
                        "%02d:%02d".format(reminder.hour, reminder.minute),
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Detect getting out of bed", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Use the accelerometer during your reminder window — fires when you start moving.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(
                        checked = reminder.useMotion,
                        onCheckedChange = { onReminderChange(reminder.copy(useMotion = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Morning reminder", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
                        Text("Fires at the time above.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(
                        checked = reminder.enabled,
                        onCheckedChange = { onReminderChange(reminder.copy(enabled = it)) },
                        colors = SwitchDefaults.colors(checkedThumbColor = BrandPrimary)
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeAssistantCard(
    backdrop: Backdrop,
    config: HomeAssistantConfig,
    onSave: (HomeAssistantConfig) -> Unit
) {
    var url by remember(config.baseUrl) { mutableStateOf(config.baseUrl) }
    var token by remember(config.token) { mutableStateOf(config.token) }
    var entity by remember(config.bedEntity) { mutableStateOf(config.bedEntity) }

    LaunchedEffect(config) {
        url = config.baseUrl
        token = config.token
        entity = config.bedEntity
    }

    GlassCard(backdrop = backdrop, contentPadding = 20.dp) {
        Column {
            Text("Home Assistant", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(
                "Optional — when your bed sensor flips to off during the reminder window, the app fires the reminder.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            LabeledField("Base URL", url, "https://homeassistant.local:8123") { url = it }
            Spacer(Modifier.height(10.dp))
            LabeledField("Long-lived token", token, "eyJ0eXAi…", isSecret = true) { token = it }
            Spacer(Modifier.height(10.dp))
            LabeledField("Bed entity id", entity, "binary_sensor.bed_occupancy") { entity = it }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    onSave(
                        HomeAssistantConfig(
                            baseUrl = url.trim().trimEnd('/'),
                            token = token.trim(),
                            bedEntity = entity.trim()
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.12f),
                    contentColor = TextPrimary
                )
            ) {
                Text("Save Home Assistant settings", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun LabeledField(
    label: String,
    value: String,
    placeholder: String,
    isSecret: Boolean = false,
    onChange: (String) -> Unit
) {
    Text(label, color = TextMuted, style = MaterialTheme.typography.labelMedium)
    Spacer(Modifier.height(4.dp))
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = true,
        textStyle = TextStyle(
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        ),
        cursorBrush = SolidColor(BrandPrimary),
        visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(placeholder, color = TextMuted, style = MaterialTheme.typography.bodyMedium)
            } else {
                inner()
            }
        }
    )
}
