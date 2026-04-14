package com.sergivm.monsterpacks.presentation.screen.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.domain.model.Card
import com.sergivm.monsterpacks.domain.model.Rarity
import com.sergivm.monsterpacks.presentation.screen.MonsterPacksTopBar
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.viewmodel.CollectionViewModel

@Composable
fun CollectionScreen(viewModel: CollectionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column {
            MonsterPacksTopBar(playerState = state.playerState)

            // Collection header with rarity progress
            CollectionHeader(
                collectionName = state.collection.name,
                progressByRarity = state.progressByRarity,
                onFilterClick = { /* TODO: show filter bottom sheet */ }
            )

            // Card grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.filteredCards, key = { it.id }) { card ->
                    val owned = state.playerState.hasCard(card.id)
                    CollectionCardCell(
                        card = card,
                        owned = owned,
                        copyCount = state.playerState.copiesOf(card.id),
                        onClick = { viewModel.selectCard(card) }
                    )
                }
            }
        }

        // Card detail dialog
        state.selectedCard?.let { card ->
            CardDetailDialog(
                card = card,
                owned = state.playerState.hasCard(card.id),
                copyCount = state.playerState.copiesOf(card.id),
                onDismiss = { viewModel.dismissCard() }
            )
        }
    }
}

// ── Collection Header ─────────────────────────────────────────────────────────

@Composable
private fun CollectionHeader(
    collectionName: String,
    progressByRarity: Map<Rarity, Pair<Int, Int>>,
    onFilterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDark)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = collectionName.uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold
            )
            IconButton(onClick = onFilterClick) {
                Text("🔍", fontSize = 20.sp)
            }
        }

        Spacer(Modifier.height(6.dp))

        // Rarity progress counters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Rarity.values().forEach { rarity ->
                val (owned, total) = progressByRarity[rarity] ?: (0 to 0)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(rarity.icon, fontSize = 14.sp)
                    Text(
                        text = "$owned/$total",
                        style = MaterialTheme.typography.labelSmall,
                        color = rarity.color,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ── Card Cell ─────────────────────────────────────────────────────────────────

@Composable
private fun CollectionCardCell(
    card: Card,
    owned: Boolean,
    copyCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (owned) card.type.color.copy(alpha = 0.75f) else Color(0xFF1A1A1A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (owned) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                // TODO: replace with card thumbnail image
                Text(card.rarity.icon, fontSize = 12.sp)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = card.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    fontSize = 9.sp
                )
                if (copyCount > 1) {
                    Text("×$copyCount", style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f), fontSize = 8.sp)
                }
            }
        } else {
            // Locked silhouette
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("■", color = Color.DarkGray, fontSize = 24.sp)  // TODO: rarity-shaped silhouette
                Text(
                    text = "#${card.collectionNumber.toString().padStart(3,'0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.DarkGray,
                    fontSize = 8.sp
                )
            }
        }
    }
}

// ── Card Detail Dialog ────────────────────────────────────────────────────────

@Composable
private fun CardDetailDialog(
    card: Card,
    owned: Boolean,
    copyCount: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(card.rarity.icon, fontSize = 20.sp)
                Text(card.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Type: ${card.type.displayName}", style = MaterialTheme.typography.bodyMedium, color = card.type.color)
                Text("Rarity: ${card.rarity.displayName}", style = MaterialTheme.typography.bodyMedium, color = card.rarity.color)
                Text("#${card.collectionNumber.toString().padStart(3,'0')}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)

                if (owned) {
                    Spacer(Modifier.height(4.dp))
                    Text("Copies owned: $copyCount", style = MaterialTheme.typography.bodyMedium, color = Color.White)
                    card.description?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text("Not yet discovered", style = MaterialTheme.typography.bodyMedium, color = Color.Gray, fontWeight = FontWeight.Light)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}
