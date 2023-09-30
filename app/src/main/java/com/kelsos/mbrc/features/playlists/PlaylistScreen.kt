package com.kelsos.mbrc.features.playlists

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.kelsos.mbrc.R
import com.kelsos.mbrc.common.state.domain.PlayerState
import com.kelsos.mbrc.common.state.models.PlayingPosition
import com.kelsos.mbrc.common.ui.EmptyScreen
import com.kelsos.mbrc.common.ui.RemoteTopAppBar
import com.kelsos.mbrc.common.ui.SingleLineRow
import com.kelsos.mbrc.common.ui.SwipeRefreshScreen
import com.kelsos.mbrc.common.ui.SwipeScreenContent
import com.kelsos.mbrc.common.ui.pagingDataFlow
import com.kelsos.mbrc.features.library.PlayingTrack
import com.kelsos.mbrc.features.minicontrol.MiniControl
import com.kelsos.mbrc.features.minicontrol.MiniControlState
import com.kelsos.mbrc.features.minicontrol.MiniControlViewModel
import com.kelsos.mbrc.theme.RemoteTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.koin.androidx.compose.getViewModel

@Composable
fun PlaylistScreen(
  snackbarHostState: SnackbarHostState,
  openDrawer: () -> Unit,
  navigateToHome: () -> Unit,
) {
  val vm = getViewModel<PlaylistViewModel>()
  val miniVm = getViewModel<MiniControlViewModel>()
  val vmState by miniVm.state.collectAsState(initial = MiniControlState())

  PlaylistScreen(
    snackbarHostState = snackbarHostState,
    playlists = vm.playlists.collectAsLazyPagingItems(),
    events = vm.emitter,
    openDrawer = openDrawer,
    actions = vm.actions
  ) {
    MiniControl(
      vmState = vmState,
      perform = { miniVm.perform(it) },
      navigateToHome = navigateToHome
    )
  }
}

@Composable
fun PlaylistScreen(
  snackbarHostState: SnackbarHostState,
  playlists: LazyPagingItems<Playlist>,
  events: Flow<PlaylistUiMessages>,
  openDrawer: () -> Unit,
  actions: IPlaylistActions,
  content: @Composable () -> Unit
) = Surface {
  Column(modifier = Modifier.fillMaxSize()) {
    RemoteTopAppBar(openDrawer = openDrawer, title = stringResource(id = R.string.nav_playlists)) {
      Row {
        IconButton(onClick = { /*TODO*/ }) {
          Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = stringResource(id = R.string.library_search_hint)
          )
        }
      }
    }

    val messages = mapOf(
      PlaylistUiMessages.RefreshSuccess to stringResource(id = R.string.playlists__refresh_success),
      PlaylistUiMessages.RefreshFailed to stringResource(id = R.string.playlists__refresh_failed)
    )

    LaunchedEffect(snackbarHostState) {
      events.collect { message ->
        snackbarHostState.showSnackbar(messages.getValue(message))
      }
    }

    val isRefreshing = playlists.loadState.refresh is LoadState.Loading

    if (playlists.itemCount == 0) {
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
      SwipeRefreshScreen(
        modifier = Modifier.weight(1f),
        content = SwipeScreenContent(
          items = playlists,
          isRefreshing = isRefreshing,
          key = { it.id },
          onRefresh = actions::reload
        )
      ) {
        PlaylistRow(playlist = it, clicked = actions::play)
      }
    }

    Row {
      content()
    }
  }
}

@Composable
fun PlaylistRow(playlist: Playlist?, clicked: (path: String) -> Unit) =
  SingleLineRow(text = playlist?.name) {
    playlist?.let { it ->
      clicked(it.url)
    }
  }

@Preview(device = Devices.PIXEL_4)
@Composable
fun PlaylistScreenPreview() {
  RemoteTheme {
    PlaylistScreen(
      snackbarHostState = SnackbarHostState(),
      playlists = pagingDataFlow(
        playlists.first()
      ).collectAsLazyPagingItems(),
      events = emptyFlow(),
      openDrawer = {},
      actions = object : IPlaylistActions {
        override fun play(path: String) = Unit
        override fun reload() = Unit
      }
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

val playlists = listOf(
  Playlist(
    name = "Heavy Metal",
    url = """C:\library\metal.m3u""",
    id = 1
  ),
  Playlist(
    name = "Rock Classics",
    url = """C:\library\rock_classics.m3u""",
    id = 2
  ),
  Playlist(
    name = "80s Pop",
    url = """C:\library\80s_pop.m3u""",
    id = 3
  ),
  Playlist(
    name = "Chill Out",
    url = """C:\library\chill_out.m3u""",
    id = 4
  ),
  Playlist(
    name = "Hip Hop Hits",
    url = """C:\library\hip_hop_hits.m3u""",
    id = 5
  ),
  Playlist(
    name = "Country Road",
    url = """C:\library\country_road.m3u""",
    id = 6
  ),
  Playlist(
    name = "Classical Symphony",
    url = """C:\library\classical_symphony.m3u""",
    id = 7
  ),
  Playlist(
    name = "Jazz Grooves",
    url = """C:\library\jazz_grooves.m3u""",
    id = 8
  ),
  Playlist(
    name = "Indie Vibes",
    url = """C:\library\indie_vibes.m3u""",
    id = 9
  ),
  Playlist(
    name = "Reggae Roots",
    url = """C:\library\reggae_roots.m3u""",
    id = 10
  ),
  Playlist(
    name = "Electronic Beats",
    url = """C:\library\electronic_beats.m3u""",
    id = 11
  ),
  Playlist(
    name = "Pop Divas",
    url = """C:\library\pop_divas.m3u""",
    id = 12
  ),
  Playlist(
    name = "Latin Fiesta",
    url = """C:\library\latin_fiesta.m3u""",
    id = 13
  ),
  Playlist(
    name = "Blues Legends",
    url = """C:\library\blues_legends.m3u""",
    id = 14
  ),
  Playlist(
    name = "R&B Soul",
    url = """C:\library\rnb_soul.m3u""",
    id = 15
  ),
  Playlist(
    name = "Alternative Rock",
    url = """C:\library\alternative_rock.m3u""",
    id = 16
  ),
  Playlist(
    name = "Funk Grooves",
    url = """C:\library\funk_grooves.m3u""",
    id = 17
  ),
  Playlist(
    name = "Pop Punk",
    url = """C:\library\pop_punk.m3u""",
    id = 18
  ),
  Playlist(
    name = "Acoustic Ballads",
    url = """C:\library\acoustic_ballads.m3u""",
    id = 19
  ),
  Playlist(
    name = "EDM Party Mix",
    url = """C:\library\edm_party_mix.m3u""",
    id = 20
  )
)
