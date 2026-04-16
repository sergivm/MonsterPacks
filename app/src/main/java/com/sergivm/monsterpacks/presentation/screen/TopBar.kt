package com.sergivm.monsterpacks.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sergivm.monsterpacks.R
import com.sergivm.monsterpacks.domain.model.LevelSystem
import com.sergivm.monsterpacks.domain.model.PlayerState
import com.sergivm.monsterpacks.presentation.ui.theme.CoinColor
import com.sergivm.monsterpacks.presentation.ui.theme.GemColor
import com.sergivm.monsterpacks.presentation.ui.theme.OnSurfaceVariant

/**
 * Global top bar shown across Pack, Collection, and Shop screens.
 * Displays: username | level + XP progress | coins | gems
 */
@Composable
fun MonsterPacksTopBar(
    playerState: PlayerState,
    modifier: Modifier = Modifier
) {
    val xpNeeded = LevelSystem.xpNeededForNextLevel(playerState.level)
    val xpProgress = LevelSystem.xpProgressInLevel(playerState.xp, playerState.level)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Username
        Text(
            text = playerState.username,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        // Level + XP
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.topbar_level, playerState.level),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (xpNeeded != null) "$xpProgress/${xpNeeded}" else stringResource(R.string.topbar_max),
                style = MaterialTheme.typography.labelSmall,
                color = OnSurfaceVariant,
                fontSize = 10.sp
            )
        }

        // Coins
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("🪙", fontSize = 16.sp)
            Text(
                text = formatResource(playerState.coins),
                style = MaterialTheme.typography.labelSmall,
                color = CoinColor,
                fontWeight = FontWeight.Bold
            )
        }

        // Gems
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("💎", fontSize = 16.sp)
            Text(
                text = formatResource(playerState.gems),
                style = MaterialTheme.typography.labelSmall,
                color = GemColor,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatResource(value: Long): String = when {
    value >= 1_000_000 -> "${value / 1_000_000}M"
    value >= 1_000     -> "${value / 1_000}K"
    else               -> value.toString()
}
