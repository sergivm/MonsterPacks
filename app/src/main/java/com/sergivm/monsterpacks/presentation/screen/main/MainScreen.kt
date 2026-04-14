package com.sergivm.monsterpacks.presentation.screen.main

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.domain.model.Card
import com.sergivm.monsterpacks.domain.model.Rarity
import com.sergivm.monsterpacks.presentation.screen.MonsterPacksTopBar
import com.sergivm.monsterpacks.presentation.screen.UsernameSetupScreen
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.viewmodel.MainViewModel

/**
 * Main "Pack" tab. Handles:
 * - First-launch username setup
 * - Pack display and "Open a Pack" button
 * - Card reveal flow (one card at a time)
 * - Summary screen after all cards revealed
 *
 * The bottom nav bar is hidden externally when [MainUiState.isOpeningPack] is true.
 * The caller (NavGraph) is responsible for hiding/showing the nav bar.
 */
@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onPackOpeningStarted: () -> Unit = {},
    onPackOpeningFinished: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    // First launch: username setup
    if (state.isFirstLaunch) {
        UsernameSetupScreen(onConfirm = { viewModel.setUsername(it) })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        when {
            state.showSummary -> {
                SummaryScreen(
                    cards = state.drawnCards,
                    playerState = state.playerState,
                    onSave = {
                        viewModel.saveSession()
                        onPackOpeningFinished()
                    }
                )
            }

            state.isOpeningPack -> {
                state.currentCard?.let { card ->
                    CardRevealScreen(
                        card = card,
                        cardIndex = state.currentCardIndex,
                        totalCards = state.drawnCards.size,
                        isNewCard = state.playerState.copiesOf(card.id) == 0,
                        copyCount = state.playerState.copiesOf(card.id),
                        playerState = state.playerState,
                        onTap = { viewModel.revealNextCard() }
                    )
                }
            }

            else -> {
                PackIdleScreen(
                    playerState = state.playerState,
                    packName = state.packDefinition.name,
                    onOpenPack = {
                        viewModel.openPack()
                        onPackOpeningStarted()
                    }
                )
            }
        }
    }
}

// ── Pack Idle ─────────────────────────────────────────────────────────────────

@Composable
private fun PackIdleScreen(
    playerState: com.sergivm.monsterpacks.domain.model.PlayerState,
    packName: String,
    onOpenPack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonsterPacksTopBar(playerState = playerState)

        Spacer(Modifier.weight(0.1f))

        // Pack visual placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = packName.uppercase(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "[ Collection cover art ]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
                // TODO: Replace with actual pack Image composable once art is ready
            }
        }

        Spacer(Modifier.weight(0.15f))

        Button(
            onClick = onOpenPack,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Open a Pack",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black
            )
        }

        Spacer(Modifier.weight(0.1f))
    }
}

// ── Card Reveal ───────────────────────────────────────────────────────────────

@Composable
private fun CardRevealScreen(
    card: Card,
    cardIndex: Int,
    totalCards: Int,
    isNewCard: Boolean,
    copyCount: Int,
    playerState: com.sergivm.monsterpacks.domain.model.PlayerState,
    onTap: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonsterPacksTopBar(playerState = playerState)

        Spacer(Modifier.weight(0.05f))

        // Card counter hint
        Text(
            text = "${cardIndex + 1} / $totalCards",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        // Card composable
        CardView(
            card = card,
            isNewCard = isNewCard,
            copyCount = copyCount,
            modifier = Modifier
                .fillMaxWidth(0.72f)
                .aspectRatio(0.65f)
        )

        Spacer(Modifier.weight(0.1f))

        Text(
            text = "Tap to continue",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )

        Spacer(Modifier.height(24.dp))
    }
}

// ── Card View ─────────────────────────────────────────────────────────────────

@Composable
fun CardView(
    card: Card,
    isNewCard: Boolean,
    copyCount: Int,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(card.type.color.copy(alpha = 0.8f), card.type.color.copy(alpha = 0.3f))
                    )
                )
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Card name
            Text(
                text = card.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Card image placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Black.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "[ ${card.imageRes} ]",
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center
                )
                // TODO: Replace with AsyncImage or painterResource when art is ready
            }

            Spacer(Modifier.height(8.dp))

            // Description (Common / Rare / Epic only)
            if (card.rarity.hasDescription && card.description != null) {
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            // Bottom row: card number + rarity icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${card.collectionNumber.toString().padStart(3, '0')}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Text(
                    text = card.rarity.icon,
                    fontSize = 18.sp
                )
            }
        }

        // "New!" or copy count badge
        if (isNewCard) {
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Text("NEW!", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
            }
        } else if (copyCount > 1) {
            Badge(
                containerColor = SurfaceDark,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            ) {
                Text("×$copyCount", color = Color.White, fontSize = 11.sp)
            }
        }
    }
}

// ── Summary Screen ────────────────────────────────────────────────────────────

@Composable
private fun SummaryScreen(
    cards: List<Card>,
    playerState: com.sergivm.monsterpacks.domain.model.PlayerState,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pack Summary",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(16.dp))

        // Mini card row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cards.forEach { card ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.65f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(card.type.color.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
                        Text(card.rarity.icon, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = card.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                        if (!playerState.hasCard(card.id)) {
                            Text("NEW!", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(0.7f).height(52.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text("Save", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(24.dp))
    }
}
