package com.example.radiofm.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.radiofm.R
import com.example.radiofm.RadioViewModel
import com.example.radiofm.data.RadioStation
import com.example.radiofm.ui.components.BottomPlayerBar
import com.example.radiofm.ui.components.StationItem
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.ExperimentalComposeUiApi
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    currentTab: String,
    modifier: Modifier = Modifier,
    onPlayerClick: () -> Unit
) {
    val currentStation by viewModel.currentStation.collectAsState()
    val stations by viewModel.stations.collectAsState()
    val featuredStations by viewModel.featuredStations.collectAsState()
    val favoriteStations by viewModel.favoriteStations.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val (greeting, dynamicSubtitle) = remember {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT+8"))
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..10 -> "Selamat Pagi," to "Awali harimu dengan musik favorit."
            in 11..14 -> "Selamat Siang," to "Tetap semangat ditemani siaran terbaik."
            in 15..18 -> "Selamat Sore," to "Santai sejenak sebelum pulang."
            else -> "Selamat Malam," to "Waktunya istirahat dengan melodi tenang."
        }
    }

    val quote = remember {
        listOf(
            "Musik adalah bahasa universal umat manusia.",
            "Di mana kata-kata gagal, musik berbicara.",
            "Radio membawa dunia ke telingamu.",
            "Hidup adalah sebuah lagu, nyanyikanlah.",
            "Temukan ritme dalam setiap langkahmu."
        ).random()
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Column {
                    val titleText = when(currentTab) {
                        "Beranda" -> greeting
                        "Favorit" -> "Favorit Anda"
                        else -> "Stasiun Radio"
                    }
                    val subText = if (currentTab == "Beranda") dynamicSubtitle else ""

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(
                            text = titleText,
                            style = if (currentTab == "Beranda") MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant) 
                                    else MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp)
                        )
                        if (subText.isNotEmpty()) {
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = (-0.5).sp,
                                    lineHeight = 32.sp
                                )
                            )
                        }
                    }
                    Divider(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        thickness = 1.dp,
                        color = Color.White.copy(alpha = 0.05f)
                    )
                }
            }
        ) { padding ->
            PullToRefreshBox(
                isRefreshing = isLoading,
                onRefresh = { viewModel.refresh() },
                modifier = Modifier.padding(padding)
            ) {
                Crossfade(
                    targetState = currentTab,
                    animationSpec = tween(250),
                    label = "TabTransition"
                ) { targetTab ->
                    if (targetTab == "Beranda") {
                        BerandaContent(
                            stations = stations,
                            featuredStations = featuredStations,
                            isLoading = isLoading,
                            quote = quote,
                            currentStationId = currentStation?.id,
                            onMoodClick = { mood -> viewModel.fetchStations(mood) },
                            onStationClick = { viewModel.playStation(it) }
                        )
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                                shape = RoundedCornerShape(24.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Cari stasiun...") },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        viewModel.fetchStations(searchQuery)
                                        keyboardController?.hide()
                                    })
                                )
                            }

                            val filteredStations = if (targetTab == "Favorit") {
                                favoriteStations
                            } else {
                                stations
                            }

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 140.dp)
                            ) {
                                if (filteredStations.isEmpty() && !isLoading) {
                                    item {
                                        Box(Modifier.fillParentMaxSize().padding(bottom = 100.dp), Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(if (targetTab == "Favorit") "Belum ada favorit" else "Tidak ditemukan")
                                                if (targetTab == "Favorit") {
                                                    Text("Sukai stasiun untuk muncul di sini", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    items(filteredStations) { station ->
                                        StationItem(
                                            station = station,
                                            isCurrent = currentStation?.id == station.id,
                                            isFavorite = favoriteIds.contains(station.id),
                                            onFavoriteClick = { viewModel.toggleFavorite(station.id) },
                                            onClick = { viewModel.playStation(station) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BerandaContent(
    stations: List<RadioStation>,
    featuredStations: List<RadioStation>,
    isLoading: Boolean,
    quote: String,
    currentStationId: String?,
    onMoodClick: (String) -> Unit,
    onStationClick: (RadioStation) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp)
    ) {
        item {
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                ),
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        if (featuredStations.isNotEmpty()) {
            item {
                Text(
                    "Pilihan Editor",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(featuredStations) { station ->
                        HeroCard(station, currentStationId == station.id) { onStationClick(station) }
                    }
                }
            }
        }

        item {
            Text(
                "Dengarkan Sesuai Mood",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { MoodItem("Fokus", Icons.Default.MusicNote, Color(0xFF4CAF50)) { onMoodClick("Lofi") } }
                item { MoodItem("Semangat", Icons.Default.FlashOn, Color(0xFFFFC107)) { onMoodClick("Upbeat") } }
                item { MoodItem("Santai", Icons.Default.MusicNote, Color(0xFF2196F3)) { onMoodClick("Jazz") } }
                item { MoodItem("Pop", Icons.Default.LocalFireDepartment, Color(0xFFF44336)) { onMoodClick("Pop") } }
            }
        }

        if (stations.size > 5) {
            item {
                Text(
                    "Lagi Trending",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(stations.drop(5).take(8)) { station ->
                        TrendingCard(station, currentStationId == station.id) { onStationClick(station) }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp).padding(top = 24.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    "Radio di Sekitarmu",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
        
        items(stations.takeLast(5)) { station ->
            StationItem(
                station = station,
                isCurrent = currentStationId == station.id,
                isFavorite = false,
                onFavoriteClick = {},
                onClick = { onStationClick(station) }
            )
        }
    }
}

@Composable
fun HeroCard(station: RadioStation, isPlaying: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        border = if(isPlaying) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(station.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))))
            )
            Column(
                modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
            ) {
                if (isPlaying) {
                    LiveBadge()
                    Spacer(Modifier.height(4.dp))
                }
                Text(
                    station.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )
                Text(
                    "Hits terbaik saat ini",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun TrendingCard(station: RadioStation, isPlaying: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.width(120.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            Surface(
                modifier = Modifier.size(120.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = if(isPlaying) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(station.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    fallback = painterResource(id = R.drawable.ic_default_station),
                    error = painterResource(id = R.drawable.ic_default_station)
                )
            }
            if (isPlaying) {
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp)))
                SimpleVisualizer()
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            station.name,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MoodItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LiveBadge() {
    Surface(
        color = Color.Red,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            "LIVE",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
            color = Color.White
        )
    }
}

@Composable
fun SimpleVisualizer() {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(20.dp)
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "viz")
        repeat(3) { index ->
            val height by infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + index * 100),
                    repeatMode = RepeatMode.Reverse
                ), label = "height"
            )
            Box(Modifier.width(3.dp).fillMaxHeight(height).background(Color.White, CircleShape))
        }
    }
}
