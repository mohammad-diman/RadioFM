package com.example.radiofm.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.example.radiofm.R
import com.example.radiofm.RadioViewModel
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

    val categories = listOf("Trending", "News", "Pop", "Dangdut")

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Header: Fixed content to avoid animation lag
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
            // Crossfade is the most performance-efficient transition
            Crossfade(
                targetState = currentTab,
                modifier = Modifier.padding(padding),
                animationSpec = tween(250),
                label = "TabTransition"
            ) { targetTab ->
                if (targetTab == "Beranda") {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp)
                    ) {
                        // Beranda content removed as requested
                    }
                } else {
                    // List view for Favorit and Stasiun
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

                        if (isLoading) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 120.dp)
                            ) {
                                if (filteredStations.isEmpty()) {
                                    item {
                                        Box(Modifier.fillParentMaxSize().padding(bottom = 100.dp), Alignment.Center) {
                                            Text(if (targetTab == "Favorit") "Belum ada favorit" else "Tidak ditemukan")
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
