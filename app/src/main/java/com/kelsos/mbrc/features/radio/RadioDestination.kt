package com.kelsos.mbrc.features.radio

import androidx.compose.material.SnackbarHostState
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.kelsos.mbrc.app.RemoteDestination

object RadioDestination : RemoteDestination {
  override val route: String = "radio_route"
  override val destination: String = "radio_destination"
}

fun NavGraphBuilder.radioGraph(
  openDrawer: () -> Unit,
  navigateToHome: () -> Unit,
  snackbarHostState: SnackbarHostState,
) {
  composable(RadioDestination.route) {
    RadioScreen(
      snackbarHostState = snackbarHostState,
      openDrawer = openDrawer,
      navigateToHome = navigateToHome
    )
  }
}
