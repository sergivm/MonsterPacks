package com.sergivm.monsterpacks.presentation.screen.shop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.sergivm.monsterpacks.domain.model.PackCost
import com.sergivm.monsterpacks.domain.model.Upgrade
import com.sergivm.monsterpacks.domain.model.UpgradeResult
import com.sergivm.monsterpacks.domain.model.PackDataSource
import com.sergivm.monsterpacks.presentation.screen.MonsterPacksTopBar
import com.sergivm.monsterpacks.presentation.ui.theme.*
import com.sergivm.monsterpacks.presentation.viewmodel.ShopViewModel
import com.sergivm.monsterpacks.presentation.viewmodel.MainViewModel

@Composable
fun ShopScreen(
    shopViewModel: ShopViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(), // Shared logic to trigger pack opening
    onNavigateToPacks: () -> Unit = {}
) {
    val state by shopViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        MonsterPacksTopBar(playerState = state.playerState)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SHOP",
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
                    onClaim = { 
                        shopViewModel.claimFreePack()
                        mainViewModel.openPack(PackDataSource.freePack)
                        onNavigateToPacks()
                    }
                )
            }

            item {
                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    "UPGRADES",
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
            }
        }
    }

    state.purchaseResult?.let { result ->
        LaunchedEffect(result) {
            shopViewModel.clearPurchaseResult()
        }
    }
}

@Composable
private fun FreePackRow(
    isReady: Boolean,
    cooldownRemainingMs: Long,
    onClaim: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Free Pack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (isReady) {
                Text("Ready to claim!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            } else {
                val totalSeconds = cooldownRemainingMs / 1000
                val totalMinutes = totalSeconds / 60
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                val seconds = totalSeconds % 60
                
                val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}:${seconds.toString().padStart(2, '0')}"
                Text(
                    "Ready in $timeText",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
        Button(
            onClick = onClaim,
            enabled = isReady,
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (isReady) "Claim!" else "Wait", fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariantDark)
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
                    "Requires Level ${upgrade.requiredLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        if (upgrade.isMaxTier) {
            Text("MAX", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
        } else {
            Button(
                onClick = onPurchase,
                enabled = !levelLocked,
                shape = RoundedCornerShape(8.dp),
                colors = buttonColor,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("UPGRADE", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        formatCost(upgrade.cost),
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = if (!levelLocked && !hasEnough) Color.White.copy(alpha = 0.8f) else Color.Unspecified
                    )
                }
            }
        }
    }
}

private fun formatCost(cost: PackCost): String = when (cost) {
    is PackCost.Coins -> "🪙 ${cost.amount}"
    is PackCost.Gems  -> "💎 ${cost.amount}"
    is PackCost.Both  -> "🪙 ${cost.coins}  💎 ${cost.gems}"
}
