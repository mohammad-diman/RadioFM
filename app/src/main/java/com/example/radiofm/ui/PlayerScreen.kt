package com.example.radiofm.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.radiofm.R
import com.example.radiofm.RadioViewModel

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
    val nowPlaying by viewModel.nowPlaying.collectAsState()

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

    val bassIntensity = if (isPlaying) 0.5f else 0f // Static pulse since visualizer is removed
    val pulseScale by animateFloatAsState(
        targetValue = 1f + (bassIntensity * 0.05f),
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // BACKGROUND BLUR
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
                        "RADIO GARDEN",
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

            // PREMIUM CASSETTE TAPE
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .scale(pulseScale)
                    .graphicsLayer {
                        rotationX = -8f 
                        cameraDistance = 12f
                    }
            ) {
                // Neon Glow Background
                if (isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(180.dp)
                            .blur(50.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = glowAlpha * 0.25f), RoundedCornerShape(30.dp))
                    )
                }

                // Cassette Main Body
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(225.dp),
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF121212),
                    border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.1f)),
                    shadowElevation = 24.dp
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Grain Texture & Reflection Overlay
                        Box(modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.03f), Color.Transparent, Color.Black.copy(alpha = 0.3f)))
                        ))
                        
                        // Small mechanical holes
                        Row(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(40.dp)
                        ) {
                            repeat(3) { Box(modifier = Modifier.size(10.dp).background(Color.Black, CircleShape)) }
                        }

                        // Sticker Label
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth(0.92f)
                                .fillMaxHeight(0.78f)
                                .padding(top = 12.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF222222),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                // Artwork Label
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(currentStation!!.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxHeight().width(115.dp).clip(RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Text Info on Label
                                Box(modifier = Modifier.fillMaxSize().background(
                                    Brush.horizontalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Transparent))
                                ).padding(12.dp)) {
                                    Column {
                                        Text(
                                            "TAPE SIDE A",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.3f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = nowPlaying ?: currentStation!!.name,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp, 
                                                    fontWeight = FontWeight.Black,
                                                    letterSpacing = 0.5.sp
                                                ),
                                                color = Color.White,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            "MAXELL HI-FI 90",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 7.sp),
                                            color = Color.White.copy(alpha = 0.3f)
                                        )
                                    }
                                    
                                    // The Tape Window
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .height(68.dp)
                                            .padding(bottom = 4.dp)
                                            .clip(RoundedCornerShape(34.dp))
                                            .background(Color.Black.copy(alpha = 0.85f))
                                            .border(1.2.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(34.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Reels
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            repeat(2) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .graphicsLayer { rotationZ = if (isPlaying) rotation else 0f },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.ic_cassette_reel),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(52.dp),
                                                        tint = Color.Unspecified
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Glass Shine
                                        Box(modifier = Modifier.fillMaxSize().background(
                                            Brush.linearGradient(
                                                0f to Color.White.copy(alpha = 0.08f),
                                                0.4f to Color.Transparent,
                                                0.6f to Color.Transparent,
                                                1f to Color.White.copy(alpha = 0.08f)
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

            // INFO SECTION (Title & Status)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = nowPlaying ?: currentStation!!.name,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).basicMarquee(),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.1f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(if (isPlaying) Color(0xFF00FF00) else Color.Gray, CircleShape)
                                .drawBehind {
                                    if(isPlaying) {
                                        drawCircle(Color(0xFF00FF00), radius = size.minDimension * 1.5f, alpha = 0.3f)
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isPlaying) "RADIO ONLINE" else "RADIO OFFLINE",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            color = if (isPlaying) Color(0xFF00FF00).copy(alpha = 0.9f) else Color.Gray.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.8f))

            // MAIN CONTROLS
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = {}, 
                    modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.06f), CircleShape)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, tint = Color.White.copy(alpha = 0.9f))
                }

                if (isBuffering) {
                    Box(modifier = Modifier.size(105.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(68.dp)
                        )
                    }
                } else {
                    Surface(
                        onClick = { viewModel.togglePlayPause() },
                        modifier = Modifier.size(105.dp),
                        shape = CircleShape,
                        color = Color.White,
                        tonalElevation = 12.dp,
                        shadowElevation = 20.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = Color.Black
                            )
                        }
                    }
                }

                IconButton(
                    onClick = { viewModel.toggleFavorite(currentStation!!.id) }, 
                    modifier = Modifier.size(56.dp).background(Color.White.copy(alpha = 0.06f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, 
                        contentDescription = "Bookmark", 
                        tint = if (isFavorite) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
