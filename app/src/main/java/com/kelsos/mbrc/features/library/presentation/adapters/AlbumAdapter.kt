package com.kelsos.mbrc.features.library.presentation.adapters

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.kelsos.mbrc.R
import com.kelsos.mbrc.features.library.MenuItemSelectedListener
import com.kelsos.mbrc.features.library.data.Album
import com.kelsos.mbrc.features.library.popup
import com.kelsos.mbrc.features.library.presentation.viewholders.AlbumViewHolder

class AlbumAdapter : PagingDataAdapter<Album, AlbumViewHolder>(DIFF_CALLBACK) {

  private var listener: MenuItemSelectedListener<Album>? = null

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlbumViewHolder {
    val holder = AlbumViewHolder.create(parent)
    holder.onIndicatorClick { view, position ->
      view.popup(R.menu.popup_album) { id ->
        val album = getItem(position) ?: return@popup
        listener?.onMenuItemSelected(id, album)
      }
    }

    holder.onPress { position ->
      val album = getItem(position) ?: return@onPress
      listener?.onItemClicked(album)
    }
    return holder
  }

  override fun onBindViewHolder(holder: AlbumViewHolder, position: Int) {
    val album = getItem(position)

    if (album != null) {
      holder.bindTo(album)
    } else {
      holder.clear()
    }
  }

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener<Album>) {
    this.listener = listener
  }

  companion object {
    val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Album>() {
      override fun areItemsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.id == newItem.id
      }

      override fun areContentsTheSame(oldItem: Album, newItem: Album): Boolean {
        return oldItem.album == newItem.album &&
          oldItem.artist == newItem.artist
      }
    }
  }
}