package com.kelsos.mbrc.content.library.tracks

import androidx.paging.PagingData
import com.kelsos.mbrc.di.modules.AppDispatchers
import com.kelsos.mbrc.networking.ApiBase
import com.kelsos.mbrc.networking.protocol.Protocol
import com.kelsos.mbrc.utilities.epoch
import com.kelsos.mbrc.utilities.paged
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TrackRepositoryImpl
@Inject
constructor(
  private val dao: TrackDao,
  private val api: ApiBase,
  private val dispatchers: AppDispatchers
) : TrackRepository {

  private val mapper = TrackDtoMapper()

  override suspend fun getAll(): Flow<PagingData<Track>> = dao.getAll().paged()

  override suspend fun getAlbumTracks(
    album: String,
    artist: String
  ): Flow<PagingData<Track>> =
    dao.getAlbumTracks(album, artist).paged()

  override suspend fun getNonAlbumTracks(artist: String): Flow<PagingData<Track>> =
    dao.getNonAlbumTracks(artist).paged()

  override suspend fun getAndSaveRemote(): Flow<PagingData<Track>> {
    getRemote()
    return dao.getAll().paged()
  }

  override suspend fun getRemote() {
    withContext(dispatchers.io) {
      val added = epoch()
      api.getAllPages(Protocol.LibraryBrowseTracks, TrackDto::class).onCompletion {
        dao.removePreviousEntries(added)
      }.collect { items ->
        val tracks = items.map { mapper.map(it).apply { dateAdded = added } }
        dao.insertAll(tracks)
      }
    }
  }

  override suspend fun search(term: String): Flow<PagingData<Track>> {
    return dao.search(term).paged()
  }

  override suspend fun getGenreTrackPaths(genre: String): List<String> {
    return dao.getGenreTrackPaths(genre)
  }

  override suspend fun getArtistTrackPaths(artist: String): List<String> =
    dao.getArtistTrackPaths(artist)

  override suspend fun getAlbumTrackPaths(album: String, artist: String): List<String> =
    dao.getAlbumTrackPaths(album, artist)

  override suspend fun getAllTrackPaths(): List<String> = dao.getAllTrackPaths()

  override suspend fun cacheIsEmpty(): Boolean = dao.count() == 0L

  override suspend fun count(): Long = dao.count()
}
