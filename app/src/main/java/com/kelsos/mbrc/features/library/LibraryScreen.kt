package com.kelsos.mbrc.features.library

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Checkbox
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.Meta
import com.kelsos.mbrc.common.state.domain.PlayerState
import com.kelsos.mbrc.common.state.models.PlayingPosition
import com.kelsos.mbrc.common.ui.RemoteTopAppBar
import com.kelsos.mbrc.features.library.presentation.LibraryActions
import com.kelsos.mbrc.features.library.presentation.LibraryState
import com.kelsos.mbrc.features.library.presentation.LibraryViewModel
import com.kelsos.mbrc.features.library.sync.LibrarySyncProgress
import com.kelsos.mbrc.features.library.sync.SyncCategory
import com.kelsos.mbrc.features.library.sync.SyncMetricsDialog
import com.kelsos.mbrc.features.minicontrol.MiniControl
import com.kelsos.mbrc.features.minicontrol.MiniControlState
import com.kelsos.mbrc.features.minicontrol.MiniControlViewModel
import com.kelsos.mbrc.features.queue.Queue
import com.kelsos.mbrc.theme.Accent
import com.kelsos.mbrc.theme.DarkBackground
import com.kelsos.mbrc.theme.RemoteTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel

interface LibraryNavigator {
  fun navigateToGenreArtists(id: Long)
  fun navigateToArtistAlbums(id: Long)
  fun navigateToAlbumTracks(id: Long)
}

@Composable
fun LibraryScreen(
  openDrawer: () -> Unit,
  coroutineScope: CoroutineScope,
  libraryNavigator: LibraryNavigator,
  vm: LibraryViewModel = getViewModel(),
  navigateToHome: () -> Unit,
) {
  val miniVm = getViewModel<MiniControlViewModel>()
  val vmState by miniVm.state.collectAsState(initial = MiniControlState())

  LibraryScreen(
    openDrawer = openDrawer,
    actions = vm.actions,
    state = vm.state,
    libraryNavigator = libraryNavigator,
    coroutineScope = coroutineScope
  ) {
    MiniControl(
      vmState = vmState,
      perform = { miniVm.perform(it) },
      navigateToHome = navigateToHome
    )
  }
}

@Composable
fun LibrarySyncIndicator(syncProgress: Flow<LibrarySyncProgress>) {
  val progress by syncProgress.collectAsState(initial = LibrarySyncProgress.empty())
  LibrarySyncIndicator(syncProgress = progress)
}

@Composable
fun LibrarySyncIndicator(syncProgress: LibrarySyncProgress) = Surface(
  color = DarkBackground,
) {
  if (!syncProgress.running) {
    return@Surface
  }
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(26.dp)
  ) {
    val category = when (syncProgress.category) {
      SyncCategory.GENRES -> R.string.library__category_genres
      SyncCategory.ARTISTS -> R.string.library__category_artists
      SyncCategory.ALBUMS -> R.string.library__category_albums
      SyncCategory.TRACKS -> R.string.library__category_tracks
      SyncCategory.PLAYLISTS -> R.string.library__category_playlists
      SyncCategory.COVERS -> R.string.library__category_covers
      else -> R.string.media__genres
    }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
      Text(
        text = stringResource(
          id = R.string.library_container__sync_text,
          syncProgress.current,
          syncProgress.total,
          stringResource(id = category)
        ),
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.weight(1f, true),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
      )
      val currentProgress = syncProgress.float()
      val animatedProgress = animateFloatAsState(
        targetValue = currentProgress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
      ).value
      LinearProgressIndicator(
        progress = animatedProgress,
        modifier = Modifier.fillMaxWidth(),
        color = Accent
      )
    }
  }
}

@Preview
@Composable
fun LibrarySyncIndicatorPreview() {
  LibrarySyncIndicator(syncProgress = LibrarySyncProgress(12000, 120000, 2, true))
}

@Composable
fun SyncButton(sync: () -> Unit) {
  IconButton(onClick = { sync() }) {
    Icon(
      imageVector = Icons.Filled.Refresh,
      contentDescription = stringResource(id = R.string.press_to_sync)
    )
  }
}

