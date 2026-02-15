package com.example.radiofm

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.radiofm.ui.PlayerScreen
import com.example.radiofm.ui.RadioScreen
import com.example.radiofm.ui.theme.RadioFMTheme

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

class MainActivity : ComponentActivity() {
    private val viewModel: RadioViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission result handled
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkNotificationPermission()
        viewModel.initPlayer(this)
        
        setContent {
            RadioFMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    var showPlayer by remember { mutableStateOf(false) }

                    AnimatedContent(
                        targetState = showPlayer,
                        label = "PlayerTransition",
                        transitionSpec = {
                            if (targetState) {
                                slideInVertically(
                                    initialOffsetY = { it },
                                    animationSpec = tween(400)
                                ) togetherWith slideOutVertically(
                                    targetOffsetY = { -it / 3 }, // Parallax effect
                                    animationSpec = tween(400)
                                )
                            } else {
                                slideInVertically(
                                    initialOffsetY = { -it / 3 },
                                    animationSpec = tween(400)
                                ) togetherWith slideOutVertically(
                                    targetOffsetY = { it },
                                    animationSpec = tween(400)
                                )
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
                            RadioScreen(
                                viewModel = viewModel,
                                onPlayerClick = { showPlayer = true }
                            )
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
