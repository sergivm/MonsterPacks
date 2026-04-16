package com.sergivm.monsterpacks.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergivm.monsterpacks.R
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
            text = stringResource(R.string.username_setup_main_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 3.sp
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.username_setup_subtitle),
            style = MaterialTheme.typography.titleMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        OutlinedTextField(
            value = text,
            onValueChange = { if (it.length <= 20) text = it },
            label = { Text(stringResource(R.string.username_collector_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.username_length_hint, text.length),
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
            Text(stringResource(R.string.username_start), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
