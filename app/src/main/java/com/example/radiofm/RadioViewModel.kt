package com.example.radiofm

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.radiofm.data.RadioApi
import com.example.radiofm.data.RadioStation
import com.example.radiofm.data.DataStoreManager
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Timer
import kotlin.concurrent.schedule

class RadioViewModel : ViewModel() {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) {
            try { controllerFuture?.get() } catch (e: Exception) { null }
        } else null

    private lateinit var dataStoreManager: DataStoreManager
    
    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds = _favoriteIds.asStateFlow()

    private val _favoriteStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val favoriteStations = _favoriteStations.asStateFlow()

    private val _historyIds = MutableStateFlow<List<String>>(emptyList())
    val historyIds = _historyIds.asStateFlow()

    private val _sleepTimerMillis = MutableStateFlow<Long?>(null)
    val sleepTimerMillis = _sleepTimerMillis.asStateFlow()
    private var sleepTimerTask: Timer? = null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering = _isBuffering.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError = _lastError.asStateFlow()

    private val _currentStation = MutableStateFlow<RadioStation?>(null)
    val currentStation = _currentStation.asStateFlow()

    private val _stations = MutableStateFlow<List<RadioStation>>(emptyList())
    val stations = _stations.asStateFlow()

    private val _featuredStations = MutableStateFlow<List<RadioStation>>(emptyList())
    val featuredStations = _featuredStations.asStateFlow()

    private val stationCache = mutableMapOf<String, RadioStation>()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var lastQuery = ""

    private val api: RadioApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .header("Referer", "https://radio.garden/")
                    .build()
                chain.proceed(request)
            }
            .build()
        Retrofit.Builder()
            .baseUrl(RadioApi.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(RadioApi::class.java)
    }

    init {
        fetchStations("")
    }

    fun initPlayer(context: Context) {
        if (controllerFuture != null) return
        
        dataStoreManager = DataStoreManager(context)
        
        // Collect DataStore flows
        viewModelScope.launch {
            dataStoreManager.favoritesFlow.collect { ids ->
                _favoriteIds.value = ids
                syncFavoriteStations(ids)
            }
        }
        viewModelScope.launch {
            dataStoreManager.historyFlow.collect {
                _historyIds.value = it
            }
        }
        viewModelScope.launch {
            dataStoreManager.lastStationFlow.collect { id ->
                if (id != null && _currentStation.value == null) {
                    restoreLastStation(id)
                }
            }
        }

        // Pre-fill cache
        stations.value.forEach { stationCache[it.id] = it }

        try {
            val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
            controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
            controllerFuture?.addListener({
                setupController()
            }, MoreExecutors.directExecutor())
        } catch (e: Exception) {
            Log.e("RadioVM", "Gagal inisialisasi: ${e.message}")
        }
    }

    private suspend fun restoreLastStation(id: String) {
        val cached = stationCache[id]
        if (cached != null) {
            _currentStation.value = cached
        } else {
            try {
                val response = api.getChannelInfo(id)
                val station = RadioStation(
                    id = id,
                    name = response.data.title,
                    streamUrl = "${RadioApi.STREAM_BASE_URL}$id/channel.mp3",
                    imageUrl = "https://radio.garden/api/ara/content/channel/$id/image",
                    description = response.data.place.title
                )
                stationCache[id] = station
                _currentStation.value = station
            } catch (e: Exception) {
                Log.e("RadioVM", "Gagal mengembalikan stasiun terakhir: $id", e)
            }
        }
    }

    private suspend fun syncFavoriteStations(ids: Set<String>) {
        val currentFavorites = _favoriteStations.value.toMutableList()
        
        // Remove ones no longer in IDs
        currentFavorites.removeAll { !ids.contains(it.id) }
        
        // Add new ones
        ids.forEach { id ->
            if (currentFavorites.none { it.id == id }) {
                val cached = stationCache[id]
                if (cached != null) {
                    currentFavorites.add(cached)
                } else {
                    // Fetch from API if not in cache
                    try {
                        val response = api.getChannelInfo(id)
                        val station = RadioStation(
                            id = id,
                            name = response.data.title,
                            streamUrl = "${RadioApi.STREAM_BASE_URL}$id/channel.mp3",
                            imageUrl = "https://radio.garden/api/ara/content/channel/$id/image",
                            description = response.data.place.title
                        )
                        stationCache[id] = station
                        currentFavorites.add(station)
                    } catch (e: Exception) {
                        Log.e("RadioVM", "Error fetching favorite $id", e)
                    }
                }
            }
        }
        _favoriteStations.value = currentFavorites.toList()
    }

    fun getStationById(id: String): RadioStation? = stationCache[id]

    fun toggleFavorite(stationId: String) {
        viewModelScope.launch {
            dataStoreManager.toggleFavorite(stationId)
        }
    }

    fun setSleepTimer(minutes: Int?) {
        sleepTimerTask?.cancel()
        sleepTimerTask = null
        
        if (minutes == null) {
            _sleepTimerMillis.value = null
            return
        }

        val millis = minutes * 60 * 1000L
        _sleepTimerMillis.value = System.currentTimeMillis() + millis
        
        sleepTimerTask = Timer()
        sleepTimerTask?.schedule(millis) {
            viewModelScope.launch {
                controller?.pause()
                _sleepTimerMillis.value = null
            }
        }
    }

    private fun setupController() {
        val player = controller ?: return
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
                if (isPlaying) {
                    _isBuffering.value = false
                    _lastError.value = null
                }
            }
            
            override fun onPlaybackStateChanged(state: Int) {
                _isBuffering.value = state == Player.STATE_BUFFERING
            }
            
            override fun onPlayerError(error: PlaybackException) {
                Log.e("RadioPlayer", "Error: ${error.errorCodeName}")
                _isPlaying.value = false
                _isBuffering.value = false
                _lastError.value = "Gagal memutar stasiun ini"
            }
        })
    }

    fun fetchStations(query: String) {
        lastQuery = query
        viewModelScope.launch {
            _isLoading.value = true
            _lastError.value = null
            try {
                val searchQuery = if (query.isEmpty()) "RRI" else query
                val response = withTimeout(15000) {
                    api.searchStations(searchQuery)
                }

                val mappedStations = response.hits.hits
                    .mapNotNull { hit ->
                        val source = hit._source ?: return@mapNotNull null
                        val page = source.page ?: return@mapNotNull null
                        val url = page.url ?: return@mapNotNull null
                        
                        // Use hit._id if available, otherwise fallback to URL split
                        val id = hit._id ?: url.split("/").last()
                        
                        if (page.type == "channel") {
                            RadioStation(
                                id = id,
                                name = page.title ?: "Unknown",
                                streamUrl = "${RadioApi.STREAM_BASE_URL}$id/channel.mp3",
                                imageUrl = "https://radio.garden/api/ara/content/channel/$id/image.png", // Added .png extension
                                description = "Radio Garden"
                            ).also { stationCache[it.id] = it }
                        } else {
                            null
                        }
                    }
                
                _stations.value = mappedStations
                
                if (_featuredStations.value.isEmpty() || query.isEmpty()) {
                    _featuredStations.value = mappedStations.shuffled().take(8)
                }
            } catch (e: Exception) {
                Log.e("RadioVM", "Error fetching from Radio Garden", e)
                _lastError.value = "Gagal memuat: ${e.localizedMessage ?: "Cek koneksi"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        fetchStations(lastQuery)
    }

    fun playStation(station: RadioStation) {
        val player = controller ?: return
        
        // Add to history using DataStore
        viewModelScope.launch {
            dataStoreManager.addToHistory(station.id)
        }

        try {
            _currentStation.value = station
            _isBuffering.value = true
            _lastError.value = null
            
            val mediaItem = MediaItem.Builder()
                .setMediaId(station.id)
                .setUri(station.streamUrl)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(station.name)
                        .setArtist(station.description)
                        .build()
                )
                .build()

            player.stop()
            player.setMediaItem(mediaItem)
            player.prepare()
            player.play()
        } catch (e: Exception) {
            Log.e("RadioVM", "Error", e)
        }
    }

    fun togglePlayPause() {
        val player = controller ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    override fun onCleared() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        super.onCleared()
    }
}
