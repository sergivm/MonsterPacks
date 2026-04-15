package com.sergivm.monsterpacks.presentation.screen.main

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sergivm.monsterpacks.domain.model.Card
import com.sergivm.monsterpacks.domain.model.PlayerState
import com.sergivm.monsterpacks.domain.model.Rarity
import com.sergivm.monsterpacks.presentation.screen.MonsterPacksTopBar
import com.sergivm.monsterpacks.presentation.screen.UsernameSetupScreen
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceVariantDark
import com.sergivm.monsterpacks.presentation.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onPackOpeningStarted: () -> Unit = {},
    onPackOpeningFinished: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()

    if (state.isFirstLaunch) {
        UsernameSetupScreen(onConfirm = { viewModel.setUsername(it) })
        return
    }

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
                        playerState = state.playerState,
                        onSave = {
                            viewModel.saveSession()
                            onPackOpeningFinished()
                        }
                    )
                }
                1 -> {
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
}

// ── Pack Idle ─────────────────────────────────────────────────────────────────

@Composable
private fun PackIdleScreen(
    playerState: PlayerState,
    packName: String,
    onOpenPack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonsterPacksTopBar(playerState = playerState)

        Spacer(Modifier.weight(0.1f))

        PackCounter(
            available = playerState.availablePacks,
            max = playerState.maxPacks
        )

        Spacer(Modifier.height(24.dp))

        val infiniteTransition = rememberInfiniteTransition(label = "PackFloat")
        val offsetY by infiniteTransition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "Offset"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(0.65f)
                .graphicsLayer { translationY = offsetY }
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceDark)
                .border(2.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
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
                    text = "[ Pack Art ]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.weight(0.15f))

        val canOpen = playerState.availablePacks > 0
        Button(
            onClick = onOpenPack,
            enabled = canOpen,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
            )
        ) {
            Text(
                text = if (canOpen) "Open a Pack" else "Out of Packs",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (canOpen) Color.Black else Color.Gray
            )
        }

        Spacer(Modifier.weight(0.1f))
    }
}

@Composable
private fun PackCounter(available: Int, max: Int) {
    Surface(
        color = SurfaceVariantDark,
        shape = CircleShape,
        modifier = Modifier.padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📦", fontSize = 16.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "$available / $max",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (available > 0) Color.White else Color.Red
            )
            Text(
                text = " PACKS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
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
    playerState: PlayerState,
    onTap: () -> Unit
) {
    // Screen shake for God/Legendary
    val shakeAnim = remember { Animatable(0f) }
    LaunchedEffect(card) {
        if (card.rarity >= Rarity.LEGENDARY) {
            repeat(6) {
                shakeAnim.animateTo(if (it % 2 == 0) 10f else -10f, tween(50))
            }
            shakeAnim.animateTo(0f, tween(50))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onTap() }
            .graphicsLayer { translationX = shakeAnim.value },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        MonsterPacksTopBar(playerState = playerState)

        Spacer(Modifier.weight(0.05f))

        Text(
            text = "CARD ${cardIndex + 1} / $totalCards",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = card,
            transitionSpec = {
                val scaleIn = if (targetState.rarity >= Rarity.LEGENDARY) {
                    scaleIn(initialScale = 1.5f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow))
                } else {
                    slideInHorizontally { width -> width } + fadeIn()
                }
                
                scaleIn togetherWith (slideOutHorizontally { width -> -width } + fadeOut())
            },
            label = "CardSlide"
        ) { currentCard ->
            CardView(
                card = currentCard,
                isNewCard = isNewCard,
                copyCount = copyCount,
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .aspectRatio(0.65f)
            )
        }

        Spacer(Modifier.weight(0.1f))

        Text(
            text = "TAP TO CONTINUE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 1.sp
        )

        Spacer(Modifier.height(32.dp))
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
    val context = LocalContext.current
    val imageResId = remember(card.imageRes) {
        context.resources.getIdentifier(card.imageRes, "drawable", context.packageName)
    }

    // Shimmer animation for high rarity
    val infiniteTransition = rememberInfiniteTransition(label = "Shimmer")
    val shimmerX by infiniteTransition.animateFloat(
        initialValue = -500f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "ShimmerX"
    )

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(card.type.color.copy(alpha = 0.9f), card.type.color.copy(alpha = 0.4f))
                    )
                )
                .border(
                    width = if (card.rarity >= Rarity.SPECIAL) 3.dp else 2.dp,
                    color = card.rarity.color.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(16.dp)
                )
                .drawWithContent {
                    drawContent()
                    if (card.rarity >= Rarity.SPECIAL) {
                        // Diagonal Shimmer overlay
                        val brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                            start = androidx.compose.ui.geometry.Offset(shimmerX, shimmerX),
                            end = androidx.compose.ui.geometry.Offset(shimmerX + 150f, shimmerX + 150f)
                        )
                        drawRect(brush = brush, blendMode = BlendMode.Overlay)
                    }
                }
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = card.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = "[ Missing Art ]",
                        color = Color.White.copy(alpha = 0.3f),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (card.rarity.hasDescription && card.description != null) {
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(12.dp))
            }

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
                    fontSize = 22.sp
                )
            }
        }

        // Animated "New!" Badge
        if (isNewCard) {
            val pulseTransition = rememberInfiniteTransition(label = "NewPulse")
            val scale by pulseTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                label = "Scale"
            )

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .scale(scale)
            ) {
                Text(
                    "NEW!",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        } else if (copyCount > 1) {
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = CircleShape,
                modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
            ) {
                Text(
                    "×$copyCount",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

// ── Summary Screen ────────────────────────────────────────────────────────────

@Composable
private fun SummaryScreen(
    cards: List<Card>,
    playerState: PlayerState,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Pack Opened!",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cards.forEachIndexed { index, card ->
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(100L * index)
                    visible = true
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = scaleIn(animationSpec = tween(400)) + fadeIn(),
                    modifier = Modifier.weight(1f)
                ) {
                    SummaryCardCell(card, !playerState.hasCard(card.id))
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text("DONE", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SummaryCardCell(card: Card, isNew: Boolean) {
    val context = LocalContext.current
    val imageResId = remember(card.imageRes) {
        context.resources.getIdentifier(card.imageRes, "drawable", context.packageName)
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.65f)
            .clip(RoundedCornerShape(8.dp))
            .background(card.type.color.copy(alpha = 0.8f))
            .border(1.dp, card.rarity.color.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (imageResId != 0) {
            Image(
                painter = painterResource(id = imageResId),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.4f
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(4.dp)) {
            Text(card.rarity.icon, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = card.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                fontSize = 8.sp,
                lineHeight = 10.sp
            )
            if (isNew) {
                Text(
                    "NEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 8.sp
                )
            }
        }
    }
}
