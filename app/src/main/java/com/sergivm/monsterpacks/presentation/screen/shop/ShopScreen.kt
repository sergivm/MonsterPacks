package com.sergivm.monsterpacks.presentation.screen.shop

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.R
import com.sergivm.monsterpacks.domain.model.PackCost
import com.sergivm.monsterpacks.domain.model.Upgrade
import com.sergivm.monsterpacks.presentation.screen.*
import com.sergivm.monsterpacks.presentation.ui.theme.*
import com.sergivm.monsterpacks.presentation.viewmodel.ShopViewModel

@Composable
fun ShopScreen(
    shopViewModel: ShopViewModel = hiltViewModel(),
    onNavigateToPacks: () -> Unit = {}
) {
    val state by shopViewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(BackgroundDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MonsterPacksTopBar(playerState = state.playerState)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.shop_title),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    FreePackRow(
                        isReady = state.isFreePackReady,
                        cooldownRemainingMs = state.freePackCooldownRemainingMs,
                        onClaim = { shopViewModel.claimFreePack() }
                    )
                }

                item {
                    HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        stringResource(R.string.shop_upgrades),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                items(state.upgrades, key = { it.id }) { upgrade ->
                    UpgradeRow(
                        upgrade = upgrade,
                        playerLevel = state.playerState.level,
                        playerCoins = state.playerState.coins,
                        playerGems = state.playerState.gems,
                        onPurchase = { shopViewModel.purchaseUpgrade(upgrade) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.White.copy(alpha = 0.05f)
                    )
                }
            }
        }

        // Overlay for Free Pack Opening
        AnimatedVisibility(
            visible = state.isOpeningFreePack,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            FreePackRevealView(
                cards = state.drawnCards,
                currentIndex = state.currentCardIndex,
                showSummary = state.showSummary,
                playerState = state.playerState,
                onNext = { shopViewModel.nextFreeCard() },
                onFinish = { shopViewModel.finishFreePack() }
            )
        }
    }

    state.purchaseResult?.let { result ->
        LaunchedEffect(result) {
            shopViewModel.clearPurchaseResult()
        }
    }
}

// ── Free Pack Opening View ───────────────────────────────────────────────────

@Composable
private fun FreePackRevealView(
    cards: List<com.sergivm.monsterpacks.domain.model.Card>,
    currentIndex: Int,
    showSummary: Boolean,
    playerState: com.sergivm.monsterpacks.domain.model.PlayerState,
    onNext: () -> Unit,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
    ) {
        if (!showSummary) {
            val currentCard = cards.getOrNull(currentIndex)
            if (currentCard != null) {
                CardRevealAnimationContainer(
                    card = currentCard,
                    isNewCard = !playerState.hasCard(currentCard.id),
                    copyCount = playerState.copiesOf(currentCard.id),
                    cardIndex = currentIndex,
                    totalCards = cards.size,
                    onTap = onNext
                )
            }
        } else {
            SummaryScreen(
                cards = cards,
                playerState = playerState,
                title = stringResource(R.string.pack_summary),
                xpReward = 5,
                onSave = onFinish
            )
        }
    }
}

// ── Components ────────────────────────────────────────────────────────────────

@Composable
private fun FreePackRow(
    isReady: Boolean,
    cooldownRemainingMs: Long,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.shop_free_pack), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (isReady) {
                Text(stringResource(R.string.shop_ready_to_claim), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            } else {
                val totalSeconds = cooldownRemainingMs / 1000
                val minutes = (totalSeconds / 60) % 60
                val seconds = totalSeconds % 60
                val timeText = "${minutes}:${seconds.toString().padStart(2, '0')}"
                Text(
                    stringResource(R.string.shop_ready_in, timeText),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Button(
            onClick = onClaim,
            enabled = isReady
        ) {
            Text(if (isReady) stringResource(R.string.shop_claim) else stringResource(R.string.shop_wait), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UpgradeRow(
    upgrade: Upgrade,
    playerLevel: Int,
    playerCoins: Long,
    playerGems: Long,
    onPurchase: () -> Unit
) {
    val levelLocked = playerLevel < upgrade.requiredLevel
    
    val hasEnough = when (val cost = upgrade.cost) {
        is PackCost.Coins -> playerCoins >= cost.amount
        is PackCost.Gems  -> playerGems >= cost.amount
        is PackCost.Both  -> playerCoins >= cost.coins && playerGems >= cost.gems
    }

    val alpha = if (levelLocked) 0.4f else 1f
    val buttonColor = if (!levelLocked && !hasEnough) {
        ButtonDefaults.buttonColors(containerColor = Color(0xFF922B21), contentColor = Color.White)
    } else {
        ButtonDefaults.buttonColors()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                upgrade.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${upgrade.currentValue}  →  ${upgrade.nextValue}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
            )
            if (levelLocked) {
                Text(
                    stringResource(R.string.shop_level_required, upgrade.requiredLevel),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Box(
            modifier = Modifier.width(110.dp),
            contentAlignment = Alignment.Center
        ) {
            if (upgrade.isMaxTier) {
                Text(
                    stringResource(R.string.shop_max),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold
                )
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = !levelLocked,
                    colors = buttonColor,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            stringResource(R.string.shop_upgrade_button), 
                            fontWeight = FontWeight.ExtraBold, 
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                        Text(
                            formatCost(upgrade.cost),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private fun formatCost(cost: PackCost): String = when (cost) {
    is PackCost.Coins -> "🪙 ${cost.amount}"
    is PackCost.Gems  -> "💎 ${cost.amount}"
    is PackCost.Both  -> "🪙${cost.coins} 💎${cost.gems}"
}
