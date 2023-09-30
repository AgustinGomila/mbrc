package com.kelsos.mbrc.features.nowplaying

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.state.domain.PlayerState
import com.kelsos.mbrc.common.state.models.PlayingPosition
import com.kelsos.mbrc.common.ui.EmptyScreen
import com.kelsos.mbrc.common.ui.NowPlayingRow
import com.kelsos.mbrc.common.ui.RemoteTopAppBar
import com.kelsos.mbrc.common.ui.SwipeRefreshDragableScreen
import com.kelsos.mbrc.common.ui.SwipeRefreshScreen
import com.kelsos.mbrc.common.ui.SwipeScreenContent
import com.kelsos.mbrc.features.library.PlayingTrack
import com.kelsos.mbrc.features.minicontrol.MiniControl
import com.kelsos.mbrc.features.minicontrol.MiniControlState
import com.kelsos.mbrc.features.minicontrol.MiniControlViewModel
import com.kelsos.mbrc.features.nowplaying.domain.NowPlaying
import com.kelsos.mbrc.features.nowplaying.presentation.INowPlayingActions
import com.kelsos.mbrc.features.nowplaying.presentation.NowPlayingUiMessages
import com.kelsos.mbrc.features.nowplaying.presentation.NowPlayingViewModel
import com.kelsos.mbrc.theme.RemoteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.androidx.compose.getViewModel

@Composable
fun NowPlayingScreen(
  snackbarHostState: SnackbarHostState,
  openDrawer: () -> Unit,
  navigateToHome: () -> Unit,
) {
  val vm = getViewModel<NowPlayingViewModel>()
  val miniVm = getViewModel<MiniControlViewModel>()
  val vmState by miniVm.state.collectAsState(initial = MiniControlState())

  NowPlayingScreen(
    snackbarHostState = snackbarHostState,
    nowPlaying = vm.list.collectAsLazyPagingItems(),
    playingTrack = vm.playingTracks,
    actions = vm.actions,
    events = vm.emitter,
    openDrawer = openDrawer
  ) {
    MiniControl(
      vmState = vmState,
      perform = { miniVm.perform(it) },
      navigateToHome = navigateToHome
    )
  }
}

@Composable
fun NowPlayingScreen(
  snackbarHostState: SnackbarHostState,
  nowPlaying: LazyPagingItems<NowPlaying>,
  playingTrack: Flow<PlayingTrack>,
  actions: INowPlayingActions,
  events: Flow<NowPlayingUiMessages>,
  openDrawer: () -> Unit,
  content: @Composable () -> Unit
) = Surface {

  val messages = mapOf(
    NowPlayingUiMessages.RefreshSuccess to stringResource(id = R.string.playlists__refresh_success),
    NowPlayingUiMessages.RefreshFailed to stringResource(id = R.string.playlists__refresh_failed)
  )

  LaunchedEffect(snackbarHostState) {
    events.collect { message ->
      snackbarHostState.showSnackbar(messages.getValue(message))
    }
  }

  val track = playingTrack.collectAsState(initial = PlayingTrack()).value

  val isRefreshing = nowPlaying.loadState.refresh is LoadState.Loading

  var isDragIconLongPressed by remember { mutableStateOf(false) }

  Column(modifier = Modifier.fillMaxSize()) {
    RemoteTopAppBar(
      openDrawer = openDrawer,
      title = stringResource(id = R.string.nav_now_playing)
    ) {
      Row {
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(id = R.string.library_search_hint)
          )
        }
      }
    }

    if (nowPlaying.itemCount == 0) {
      EmptyScreen(
        modifier = Modifier.weight(1f),
        text = stringResource(id = R.string.playlists_list_empty),
        imageVector = Icons.Filled.QueueMusic,
        contentDescription = stringResource(id = R.string.playlists_list_empty)
      ) {
        TextButton(onClick = { actions.reload() }) {
          Text(text = stringResource(id = R.string.press_to_sync))
        }
      }
    } else {
      SwipeRefreshDragableScreen(
        modifier = Modifier.weight(1f),
        content = SwipeScreenContent(
          items = nowPlaying,
          isRefreshing = isRefreshing,
          key = { it.id },
          onRefresh = actions::reload
        ),
        isLongPressed = isDragIconLongPressed
      ) {
        if (it != null) {
          NowPlayingRow(
            nowPlaying = it,
            isCurrent = track.path == it.path,
            isDragIconPressed = { it2 -> isDragIconLongPressed = it2 },
            actions = actions
          )
        }
      }
    }

    Row {
      content()
    }
  }
}