@Composable
fun LibraryScreenOverflow(
  state: Flow<LibraryState>,
  setAlbumArtistOnly: (Boolean) -> Unit,
) {
  var showMenu by remember { mutableStateOf(false) }
  var showMetricsDialog by remember { mutableStateOf(false) }
  val viewState by state.collectAsState(LibraryState())
  IconButton(onClick = { showMenu = !showMenu }) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      contentDescription = stringResource(id = R.string.press_to_sync)
    )
  }
  DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
    val albumArtistsOnly = viewState.albumArtistOnly

    DropdownMenuItem(onClick = { setAlbumArtistOnly(!albumArtistsOnly) }) {
      Checkbox(
        checked = albumArtistsOnly, onCheckedChange = setAlbumArtistOnly
      )
      Text(
        text = stringResource(id = R.string.library__action_only_album_artists),
        modifier = Modifier.padding(start = 16.dp)
      )
    }

    DropdownMenuItem(onClick = {
      showMenu = false
      showMetricsDialog = true
    }) {
      Text(
        text = stringResource(id = R.string.library_menu__sync_state)
      )
    }
  }
  SyncMetricsDialog(
    metrics = viewState.syncState,
    showDialog = showMetricsDialog
  ) { showMetricsDialog = false }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun LibraryScreen(
  openDrawer: () -> Unit,
  actions: LibraryActions? = null,
  state: Flow<LibraryState>,
  libraryNavigator: LibraryNavigator,
  coroutineScope: CoroutineScope,
  content: @Composable () -> Unit
) = Surface {
  val tabs = listOf(
    R.string.media__genres,
    R.string.media__artists,
    R.string.media__albums,
    R.string.media__tracks
  )

  val progress by state.collectAsState(initial = LibraryState())
  val pagerState = rememberPagerState()

  val sync: () -> Unit = { if (actions != null) actions::refresh.invoke() }

  val albumArtistOnly: (it: Boolean) -> Unit =
    { if (actions != null) actions::setAlbumArtistOnly.invoke(it) }

  val action = { id: Long, meta: Meta, action: Queue ->
    if (meta != Meta.Track && action == Queue.Default) {
      when (meta) {
        Meta.Genre -> libraryNavigator.navigateToGenreArtists(id)
        Meta.Album -> libraryNavigator.navigateToAlbumTracks(id)
        Meta.Artist -> libraryNavigator.navigateToArtistAlbums(id)
        else -> {}
      }
    } else {
      actions?.queue(id, meta, action)
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    RemoteTopAppBar(
      openDrawer = openDrawer,
      title = stringResource(id = R.string.nav_library)
    ) {
      Row {
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(id = R.string.library_search_hint)
          )
        }
        SyncButton(sync = sync)
        LibraryScreenOverflow(state, albumArtistOnly)
      }
    }
    LibraryTabs(pagerState, tabs, coroutineScope)
    LibrarySyncIndicator(syncProgress = progress.syncProgress)
    HorizontalPager(
      modifier = Modifier.weight(1f),
      state = pagerState,
      count = tabs.size
    ) { page ->
      when (page) {
        Pages.GENRES -> GenresScreen(sync) { queue, id -> action(id, Meta.Genre, queue) }
        Pages.ARTISTS -> ArtistsScreen(sync) { queue, id -> action(id, Meta.Artist, queue) }
        Pages.ALBUMS -> AlbumsScreen(sync) { queue, id -> action(id, Meta.Album, queue) }
        Pages.TRACKS -> TracksScreen(sync) { queue, id -> action(id, Meta.Track, queue) }
      }
    }
    Row {
      content()
    }
  }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun LibraryTabs(
  pagerState: PagerState,
  tabs: List<Int>,
  coroutineScope: CoroutineScope,
) {
  TabRow(
    selectedTabIndex = pagerState.currentPage,
    indicator = { tabPositions ->
      TabRowDefaults.Indicator(
        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
      )
    }
  ) {
    tabs.forEachIndexed { index, titleId ->
      Tab(
        text = { Text(text = stringResource(id = titleId)) },
        selected = pagerState.currentPage == index,
        onClick = {
          coroutineScope.launch {
            pagerState.scrollToPage(index)
          }
        }
      )
    }
  }
}

object Pages {
  const val GENRES = 0
  const val ARTISTS = 1
  const val ALBUMS = 2
  const val TRACKS = 3
}

@Preview(device = Devices.PIXEL_4)
@Composable
fun LibraryScreenPreview() {
  RemoteTheme {
    LibraryScreen(
      openDrawer = { },
      state = flowOf(LibraryState()),
      libraryNavigator = object :
        LibraryNavigator {
        override fun navigateToGenreArtists(id: Long) = Unit
        override fun navigateToArtistAlbums(id: Long) = Unit
        override fun navigateToAlbumTracks(id: Long) = Unit
      },
      coroutineScope = MainScope(),
    ) {
      MiniControl(
        vmState = MiniControlState(
          playingTrack = PlayingTrack(
            artist = "Caravan Palace",
            album = "Panic",
            title = "Rock It for Me",
            year = "2008"
          ),
          playingPosition = PlayingPosition(63000, 174000),
          playingState = PlayerState.Playing
        ),
        perform = {},
        navigateToHome = {}
      )
    }
  }
}
