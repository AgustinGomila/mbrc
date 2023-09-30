package com.kelsos.mbrc.networking.protocol

import com.kelsos.mbrc.protocol.ProtocolAction
import com.kelsos.mbrc.protocol.ProtocolPingHandle
import com.kelsos.mbrc.protocol.ProtocolVersionUpdate
import com.kelsos.mbrc.protocol.SimpleLogCommand
import com.kelsos.mbrc.protocol.UpdateCover
import com.kelsos.mbrc.protocol.UpdateLastFm
import com.kelsos.mbrc.protocol.UpdateLfmRating
import com.kelsos.mbrc.protocol.UpdateLyrics
import com.kelsos.mbrc.protocol.UpdateMute
import com.kelsos.mbrc.protocol.UpdateNowPlayingTrack
import com.kelsos.mbrc.protocol.UpdateNowPlayingTrackMoved
import com.kelsos.mbrc.protocol.UpdateNowPlayingTrackRemoval
import com.kelsos.mbrc.protocol.UpdatePlayState
import com.kelsos.mbrc.protocol.UpdatePlaybackPositionCommand
import com.kelsos.mbrc.protocol.UpdatePlayerStatus
import com.kelsos.mbrc.protocol.UpdatePluginVersionCommand
import com.kelsos.mbrc.protocol.UpdateRating
import com.kelsos.mbrc.protocol.UpdateRepeat
import com.kelsos.mbrc.protocol.UpdateShuffle
import com.kelsos.mbrc.protocol.UpdateVolume
import org.koin.core.component.KoinComponent

class CommandFactoryImpl : CommandFactory, KoinComponent {

  @Suppress("ComplexMethod")
  override fun create(protocol: Protocol): ProtocolAction = when (protocol) {
    Protocol.NowPlayingCover -> getKoin().get<UpdateCover>()
    Protocol.NowPlayingLfmRating -> getKoin().get<UpdateLfmRating>()
    Protocol.NowPlayingListMove -> getKoin().get<UpdateNowPlayingTrackMoved>()
    Protocol.NowPlayingListRemove -> getKoin().get<UpdateNowPlayingTrackRemoval>()
    Protocol.NowPlayingLyrics -> getKoin().get<UpdateLyrics>()
    Protocol.NowPlayingPosition -> getKoin().get<UpdatePlaybackPositionCommand>()
    Protocol.NowPlayingRating -> getKoin().get<UpdateRating>()
    Protocol.NowPlayingTrack -> getKoin().get<UpdateNowPlayingTrack>()
    Protocol.Ping -> getKoin().get<ProtocolPingHandle>()
    Protocol.PlayerMute -> getKoin().get<UpdateMute>()
    Protocol.NowPlayingListPlay,
    Protocol.PlayerNext,
    Protocol.PlayerPrevious -> getKoin().get<SimpleLogCommand>()

    Protocol.PlayerRepeat -> getKoin().get<UpdateRepeat>()
    Protocol.PlayerScrobble -> getKoin().get<UpdateLastFm>()
    Protocol.PlayerShuffle -> getKoin().get<UpdateShuffle>()
    Protocol.PlayerState -> getKoin().get<UpdatePlayState>()
    Protocol.PlayerStatus -> getKoin().get<UpdatePlayerStatus>()
    Protocol.PlayerVolume -> getKoin().get<UpdateVolume>()
    Protocol.PluginVersion -> getKoin().get<UpdatePluginVersionCommand>()
    Protocol.Pong,
    Protocol.ProtocolTag -> getKoin().get<ProtocolVersionUpdate>()

    else -> error("Not supported message context $protocol")
  }
}

interface CommandFactory {
  fun create(protocol: Protocol): ProtocolAction
}
