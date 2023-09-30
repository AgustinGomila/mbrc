package com.kelsos.mbrc.features.nowplaying

import androidx.compose.material.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kelsos.mbrc.app.RemoteDestination

object NowPlayingDestination : RemoteDestination {
  override val route: String = "now_playing_route"
  override val destination: String = "now_playing_destination"
}

fun NavGraphBuilder.nowPlayingGraph(
  openDrawer: () -> Unit,
  navigateToHome: () -> Unit,
  snackbarHostState: SnackbarHostState,
) {
  composable(NowPlayingDestination.route) {
    NowPlayingScreen(
      snackbarHostState = snackbarHostState,
      openDrawer = openDrawer,
      navigateToHome = navigateToHome
    )
  }
}
