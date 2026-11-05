package com.clubcat.player.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.clubcat.player.data.Song

/** A partir de este ancho (dp) usamos el diseño de tablet con panel dividido. */
private const val EXPANDED_WIDTH_DP = 840

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubCatApp(viewModel: PlayerViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isExpanded = LocalConfiguration.current.screenWidthDp >= EXPANDED_WIDTH_DP

    val audioPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, audioPermission) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasPermission = granted
        if (granted) viewModel.loadLibrary()
    }

    // Permiso para mostrar la notificación del reproductor (Android 13+).
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    LaunchedEffect(Unit) {
        if (hasPermission) {
            viewModel.loadLibrary()
        } else {
            permissionLauncher.launch(audioPermission)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    var showNowPlaying by remember { mutableStateOf(false) }
    val currentSong = state.songs.firstOrNull { it.id.toString() == state.currentMediaId }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val library: @Composable () -> Unit = {
        when {
            !hasPermission -> PermissionPrompt(
                onGrant = { permissionLauncher.launch(audioPermission) },
            )
            state.isLoading -> CenteredMessage {
                CircularProgressIndicator()
            }
            state.songs.isEmpty() -> CenteredMessage {
                Text(
                    "No encontramos música en tu dispositivo",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            else -> LibraryList(
                state = state,
                onSongClick = viewModel::play,
            )
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("ClubCat") },
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            // En tablet el reproductor vive en el panel lateral, no abajo.
            if (!isExpanded && currentSong != null) {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = state.isPlaying,
                    onClick = { showNowPlaying = true },
                    onPlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::skipNext,
                )
            }
        },
    ) { padding ->
        if (isExpanded) {
            Row(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            ) {
                Box(modifier = Modifier.weight(1f)) { library() }
                NowPlayingPane(
                    song = currentSong,
                    isPlaying = state.isPlaying,
                    positionMs = state.positionMs,
                    durationMs = state.durationMs,
                    onPlayPause = viewModel::togglePlayPause,
                    onNext = viewModel::skipNext,
                    onPrevious = viewModel::skipPrevious,
                    onSeek = viewModel::seekTo,
                    modifier = Modifier
                        .width(400.dp)
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 16.dp, bottom = 16.dp),
                )
            }
        } else {
            Box(modifier = Modifier.padding(padding)) { library() }
        }
    }

    if (!isExpanded && showNowPlaying && currentSong != null) {
        NowPlayingSheet(
            song = currentSong,
            isPlaying = state.isPlaying,
            positionMs = state.positionMs,
            durationMs = state.durationMs,
            onDismiss = { showNowPlaying = false },
            onPlayPause = viewModel::togglePlayPause,
            onNext = viewModel::skipNext,
            onPrevious = viewModel::skipPrevious,
            onSeek = viewModel::seekTo,
        )
    }
}

@Composable
private fun LibraryList(
    state: PlayerUiState,
    onSongClick: (Song) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(state.songs, key = { it.id }) { song ->
            SongRow(
                song = song,
                isCurrent = song.id.toString() == state.currentMediaId,
                onClick = { onSongClick(song) },
            )
        }
    }
}

@Composable
private fun PermissionPrompt(onGrant: () -> Unit) {
    CenteredMessage {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "ClubCat necesita acceso a tu música para mostrar tu biblioteca",
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(onClick = onGrant) { Text("Dar acceso") }
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        content()
    }
}
