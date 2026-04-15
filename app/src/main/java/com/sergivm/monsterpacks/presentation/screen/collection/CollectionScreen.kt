package com.sergivm.monsterpacks.presentation.screen.collection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.domain.model.Card
import com.sergivm.monsterpacks.domain.model.CardType
import com.sergivm.monsterpacks.domain.model.Rarity
import com.sergivm.monsterpacks.presentation.screen.MonsterPacksTopBar
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceVariantDark
import com.sergivm.monsterpacks.presentation.viewmodel.CollectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(viewModel: CollectionViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    var showFilters by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column {
            MonsterPacksTopBar(playerState = state.playerState)

            CollectionHeader(
                collectionName = state.collection.name,
                progressByRarity = state.progressByRarity,
                onFilterClick = { showFilters = true }
            )

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

        if (showFilters) {
            ModalBottomSheet(
                onDismissRequest = { showFilters = false },
                sheetState = sheetState,
                containerColor = SurfaceDark
            ) {
                FilterSheetContent(
                    activeRarities = state.activeRarityFilter,
                    activeTypes = state.activeTypeFilter,
                    onToggleRarity = { viewModel.toggleRarityFilter(it) },
                    onToggleType = { viewModel.toggleTypeFilter(it) },
                    onClear = { viewModel.clearFilters() }
                )
            }
        }

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

// ── Filter Sheet Content ──────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterSheetContent(
    activeRarities: Set<Rarity>,
    activeTypes: Set<CardType>,
    onToggleRarity: (Rarity) -> Unit,
    onToggleType: (CardType) -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Filters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            TextButton(onClick = onClear) { Text("Clear All") }
        }

        Spacer(Modifier.height(16.dp))

        Text("RARITY", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Rarity.values().forEach { rarity ->
                FilterChip(
                    text = rarity.displayName,
                    icon = rarity.icon,
                    selected = rarity in activeRarities,
                    color = rarity.color,
                    onClick = { onToggleRarity(rarity) }
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("TYPE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CardType.values().forEach { type ->
                FilterChip(
                    text = type.displayName,
                    icon = null,
                    selected = type in activeTypes,
                    color = type.color,
                    onClick = { onToggleType(type) }
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    text: String,
    icon: String?,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) color.copy(alpha = 0.2f) else SurfaceVariantDark,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Text(icon, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) color else Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
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
    val context = LocalContext.current
    val imageResId = remember(card.imageRes) {
        context.resources.getIdentifier(card.imageRes, "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.7f)
            .clip(RoundedCornerShape(6.dp))
            .background(if (owned) card.type.color.copy(alpha = 0.75f) else Color(0xFF1A1A1A))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (owned) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.5f
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
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
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("■", color = Color.DarkGray, fontSize = 24.sp)
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
    val context = LocalContext.current
    val imageResId = remember(card.imageRes) {
        context.resources.getIdentifier(card.imageRes, "drawable", context.packageName)
    }

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageResId != 0 && owned) {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = if (owned) "[ Missing Art ]" else "[ Locked ]",
                            color = Color.White.copy(alpha = 0.2f),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
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
