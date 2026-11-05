package com.clubcat.player.ui

import android.app.Application
import android.content.ComponentName
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.clubcat.player.data.MusicRepository
import com.clubcat.player.data.Song
import com.clubcat.player.playback.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val songs: List<Song> = emptyList(),
    val isLoading: Boolean = false,
    val currentMediaId: String? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MusicRepository(app)

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private var controller: MediaController? = null
    private val controllerFuture: ListenableFuture<MediaController>

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            syncState(player)
        }
    }

    init {
        val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(app, token).buildAsync()
        controllerFuture.addListener(
            {
                runCatching { controllerFuture.get() }.getOrNull()?.let { c ->
                    controller = c
                    c.addListener(listener)
                    syncState(c)
                }
            },
            ContextCompat.getMainExecutor(app),
        )

        // Actualiza la barra de progreso mientras suena algo.
        viewModelScope.launch {
            while (isActive) {
                controller?.let { c ->
                    if (c.isPlaying) {
                        _state.update { it.copy(positionMs = c.currentPosition) }
                    }
                }
                delay(500)
            }
        }
    }

    private fun syncState(player: Player) {
        _state.update {
            it.copy(
                currentMediaId = player.currentMediaItem?.mediaId,
                isPlaying = player.isPlaying,
                positionMs = player.currentPosition,
                durationMs = if (player.duration == C.TIME_UNSET) 0L else player.duration,
            )
        }
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val songs = repository.loadSongs()
            _state.update { it.copy(songs = songs, isLoading = false) }
        }
    }

    /** Reproduce la biblioteca completa empezando por [song]. */
    fun play(song: Song) {
        val c = controller ?: return
        val songs = _state.value.songs
        val index = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        c.setMediaItems(songs.map { it.toMediaItem() }, index, 0L)
        c.prepare()
        c.play()
    }

    fun togglePlayPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun skipNext() {
        controller?.seekToNextMediaItem()
    }

    fun skipPrevious() {
        controller?.seekToPreviousMediaItem()
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _state.update { it.copy(positionMs = positionMs) }
    }

    override fun onCleared() {
        controller?.removeListener(listener)
        MediaController.releaseFuture(controllerFuture)
        super.onCleared()
    }

    private fun Song.toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setMediaId(id.toString())
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setAlbumTitle(album)
                    .setArtworkUri(artworkUri)
                    .build(),
            )
            .build()
}
