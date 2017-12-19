package com.kelsos.mbrc.ui.navigation.library.genres

import android.app.Activity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.kelsos.mbrc.R
import com.kelsos.mbrc.content.library.genres.Genre
import com.kelsos.mbrc.databinding.ListitemSingleBinding
import javax.inject.Inject

class GenreEntryAdapter
@Inject
constructor(context: Activity) : RecyclerView.Adapter<GenreEntryAdapter.ViewHolder>() {
  private var data: List<Genre>? = null
  private var listener: MenuItemSelectedListener? = null
  private val inflater: LayoutInflater = LayoutInflater.from(context)

  fun setMenuItemSelectedListener(listener: MenuItemSelectedListener) {
    this.listener = listener
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = inflater.inflate(R.layout.listitem_single, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val genre = data?.get(holder.bindingAdapterPosition)

    genre?.let {
      holder.title.text = if (it.genre.isBlank()) holder.empty else genre.genre
      holder.indicator.setOnClickListener { createPopup(it, genre) }
      holder.itemView.setOnClickListener { listener?.onItemClicked(genre) }
    }
  }

  private fun createPopup(it: View, genre: Genre) {
    val popupMenu = PopupMenu(it.context, it)
    popupMenu.inflate(R.menu.popup_genre)
    popupMenu.setOnMenuItemClickListener { menuItem ->
      return@setOnMenuItemClickListener listener?.onMenuItemSelected(menuItem, genre) ?: false
    }
    popupMenu.show()
  }

  override fun getItemCount(): Int = data?.size ?: 0

  interface MenuItemSelectedListener {
    fun onMenuItemSelected(menuItem: MenuItem, genre: Genre): Boolean

    fun onItemClicked(genre: Genre)
  }

  class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView
    val indicator: ImageView
    val empty: String by lazy { itemView.context.getString(R.string.empty) }

    init {
      val binding = ListitemSingleBinding.bind(itemView)
      title = binding.lineOne
      indicator = binding.uiItemContextIndicator
    }
  }

  fun update(cursor: List<Genre>) {
    data = cursor
    notifyDataSetChanged()
  }
}
