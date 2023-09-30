package com.kelsos.mbrc.features.nowplaying.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kelsos.mbrc.common.state.AppState
import com.kelsos.mbrc.common.utilities.AppCoroutineDispatchers
import com.kelsos.mbrc.features.library.PlayingTrack
import com.kelsos.mbrc.features.nowplaying.domain.MoveManager
import com.kelsos.mbrc.features.nowplaying.domain.NowPlaying
import com.kelsos.mbrc.features.nowplaying.repository.NowPlayingRepository
import com.kelsos.mbrc.networking.client.UserActionUseCase
import com.kelsos.mbrc.networking.client.moveTrack
import com.kelsos.mbrc.networking.client.playTrack
import com.kelsos.mbrc.networking.client.removeTrack
import com.kelsos.mbrc.networking.protocol.NowPlayingMoveRequest
import com.kelsos.mbrc.ui.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

interface INowPlayingActions {
  fun reload()
  fun search(query: String)
  fun moveTrack(from: Int, to: Int)
  fun removeTrack(position: Int)
  fun play(position: Int)
  fun move()
}

class NowPlayingActions(
  private val scope: CoroutineScope,
  private val moveManager: MoveManager,
  private val dispatchers: AppCoroutineDispatchers,
  private val repository: NowPlayingRepository,
  private val userActionUseCase: UserActionUseCase,
  private val emit: suspend (uiMessage: NowPlayingUiMessages) -> Unit
) : INowPlayingActions {
  override fun reload() {
    scope.launch(dispatchers.network) {
      val result = repository.getRemote()
        .fold(
          {
            NowPlayingUiMessages.RefreshFailed
          },
          {
            NowPlayingUiMessages.RefreshSuccess
          }
        )
      emit(result)
    }
  }

  override fun search(query: String) {
    scope.launch(dispatchers.database) {
      val position = repository.findPosition(query)
      if (position > 0) {
        play(position)
      }
    }
  }

  override fun moveTrack(from: Int, to: Int) {
    moveManager.move(from, to)
  }

  override fun play(position: Int) {
    scope.launch(dispatchers.network) {
      userActionUseCase.playTrack(position)
    }
  }

  override fun removeTrack(position: Int) {
    scope.launch(dispatchers.network) {
      delay(timeMillis = 400)
      userActionUseCase.removeTrack(position)
    }
  }

  override fun move() {
    moveManager.commit()
  }
}

class NowPlayingViewModel(
  private val dispatchers: AppCoroutineDispatchers,
  repository: NowPlayingRepository,
  moveManager: MoveManager,
  private val userActionUseCase: UserActionUseCase,
  appState: AppState
) : BaseViewModel<NowPlayingUiMessages>() {

  val list: Flow<PagingData<NowPlaying>> = repository.getAll().cachedIn(viewModelScope)
  val playingTracks: Flow<PlayingTrack> = appState.playingTrack

  val actions: NowPlayingActions = NowPlayingActions(
    scope = viewModelScope,
    moveManager = moveManager,
    dispatchers = dispatchers,
    repository = repository,
    userActionUseCase = userActionUseCase,
    emit = this::emit
  )

  init {
    moveManager.onMoveCommit { originalPosition, finalPosition ->
      viewModelScope.launch(dispatchers.network) {
        userActionUseCase.moveTrack(
          NowPlayingMoveRequest(
            originalPosition,
            finalPosition
          )
        )
      }
    }
  }
}
