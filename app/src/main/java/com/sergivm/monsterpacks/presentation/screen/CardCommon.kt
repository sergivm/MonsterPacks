package com.sergivm.monsterpacks.presentation.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.sergivm.monsterpacks.R
import com.sergivm.monsterpacks.domain.engine.GameEngine
import com.sergivm.monsterpacks.domain.model.*
import com.sergivm.monsterpacks.presentation.ui.theme.BackgroundDark
import com.sergivm.monsterpacks.presentation.ui.theme.SurfaceVariantDark
import java.util.Locale
import kotlin.random.Random

data class Particle(
    val color: Color,
    var pos: Offset = Offset(0f, 0f),
    var velocity: Offset,
    var alpha: Float = 1f,
    val radius: Float = Random.nextFloat() * 10f + 5f
) {
    fun update() {
        pos += velocity
        velocity *= 0.96f 
        alpha -= 0.015f 
    }
}

@Composable
fun NewTag(
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 12.sp,
    horizontalPadding: Dp = 8.dp,
    verticalPadding: Dp = 2.dp,
    minWidth: Dp = Dp.Unspecified
) {
    val pulseTransition = rememberInfiniteTransition(label = "NewPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "Scale"
    )

    Surface(
        color = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(4.dp),
        modifier = modifier.graphicsLayer {
            scaleX = pulseScale
            scaleY = pulseScale
        }
    ) {
        Box(
            modifier = Modifier.widthIn(min = minWidth).padding(horizontal = horizontalPadding, vertical = verticalPadding),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.new_card),
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

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
                        val brush = Brush.linearGradient(
                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.3f), Color.Transparent),
                            start = Offset(shimmerX, shimmerX),
                            end = Offset(shimmerX + 150f, shimmerX + 150f)
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
                textAlign = TextAlign.Center,
                maxLines = 1
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
                        text = stringResource(R.string.missing_art),
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
                    lineHeight = 18.sp,
                    maxLines = 2
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

        if (isNewCard) {
            NewTag(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
            )
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

@Composable
fun SummaryCardCell(card: Card, isNew: Boolean) {
    val context = LocalContext.current
    val imageResId = remember(card.imageRes) {
        context.resources.getIdentifier(card.imageRes, "drawable", context.packageName)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .aspectRatio(0.65f)
                .clip(RoundedCornerShape(6.dp))
                .background(card.type.color.copy(alpha = 0.8f))
                .border(1.dp, card.rarity.color.copy(alpha = 0.4f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)))),
                contentAlignment = Alignment.BottomCenter
            ) {
                Text(card.rarity.icon, fontSize = 14.sp, modifier = Modifier.padding(2.dp))
            }
        }
        
        if (isNew) {
            Spacer(Modifier.height(4.dp))
            NewTag(
                fontSize = 8.sp,
                horizontalPadding = 4.dp,
                verticalPadding = 1.dp,
                minWidth = 50.dp
            )
        } else {
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun CardRevealAnimationContainer(
    card: Card,
    isNewCard: Boolean,
    copyCount: Int,
    modifier: Modifier = Modifier,
    header: @Composable ColumnScope.() -> Unit,
    footer: @Composable ColumnScope.() -> Unit,
    onTap: () -> Unit
) {
    val shakeAnim = remember { Animatable(0f) }
    val particles = remember { mutableStateListOf<Particle>() }
    
    LaunchedEffect(card) {
        if (card.rarity >= Rarity.LEGENDARY) {
            repeat(6) {
                shakeAnim.animateTo(if (it % 2 == 0) 10f else -10f, tween(50))
            }
            shakeAnim.animateTo(0f, tween(50))
        }
        
        if (card.rarity >= Rarity.SPECIAL) {
            repeat(40) {
                particles.add(Particle(
                    color = card.rarity.color,
                    velocity = Offset(Random.nextFloat() * 30f - 15f, Random.nextFloat() * 30f - 15f)
                ))
            }
        }
    }

    LaunchedEffect(Unit) {
        while(true) {
            withFrameMillis { 
                val toRemove = mutableListOf<Particle>()
                particles.forEach { 
                    p -> p.update() 
                    if (p.alpha <= 0f) toRemove.add(p)
                }
                particles.removeAll(toRemove)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.radius,
                    center = Offset(size.width / 2 + p.pos.x, size.height / 2 + p.pos.y)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onTap() }
                .graphicsLayer { translationX = shakeAnim.value },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             header()

             Spacer(Modifier.weight(1f))

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
                Box(
                    modifier = Modifier.padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CardView(
                        card = currentCard,
                        isNewCard = false,
                        copyCount = copyCount,
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .aspectRatio(0.65f)
                    )

                    if (isNewCard) {
                        NewTag(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset(x = 12.dp, y = (-12).dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(1.2f))
            footer()
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryScreen(
    cards: List<Card>,
    playerState: PlayerState,
    onSave: () -> Unit,
    title: String = stringResource(R.string.pack_opened),
    actionLabel: String = stringResource(R.string.done)
) {
    val totalCoins = cards.sumOf { it.coinReward }
    val totalGems = cards.sumOf { it.gemReward }
    val xpMult = GameEngine.getXpMultiplier(playerState.xpMultiplierLevel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(top = 24.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceVariantDark)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RewardItem(icon = "🪙", amount = totalCoins)
            RewardItem(icon = "💎", amount = totalGems)
            if (xpMult > 1.0f) {
                VerticalDivider(modifier = Modifier.height(20.dp), color = Color.White.copy(alpha = 0.1f))
                Text(
                    text = "${String.format(Locale.US, "%.1f", xpMult)}x XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            itemsIndexed(cards, key = { index, card -> "${card.id}_$index" }) { _, card ->
                SummaryCardCell(card, !playerState.hasCard(card.id))
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(0.8f).height(56.dp),
            shape = RoundedCornerShape(28.dp)
        ) {
            Text(actionLabel, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun RewardItem(icon: String, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(icon, fontSize = 18.sp)
        Text(
            text = "+$amount",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
