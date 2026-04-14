package com.sergivm.monsterpacks.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceVariantDark
import com.sergivm.monsterpacks.presentation.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Title bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "SETTINGS",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsRow(icon = "🌐", label = "Language") { showLanguageDialog = true }
            SettingsRow(icon = "▶", label = "Google Play") {
                // TODO: trigger Google Play Games sign-in
            }
            SettingsRow(
                icon = "👤",
                label = "Account",
                sublabel = state.playerState.username
            ) { showUsernameDialog = true }
            SettingsRow(icon = "ℹ", label = "Terms of Use") {
                // TODO: open Terms of Use URL or in-app screen
            }
            SettingsRow(icon = "🔒", label = "Privacy Notice") {
                // TODO: open Privacy Notice URL or in-app screen
            }
        }
    }

    // ── Username change dialog ────────────────────────────────────────────────
    if (showUsernameDialog) {
        UsernameChangeDialog(
            currentUsername = state.playerState.username,
            alreadyChanged = state.playerState.usernameChanged,
            onConfirm = { newName ->
                viewModel.changeUsername(newName)
                showUsernameDialog = false
            },
            onDismiss = { showUsernameDialog = false }
        )
    }

    // ── Language dialog ───────────────────────────────────────────────────────
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = SurfaceDark,
            title = { Text("Language") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // TODO: Wire to actual locale switching logic
                    TextButton(onClick = { showLanguageDialog = false }) { Text("English") }
                    TextButton(onClick = { showLanguageDialog = false }) { Text("Español") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// ── Settings Row ──────────────────────────────────────────────────────────────

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    sublabel: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon, fontSize = 20.sp)
            Column {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                sublabel?.let {
                    Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color.Gray)
    }
}

// ── Username Change Dialog ────────────────────────────────────────────────────

@Composable
private fun UsernameChangeDialog(
    currentUsername: String,
    alreadyChanged: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentUsername) }
    val isValid = text.trim().isNotBlank() && text.trim().length <= 20

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = { Text("Change Username") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (alreadyChanged) {
                    Text(
                        "Your username has already been changed once and cannot be changed again.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        "You can change your username one time only.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.length <= 20) text = it },
                        label = { Text("New username") },
                        singleLine = true,
                        enabled = !alreadyChanged
                    )
                }
            }
        },
        confirmButton = {
            if (!alreadyChanged) {
                Button(
                    onClick = { if (isValid) onConfirm(text.trim()) },
                    enabled = isValid
                ) { Text("Confirm") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
