package com.kelsos.mbrc.features.nowplaying

import androidx.paging.PagingSource
import androidx.paging.PagingState
import coil.network.HttpException
import com.kelsos.mbrc.features.nowplaying.domain.NowPlaying
import java.io.IOException

class NowPlayingPagingSource(private val nowPlayingList: List<NowPlaying>) :
  PagingSource<Int, NowPlaying>() {

  override suspend fun load(params: LoadParams<Int>): LoadResult<Int, NowPlaying> {
    return try {
      val page = params.key ?: 0
      val pageSize = params.loadSize
      val start = page * pageSize
      val end = start + pageSize
      val sublist = nowPlayingList.subList(start, minOf(end, nowPlayingList.size))
      LoadResult.Page(
        data = sublist,
        prevKey = if (page == 0) null else page - 1,
        nextKey = if (end >= nowPlayingList.size) null else page + 1
      )
    } catch (e: IOException) {
      return LoadResult.Error(e)
    } catch (e: HttpException) {
      return LoadResult.Error(e)
    }
  }

  override fun getRefreshKey(state: PagingState<Int, NowPlaying>): Int? {
    return null
  }
}
