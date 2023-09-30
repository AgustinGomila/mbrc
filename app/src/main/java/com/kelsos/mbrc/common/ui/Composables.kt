package com.kelsos.mbrc.common.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.DropdownMenu
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.contentColorFor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.kelsos.mbrc.R
import kotlinx.coroutines.flow.flow
import kotlin.math.roundToInt

fun <T : Any> pagingDataFlow(vararg elements: T) = flow {
  emit(PagingData.from(listOf(*elements)))
}

@Composable
fun RemoteTopAppBar(
  openDrawer: () -> Unit,
  title: String,
  content: (@Composable ColumnScope.() -> Unit)? = null
) = TopAppBar(
  backgroundColor = MaterialTheme.colors.primary,
  contentColor = contentColorFor(backgroundColor = MaterialTheme.colors.primary),
) {
  Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier.fillMaxWidth()
  ) {
    IconButton(onClick = { openDrawer() }) {
      Icon(
        imageVector = Icons.Filled.Menu,
        contentDescription = stringResource(id = R.string.navigation_menu_description)
      )
    }
    Text(
      modifier = Modifier.padding(start = 16.dp),
      text = title,
      style = MaterialTheme.typography.h6,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      textAlign = TextAlign.Start
    )
    Spacer(Modifier.weight(1f))
    if (content != null) {
      Column(content = content)
    }
  }
}

@Composable
fun EmptyScreen(
  modifier: Modifier = Modifier,
  text: String,
  imageVector: ImageVector,
  contentDescription: String,
  content: @Composable (ColumnScope.() -> Unit)? = null
) = Row(
  modifier = modifier,
  verticalAlignment = Alignment.CenterVertically,
  horizontalArrangement = Arrangement.Center,
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.weight(weight = 1f)
  ) {
    Text(text = text, style = MaterialTheme.typography.h5)
    Icon(
      imageVector = imageVector,
      contentDescription = contentDescription,
      modifier = Modifier.fillMaxSize(fraction = 0.2f)
    )
    content?.invoke(this)
  }
}

@Composable
fun <T : Any> ScreenContent(
  items: LazyPagingItems<T>,
  itemContent: @Composable() (LazyItemScope.(value: T?) -> Unit),
) {

  val listState = rememberLazyListState()

  LazyColumn(
    state = listState,
    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    itemsIndexed(items) { _, item ->
      itemContent(item)
    }
  }
}


@Composable
fun <T : Any> SwipeRefreshDragableScreen(
  modifier: Modifier = Modifier,
  content: SwipeScreenContent<T>,
  isLongPressed: Boolean,
  itemContent: @Composable (LazyItemScope.(value: T?) -> Unit),
) {
  SwipeRefresh(
    state = rememberSwipeRefreshState(content.isRefreshing),
    onRefresh = { content.onRefresh() },
    modifier = modifier
  ) {
    ScreenDragableContent(
      items = content.items,
      isLongPressed = isLongPressed,
      itemContent = itemContent
    )
  }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T : Any> ScreenDragableContent2(
  items: LazyPagingItems<T>,
  itemContent: @Composable() (LazyItemScope.(value: T?) -> Unit),
) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    val itemHeight = 64.dp

    itemsIndexed(items) { index, item ->
      val maxOffset = (itemHeight.value * 0.5f).toInt()
      var offsetX by remember { mutableStateOf(0f) }
      val offset = with(LocalDensity.current) { offsetX.dp.toPx() }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .offset { IntOffset(offset.roundToInt(), 0) }
          .swipeable(
            state = rememberSwipeableState(0),
            anchors = mapOf(0f to 0, maxOffset.toFloat() to 1),
            thresholds = { _, _ -> FractionalThreshold(0.5f) },
            orientation = Orientation.Vertical
          )
          .pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
              val newOffset = offsetX + dragAmount
              if (newOffset in 0f..maxOffset.toFloat()) {
                offsetX = newOffset
              }
            }
          }
      ) {
        itemContent(item)
      }
    }
  }
}

@Composable
fun <T : Any> ScreenDragableContent(
  items: LazyPagingItems<T>,
  isLongPressed: Boolean,
  itemContent: @Composable() (LazyItemScope.(value: T?) -> Unit),
) {
  LazyColumn(
    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 16.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    itemsIndexed(items) { index, item ->
      var offsetY by remember { mutableStateOf(0f) }
      var isSwipingRight by remember { mutableStateOf(false) }

      val modifier = Modifier
        .fillMaxWidth()

      if (isLongPressed) {
        modifier
          .offset { IntOffset(0, offsetY.roundToInt()) }
          .pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, rotation ->
              when {
                pan.x < 0 -> {
                  isSwipingRight = false
                }

                pan.x > 0 -> {
                  isSwipingRight = true
                  offsetY += pan.x
                }

                else -> {
                  isSwipingRight = false
                }
              }
            }
          }
          .pointerInput(Unit) {
            detectTransformGestures { centroid, pan, zoom, rotation ->
              when {
                pan.y < 0 -> {
                  if (!isSwipingRight) offsetY += pan.y
                }

                pan.y > 0 -> {
                  if (!isSwipingRight) offsetY += pan.y
                }

                else -> {
                  offsetY = 0f
                }
              }
            }
          }
      }

      Column(
        modifier = modifier
      ) {
        itemContent(item)
      }
    }
  }
}

data class SwipeScreenContent<T : Any>(
  val items: LazyPagingItems<T>,
  val key: (t: T) -> Long,
  val isRefreshing: Boolean,
  val onRefresh: () -> Unit
)

@Composable
fun <T : Any> SwipeRefreshScreen(
  modifier: Modifier = Modifier,
  content: SwipeScreenContent<T>,
  itemContent: @Composable (LazyItemScope.(value: T?) -> Unit),
) {
  SwipeRefresh(
    state = rememberSwipeRefreshState(content.isRefreshing),
    onRefresh = { content.onRefresh() },
    modifier = modifier
  ) {
    ScreenContent(
      items = content.items,
      itemContent = itemContent
    )
  }
}

@Composable
fun PopupMenu(menuContent: @Composable (ColumnScope.() -> Unit)) = Column {
  var showMenu by remember { mutableStateOf(false) }
  IconButton(onClick = { showMenu = !showMenu }, modifier = Modifier.padding(end = 16.dp)) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      contentDescription = stringResource(id = R.string.menu_overflow_description)
    )
  }
  DropdownMenu(
    expanded = showMenu,
    onDismissRequest = { showMenu = false },
    content = menuContent
  )
}

@Composable
fun PopupMenu(
  isVisible: Boolean,
  setVisible: (isVisible: Boolean) -> Unit,
  menuContent: @Composable (ColumnScope.() -> Unit)
) = Column {
  IconButton(onClick = { setVisible(!isVisible) }) {
    Icon(
      imageVector = Icons.Filled.MoreVert,
      contentDescription = stringResource(id = R.string.menu_overflow_description)
    )
  }
  DropdownMenu(
    expanded = isVisible,
    onDismissRequest = { setVisible(false) },
    content = menuContent
  )
}

@Preview
@Composable
fun EmptyScreenPreview() {
  Column(Modifier.fillMaxSize()) {
    EmptyScreen(
      modifier = Modifier.weight(1f),
      text = "Is Empty",
      imageVector = Icons.Filled.Warning,
      contentDescription = ""
    )
  }
}