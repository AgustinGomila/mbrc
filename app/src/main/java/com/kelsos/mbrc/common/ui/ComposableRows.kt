package com.kelsos.mbrc.common.ui

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import arrow.fx.coroutines.onCancel
import com.kelsos.mbrc.features.nowplaying.domain.NowPlaying
import com.kelsos.mbrc.features.nowplaying.presentation.INowPlayingActions
import com.kelsos.mbrc.features.nowplaying.previewNowPlayingList
import kotlin.math.roundToInt

@Composable
fun SingleLineRow(
  text: String?,
  clicked: () -> Unit = {},
  menuContent: @Composable ColumnScope.() -> Unit
) = Row(
  modifier = Modifier
    .fillMaxWidth()
    .height(48.dp)
    .clickable { clicked() },
  verticalAlignment = Alignment.CenterVertically,
  horizontalArrangement = Arrangement.SpaceBetween,
) {
  Column(modifier = Modifier.weight(1f)) {
    Text(
      text = text ?: "",
      style = MaterialTheme.typography.body1,
      modifier = Modifier
        .padding(start = 16.dp),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
  PopupMenu(menuContent)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingRow(
  modifier: Modifier = Modifier,
  nowPlaying: NowPlaying,
  isCurrent: Boolean,
  isDragIconPressed: (Boolean) -> Unit,
  actions: INowPlayingActions,
) {
  val title = nowPlaying.title
  val position = remember { mutableStateOf(nowPlaying.position) }

  val offset = remember { mutableStateOf(Offset(0f, 0f)) }

  // TODO
  // val onRemove = actions.removeTrack(position)
  // val onMove = actions.moveTrack(0, 0)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .offset { IntOffset(offset.value.x.roundToInt(), offset.value.y.roundToInt()) }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(48.dp)
        .clickable { actions.play(position.value) }
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Column(
        modifier = Modifier
          .padding(start = 16.dp)
          .width(48.dp)
          .height(48.dp),
        Arrangement.Center
      ) {
        DraggableIconButton(
          onClick = {
            println("Is clicked...")
          },
          onLongClick = {
            println("Is dragging...")
            isDragIconPressed(true)
          },
          onDoubleClick = {
            println("Is double clicked...")
          }) {
          Icon(
            imageVector = Icons.Filled.DragIndicator,
            contentDescription = "Drag and drop",
          )
        }
      }
      Column(
        modifier = Modifier
          .weight(1f)
          .padding(start = 16.dp),
        Arrangement.SpaceAround
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.body1,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
      if (isCurrent) {
        Icon(
          modifier = Modifier.padding(0.dp, 0.dp, 16.dp, 0.dp),
          imageVector = Icons.Filled.PlayArrow,
          contentDescription = "Playing now",
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableIconButton(
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onDoubleClick: () -> Unit,
  enabled: Boolean = true,
  content: @Composable () -> Unit,
) {
  Box(
    modifier = Modifier
      .combinedClickable(
        enabled = enabled,
        onClickLabel = "Pause / Play",
        role = Role.Button,
        onLongClickLabel = "Drag and Drop",
        onLongClick = onLongClick,
        onDoubleClick = onDoubleClick,
        onClick = onClick,
      ),
    contentAlignment = Alignment.Center
  ) {
    content()
  }
}

@Composable
fun DoubleLineRow(
  lineOne: String?,
  lineTwo: String?,
  coverUrl: String?,
  clicked: () -> Unit = {},
  menuContent: @Composable ColumnScope.() -> Unit
) = Row(
  modifier = Modifier
    .fillMaxWidth()
    .height(72.dp)
    .clickable { clicked() }
    .padding(vertical = 8.dp),
  verticalAlignment = Alignment.CenterVertically,
) {
  if (coverUrl != null) {
    Column(
      modifier = Modifier
        .padding(start = 16.dp)
        .width(48.dp)
        .height(48.dp)
    ) {
      TrackCover(
        coverUrl = coverUrl,
        modifier = Modifier
          .size(48.dp),
        cornerRadius = 2.dp
      )
    }
  }
  Column(
    modifier = Modifier
      .weight(1f)
      .padding(start = 16.dp),
    Arrangement.SpaceAround
  ) {
    Text(
      text = lineOne ?: "",
      style = MaterialTheme.typography.body1,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
    Text(
      text = lineTwo ?: "",
      style = MaterialTheme.typography.subtitle2,
      color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
      maxLines = 1,
      overflow = TextOverflow.Ellipsis
    )
  }
  PopupMenu(menuContent)
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SingleLineRowPreview() {
  SingleLineRow(text = "Playlist") {}
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun NowPlayingRowPreview1() {
  NowPlayingRow(
    nowPlaying = previewNowPlayingList.last(),
    isCurrent = true,
    isDragIconPressed = { },
    actions = object : INowPlayingActions {
      override fun reload() = Unit
      override fun search(query: String) = Unit
      override fun play(position: Int) = Unit
      override fun moveTrack(from: Int, to: Int) = Unit
      override fun removeTrack(position: Int) = Unit
      override fun move() = Unit
    }
  )
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun NowPlayingRowPreview2() {
  NowPlayingRow(
    nowPlaying = previewNowPlayingList.last(),
    isCurrent = false,
    isDragIconPressed = { },
    actions = object : INowPlayingActions {
      override fun reload() = Unit
      override fun search(query: String) = Unit
      override fun play(position: Int) = Unit
      override fun moveTrack(from: Int, to: Int) = Unit
      override fun removeTrack(position: Int) = Unit
      override fun move() = Unit
    }
  )
}

@Preview(
  uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DoubleLineRowPreview() {
  DoubleLineRow(lineOne = "Album", lineTwo = "Artist", coverUrl = "") {}
}