@Preview(device = Devices.PIXEL_4)
@Composable
fun NowPlayingScreenPreview() {
  RemoteTheme {
    NowPlayingScreen(
      snackbarHostState = SnackbarHostState(),
      nowPlaying = createDummyLazyPagingItems(previewNowPlayingList),
      playingTrack = previewPlaylist.asFlow(),
      actions = object : INowPlayingActions {
        override fun reload() {}
        override fun search(query: String) {}
        override fun moveTrack(from: Int, to: Int) {}
        override fun removeTrack(position: Int) {}
        override fun play(position: Int) {}
        override fun move() {}
      },
      events = emptyFlow(),
      openDrawer = {},
    ) {
      MiniControl(
        vmState = MiniControlState(
          playingTrack = previewPlaylist.first(),
          playingPosition = PlayingPosition(63000, 174000),
          playingState = PlayerState.Playing
        ),
        perform = {},
        navigateToHome = {}
      )
    }
  }
}

@Composable
fun createDummyLazyPagingItems(nowPlayingList: List<NowPlaying>): LazyPagingItems<NowPlaying> {
  val pager = remember {
    Pager(
      config = PagingConfig(pageSize = 20),
      pagingSourceFactory = { NowPlayingPagingSource(nowPlayingList) }
    )
  }
  return pager.flow.collectAsLazyPagingItems()
}

val previewNowPlayingList = mutableListOf(
  NowPlaying(
    title = "Rock It for Me",
    artist = "Caravan Palace",
    path = "path1",
    position = 0,
    id = 1
  ),
  NowPlaying(
    title = "Get Lucky",
    artist = "Daft Punk",
    path = "path2",
    position = 0,
    id = 2
  ),
  NowPlaying(
    title = "Come Together",
    artist = "The Beatles",
    path = "path3",
    position = 0,
    id = 3
  ),
  NowPlaying(
    title = "Bohemian Rhapsody",
    artist = "Queen",
    path = "path4",
    position = 0,
    id = 4
  ),
  NowPlaying(
    title = "Comfortably Numb",
    artist = "Pink Floyd",
    path = "path5",
    position = 0,
    id = 5
  ),
  NowPlaying(
    title = "Stairway to Heaven",
    artist = "Led Zeppelin",
    path = "path6",
    position = 0,
    id = 6
  ),
  NowPlaying(
    title = "Smells Like Teen Spirit",
    artist = "Nirvana",
    path = "path7",
    position = 0,
    id = 7
  ),
  NowPlaying(
    title = "Billie Jean",
    artist = "Michael Jackson",
    path = "path8",
    position = 0,
    id = 8
  ),
  NowPlaying(
    title = "Paranoid Android",
    artist = "Radiohead",
    path = "path9",
    position = 0,
    id = 9
  ),
  NowPlaying(
    title = "With or Without You",
    artist = "U2",
    path = "path10",
    position = 0,
    id = 10
  ),
  NowPlaying(
    title = "Starman",
    artist = "David Bowie",
    path = "path11",
    position = 0,
    id = 11
  ),
  NowPlaying(
    title = "Angie",
    artist = "The Rolling Stones",
    path = "path12",
    position = 0,
    id = 12
  ),
  NowPlaying(
    title = "Purple Rain",
    artist = "Prince",
    path = "path13",
    position = 0,
    id = 13
  ),
  NowPlaying(
    title = "Baba O'Riley",
    artist = "The Who",
    path = "path14",
    position = 0,
    id = 14
  ),
  NowPlaying(
    title = "Rocket Man",
    artist = "Elton John",
    path = "path15",
    position = 0,
    id = 15
  ),
  NowPlaying(
    title = "Like a Rolling Stone",
    artist = "Bob Dylan",
    path = "path16",
    position = 0,
    id = 16
  ),
  NowPlaying(
    title = "London Calling",
    artist = "The Clash",
    path = "path17",
    position = 0,
    id = 17
  ),
  NowPlaying(
    title = "Light My Fire",
    artist = "The Doors",
    path = "path18",
    position = 0,
    id = 18
  ),
  NowPlaying(
    title = "Folsom Prison Blues",
    artist = "Johnny Cash",
    path = "path19",
    position = 0,
    id = 19
  ),
  NowPlaying(
    title = "I Got You (I Feel Good)",
    artist = "James Brown",
    path = "path20",
    position = 0,
    id = 20
  )
)

