package com.example.radiofm.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.radiofm.R
import com.example.radiofm.RadioViewModel
import com.example.radiofm.ui.theme.AccentBlue
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayerScreen(
    viewModel: RadioViewModel,
    onBack: () -> Unit
) {
    val currentStation by viewModel.currentStation.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val sleepTimerMillis by viewModel.sleepTimerMillis.collectAsState()

    var showTimerDialog by remember { mutableStateOf(false) }

    if (currentStation == null) return
    val isFavorite = favoriteIds.contains(currentStation!!.id)

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = { Text("Sleep Timer") },
            text = {
                Column {
                    listOf(15, 30, 60, 0).forEach { mins ->
                        TextButton(
                            onClick = {
                                viewModel.setSleepTimer(if (mins == 0) null else mins)
                                showTimerDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (mins == 0) "Matikan Timer" else "$mins Menit")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDialog = false }) { Text("Batal") }
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "animations")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "rotation"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // BACKGROUND: Dynamic Glassmorphism
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(currentStation!!.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .scale(1.3f),
            contentScale = ContentScale.Crop,
            alpha = 0.4f
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = onBack,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.1f),
                    modifier = Modifier.size(42.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "PLAYING FROM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        "RADIO INDONESIA",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    )
                }
                
                IconButton(onClick = { showTimerDialog = true }, modifier = Modifier.size(42.dp)) {
                    Icon(
                        Icons.Default.Timer, 
                        contentDescription = "Sleep Timer", 
                        tint = if (sleepTimerMillis != null) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // THE CASSETTE (Polished)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .graphicsLayer {
                        rotationX = -5f // Slight 3D tilt
                    }
            ) {
                // Neon Glow behind cassette
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(200.dp)
                            .blur(40.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.3f), RoundedCornerShape(20.dp))
                    )
                }

                // Main Body
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1A1A1A),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.08f)),
                    shadowElevation = 20.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Texture Overlay (subtle)
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent, Color.Black.copy(alpha = 0.2f)))
                        ))

                        // Corner Screws
                        repeat(4) { i ->
                            val alignment = when(i) {
                                0 -> Alignment.TopStart
                                1 -> Alignment.TopEnd
                                2 -> Alignment.BottomStart
                                else -> Alignment.BottomEnd
                            }
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(8.dp)
                                    .align(alignment)
                                    .background(Color(0xFF333333), CircleShape)
                                    .border(1.dp, Color.Black, CircleShape)
                            )
                        }

                        // Artwork Label
                        Surface(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth(0.88f)
                                .fillMaxHeight(0.75f)
                                .padding(top = 10.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2D2D2D),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentStation!!.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxHeight().width(110.dp),
                                    contentScale = ContentScale.Crop
                                )
                                
                                Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                    Text(
                                        "SIDE A - HI-FI STEREO",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    
                                    // Tape Window
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .fillMaxWidth()
                                            .height(64.dp)
                                            .clip(RoundedCornerShape(32.dp))
                                            .background(Color.Black.copy(alpha = 0.8f))
                                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // The rotating reels
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(2) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .graphicsLayer { rotationZ = if (isPlaying) rotation else 0f },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_cassette_reel),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(50.dp),
                                                        tint = Color.Unspecified
                                                    )
                                                }
                                            }
                                        }
                                        // Glass Reflection
                                        Box(modifier = Modifier.fillMaxSize().background(
                                            Brush.linearGradient(
                                                0f to Color.White.copy(alpha = 0.05f),
                                                0.5f to Color.Transparent,
                                                1f to Color.White.copy(alpha = 0.05f)
                                            )
                                        ))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // INFO SECTION
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Marquee/Dynamic Title
                Text(
                    text = currentStation!!.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .basicMarquee(
                            iterations = Int.MAX_VALUE,
                            delayMillis = 2000
                        ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Digital Display Indicator
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(if (isPlaying) Color(0xFF00FF00) else Color.Red, CircleShape)
                                .let { if(isPlaying) it.blur(2.dp) else it }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isPlaying) "STEREO ONLINE" else "SIGNAL LOST",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = if (isPlaying) Color(0xFF00FF00).copy(alpha = 0.8f) else Color.Red.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // CONTROLS (PREMIUM LOOK)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {}, 
                    modifier = Modifier.size(54.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.8f))
                }

                if (isBuffering) {
                    Box(modifier = Modifier.size(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                } else {
                    Surface(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 8.dp,
                        shadowElevation = 16.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(52.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(currentStation!!.id) }, 
                    modifier = Modifier.size(54.dp).background(Color.White.copy(alpha = 0.05f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = null, 
                        tint = if (isFavorite) Color.Red else Color.White.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
