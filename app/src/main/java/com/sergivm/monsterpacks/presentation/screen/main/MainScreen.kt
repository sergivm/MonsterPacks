package com.sergivm.monsterpacks.presentation.screen.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.R
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import com.sergivm.monsterpacks.presentation.screen.*
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceVariantDark
import com.sergivm.monsterpacks.presentation.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onPackOpeningStarted: () -> Unit = {},
    onPackOpeningFinished: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize().background(BackgroundDark), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    if (state.isFirstLaunch) {
        UsernameSetupScreen(onConfirm = { viewModel.setUsername(it) })
        return
    }

    val playerState = state.playerState ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        AnimatedContent(
            targetState = when {
                state.showSummary -> 2
                state.isOpeningPack -> 1
                else -> 0
            },
            transitionSpec = {
                fadeIn(animationSpec = tween(500)) togetherWith fadeOut(animationSpec = tween(500))
            },
            label = "ScreenTransition"
        ) { targetIndex ->
            when (targetIndex) {
                2 -> {
                    SummaryScreen(
                        cards = state.drawnCards,
                        playerState = playerState,
                        xpReward = state.packDefinition.xpReward,
                        onSave = {
                            viewModel.saveSession()
                            onPackOpeningFinished()
                        }
                    )
                }
                1 -> {
                    state.currentCard?.let { card ->
                        CardRevealAnimationContainer(
                            card = card,
                            isNewCard = playerState.copiesOf(card.id) == 0,
                            copyCount = playerState.copiesOf(card.id),
                            cardIndex = state.currentCardIndex,
                            totalCards = state.drawnCards.size,
                            onTap = { viewModel.revealNextCard() }
                        )
                    }
                }
                else -> {
                    PackIdleScreen(
                        playerState = playerState,
                        packDefinition = state.packDefinition,
                        onOpenPack = {
                            viewModel.openPack(1)
                            onPackOpeningStarted()
                        },
                        onOpenBulk = {
                            viewModel.openPack(5)
                            onPackOpeningStarted()
                        }
                    )
                }
            }
        }
    }
}

// ── Pack Idle ─────────────────────────────────────────────────────────────────

@Composable
private fun PackIdleScreen(
    playerState: PlayerState,
    packDefinition: PackDefinition,
    onOpenPack: () -> Unit,
    onOpenBulk: () -> Unit
) {
    val context = LocalContext.current
    val coverResId = remember(packDefinition.coverRes) {
        context.resources.getIdentifier(packDefinition.coverRes, "drawable", context.packageName)
    }

    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(playerState.availablePacks, playerState.maxPacks) {
        while (playerState.availablePacks < playerState.maxPacks) {
            delay(1000)
            currentTime = System.currentTimeMillis()
        }
    }

    val regenInterval = GameEngine.getPackRegenIntervalMs(playerState.basicPackRegenLevel)
    val remainingRegenMs = playerState.nextPackRegenRemainingMs(currentTime, regenInterval)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonsterPacksTopBar(playerState = playerState)

        Spacer(Modifier.weight(0.1f))

        PackCounterWithTimer(
            available = playerState.availablePacks,
            max = playerState.maxPacks,
            remainingMs = remainingRegenMs
        )

        Spacer(Modifier.height(24.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "PackFloat")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Offset"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .aspectRatio(0.65f)
                .graphicsLayer { translationY = offsetY }
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (coverResId != 0) {
                Image(
                    painter = painterResource(id = coverResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Only show pack name overlay for non-basic packs
            if (packDefinition.type != PackType.BASIC) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = packDefinition.name.uppercase(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(0.15f))

        val canOpen = playerState.availablePacks > 0
        val canBulk = playerState.availablePacks >= 5 && playerState.isBulkOpenUnlocked

        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onOpenPack,
                enabled = canOpen,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(stringResource(R.string.open_pack), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            }

            if (playerState.isBulkOpenUnlocked) {
                OutlinedButton(
                    onClick = onOpenBulk,
                    enabled = canBulk,
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, if (canBulk) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.3f))
                ) {
                    Text(stringResource(R.string.bulk_open_x5), fontWeight = FontWeight.Bold, color = if (canBulk) MaterialTheme.colorScheme.primary else Color.Gray)
                }
            }
        }

        Spacer(Modifier.weight(0.1f))
    }
}

@Composable
private fun PackCounterWithTimer(available: Int, max: Int, remainingMs: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            color = SurfaceVariantDark,
            shape = CircleShape,
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📦", fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "$available / $max",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (available > 0) Color.White else Color.Red
                )
                Text(
                    text = " " + stringResource(R.string.packs_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
        }
        
        if (available < max && remainingMs > 0) {
            val totalSeconds = remainingMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val timeString = "${minutes}:${seconds.toString().padStart(2, '0')}"
            Text(
                text = stringResource(R.string.next_in, timeString),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp
            )
        }
    }
}
