package com.sergivm.monsterpacks.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark

/**
 * Full-screen prompt shown once on first launch to collect the player's username.
 */
@Composable
fun UsernameSetupScreen(onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val isValid = text.trim().isNotBlank() && text.trim().length <= 20

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MONSTER PACKS",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Choose your collector name",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= 20) text = it },
            label = { Text("Collector Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "${text.length}/20  •  You can change this once later in Settings",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = { if (isValid) onConfirm(text.trim()) },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Start Collecting", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