private val previewPlaylist = listOf(
  PlayingTrack(
    artist = "Caravan Palace",
    album = "Panic",
    title = "Rock It for Me",
    year = "2008"
  ),
  PlayingTrack(
    artist = "Daft Punk",
    album = "Random Access Memories",
    title = "Get Lucky",
    year = "2013"
  ),
  PlayingTrack(
    artist = "The Beatles",
    album = "Abbey Road",
    title = "Come Together",
    year = "1969"
  ),
  PlayingTrack(
    artist = "Queen",
    album = "A Night at the Opera",
    title = "Bohemian Rhapsody",
    year = "1975"
  ),
  PlayingTrack(
    artist = "Pink Floyd",
    album = "The Wall",
    title = "Comfortably Numb",
    year = "1979"
  ),
  PlayingTrack(
    artist = "Led Zeppelin",
    album = "IV",
    title = "Stairway to Heaven",
    year = "1971"
  ),
  PlayingTrack(
    artist = "Nirvana",
    album = "Nevermind",
    title = "Smells Like Teen Spirit",
    year = "1991"
  ),
  PlayingTrack(
    artist = "Michael Jackson",
    album = "Thriller",
    title = "Billie Jean",
    year = "1982"
  ),
  PlayingTrack(
    artist = "Radiohead",
    album = "OK Computer",
    title = "Paranoid Android",
    year = "1997"
  ),
  PlayingTrack(
    artist = "U2",
    album = "The Joshua Tree",
    title = "With or Without You",
    year = "1987"
  ),
  PlayingTrack(
    artist = "David Bowie",
    album = "The Rise and Fall of Ziggy Stardust",
    title = "Starman",
    year = "1972"
  ),
  PlayingTrack(
    artist = "The Rolling Stones",
    album = "Exile on Main St.",
    title = "Angie",
    year = "1972"
  ),
  PlayingTrack(
    artist = "Prince",
    album = "Purple Rain",
    title = "Purple Rain",
    year = "1984"
  ),
  PlayingTrack(
    artist = "The Who",
    album = "Who's Next",
    title = "Baba O'Riley",
    year = "1971"
  ),
  PlayingTrack(
    artist = "Elton John",
    album = "Goodbye Yellow Brick Road",
    title = "Rocket Man",
    year = "1972"
  ),
  PlayingTrack(
    artist = "Bob Dylan",
    album = "Highway 61 Revisited",
    title = "Like a Rolling Stone",
    year = "1965"
  ),
  PlayingTrack(
    artist = "The Clash",
    album = "London Calling",
    title = "London Calling",
    year = "1979"
  ),
  PlayingTrack(
    artist = "The Doors",
    album = "The Doors",
    title = "Light My Fire",
    year = "1967"
  ),
  PlayingTrack(
    artist = "Johnny Cash",
    album = "At Folsom Prison",
    title = "Folsom Prison Blues",
    year = "1968"
  ),
  PlayingTrack(
    artist = "James Brown",
    album = "Live at the Apollo",
    title = "I Got You (I Feel Good)",
    year = "1963"
  )
)

