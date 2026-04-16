package com.sergivm.monsterpacks.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.R
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
                stringResource(R.string.settings_title),
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
            SettingsRow(icon = "🌐", label = stringResource(R.string.settings_language)) { showLanguageDialog = true }
            SettingsRow(icon = "▶", label = stringResource(R.string.settings_google_play)) {
                // TODO: trigger Google Play Games sign-in
            }
            SettingsRow(
                icon = "👤",
                label = stringResource(R.string.settings_account),
                sublabel = state.playerState.username
            ) { showUsernameDialog = true }
            SettingsRow(icon = "ℹ", label = stringResource(R.string.settings_terms)) {
                // TODO: open Terms of Use URL or in-app screen
            }
            SettingsRow(icon = "🔒", label = stringResource(R.string.settings_privacy)) {
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
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // TODO: Wire to actual locale switching logic
                    TextButton(onClick = { showLanguageDialog = false }) { Text("English") }
                    TextButton(onClick = { showLanguageDialog = false }) { Text("Español") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) { Text(stringResource(R.string.settings_cancel)) }
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
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
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
        title = { Text(stringResource(R.string.settings_change_username)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (alreadyChanged) {
                    Text(
                        stringResource(R.string.settings_username_already_changed),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                } else {
                    Text(
                        stringResource(R.string.settings_username_change_one_time),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(Modifier.height(4.dp))
                    OutlinedTextField(
                        value = text,
                        onValueChange = { if (it.length <= 20) text = it },
                        label = { Text(stringResource(R.string.settings_new_username)) },
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
                ) { Text(stringResource(R.string.settings_confirm)) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.settings_cancel)) }
        }
    )
}
