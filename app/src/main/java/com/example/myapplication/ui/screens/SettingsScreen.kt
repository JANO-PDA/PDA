package com.example.myapplication.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.myapplication.data.models.AppTheme
import com.example.myapplication.ui.components.AnimatedBackground
import com.example.myapplication.ui.theme.GlassCard
import com.example.myapplication.ui.theme.GlassPill
import com.example.myapplication.ui.viewmodel.TodoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: TodoViewModel,
    onNavigateBack: () -> Unit
) {
    val userProfile        by viewModel.userProfile.collectAsState()
    val isSystemInDarkMode = isSystemInDarkTheme()

    BackHandler { onNavigateBack() }

    AnimatedBackground {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = {
                TopAppBar(
                    title  = { Text("Settings") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ),
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // ─── Theme ─────────────────────────────────────────────────────
                SettingsSectionCard(title = "Theme") {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppTheme.entries.forEach { theme ->
                            FilterChip(
                                selected = userProfile.selectedTheme == theme,
                                onClick  = { viewModel.setAppTheme(theme) },
                                label    = {
                                    Text(
                                        theme.name.lowercase().replaceFirstChar { it.uppercase() },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                shape = GlassPill
                            )
                        }
                    }
                }

                // ─── Dark mode ─────────────────────────────────────────────────
                SettingsSectionCard(title = "Appearance") {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (userProfile.darkMode == true)
                                    Icons.Filled.DarkMode else Icons.Filled.LightMode,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Dark Mode",
                                    style      = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text  = when (userProfile.darkMode) {
                                        true  -> "Dark"
                                        false -> "Light"
                                        null  -> "System (${if (isSystemInDarkMode) "Dark" else "Light"})"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                                )
                            }
                        }

                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, "Expand")
                            }
                            DropdownMenu(
                                expanded          = expanded,
                                onDismissRequest  = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text         = { Text("System Default") },
                                    leadingIcon  = { Icon(Icons.Filled.DeviceUnknown, null) },
                                    onClick      = { viewModel.setDarkMode(null); expanded = false }
                                )
                                DropdownMenuItem(
                                    text         = { Text("Light Mode") },
                                    leadingIcon  = { Icon(Icons.Filled.LightMode, null) },
                                    onClick      = { viewModel.setDarkMode(false); expanded = false }
                                )
                                DropdownMenuItem(
                                    text         = { Text("Dark Mode") },
                                    leadingIcon  = { Icon(Icons.Filled.DarkMode, null) },
                                    onClick      = { viewModel.setDarkMode(true); expanded = false }
                                )
                            }
                        }
                    }
                }

                // ─── About ─────────────────────────────────────────────────────
                SettingsSectionCard(title = "About") {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "PDA — Personal Device Assistant",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "Version 1.0  ·  Vault-themed task manager",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = GlassCard,
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelLarge,
                color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontWeight = FontWeight.SemiBold
            )
            content()
        }
    }
}
