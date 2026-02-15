package com.example.radiofm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import com.example.radiofm.ui.PlayerScreen
import com.example.radiofm.ui.RadioScreen
import com.example.radiofm.ui.theme.RadioFMTheme
import com.example.radiofm.ui.components.CustomBottomNavigation
import com.example.radiofm.ui.components.BottomPlayerBar

class MainActivity : ComponentActivity() {
    private val viewModel: RadioViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()
        viewModel.initPlayer(this)
        
        setContent {
            RadioFMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var showPlayer by remember { mutableStateOf(false) }
                    var currentTab by remember { mutableStateOf("Beranda") }
                    
                    val currentStation by viewModel.currentStation.collectAsState()
                    val isPlaying by viewModel.isPlaying.collectAsState()
                    val isBuffering by viewModel.isBuffering.collectAsState()
                    val lastError by viewModel.lastError.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = showPlayer,
                            label = "PlayerTransition",
                            transitionSpec = {
                                val animSpec = tween<Float>(350, easing = FastOutSlowInEasing)
                                val slideSpec = tween<IntOffset>(350, easing = FastOutSlowInEasing)
                                
                                if (targetState) {
                                    (slideInVertically(initialOffsetY = { it }, animationSpec = slideSpec) + fadeIn(animationSpec = animSpec)) togetherWith 
                                    (fadeOut(animationSpec = animSpec) + scaleOut(targetScale = 0.9f, animationSpec = animSpec))
                                } else {
                                    (fadeIn(animationSpec = animSpec) + scaleIn(initialScale = 0.9f, animationSpec = animSpec)) togetherWith 
                                    (slideOutVertically(targetOffsetY = { it }, animationSpec = slideSpec) + fadeOut(animationSpec = animSpec))
                                }
                            }
                        ) { isPlayerVisible ->
                            if (isPlayerVisible) {
                                PlayerScreen(
                                    viewModel = viewModel,
                                    onBack = { showPlayer = false }
                                )
                                BackHandler { showPlayer = false }
                            } else {
                                Scaffold(
                                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                    bottomBar = {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .navigationBarsPadding()
                                                .padding(bottom = 8.dp), // Lowered from 16.dp
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            // 1. MINI PLAYER FLOATING
                                            AnimatedVisibility(
                                                visible = currentStation != null,
                                                enter = slideInVertically { it / 2 } + fadeIn(),
                                                exit = slideOutVertically { it / 2 } + fadeOut()
                                            ) {
                                                BottomPlayerBar(
                                                    station = currentStation!!,
                                                    isPlaying = isPlaying,
                                                    isBuffering = isBuffering,
                                                    error = lastError,
                                                    onTogglePlay = { viewModel.togglePlayPause() },
                                                    onClick = { showPlayer = true }
                                                )
                                            }

                                            // 2. BOTTOM NAV PILL
                                            CustomBottomNavigation(
                                                currentTab = currentTab,
                                                onTabSelected = { currentTab = it }
                                            )
                                        }
                                    }
                                ) { padding ->
                                    RadioScreen(
                                        viewModel = viewModel,
                                        currentTab = currentTab,
                                        modifier = Modifier.padding(bottom = padding.calculateBottomPadding()),
                                        onPlayerClick = { showPlayer = true }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
