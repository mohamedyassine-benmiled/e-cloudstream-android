package com.lagradost.cloudstream3.ui.resume

import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.resume.*
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.ui.result.VideoWatchState
import com.lagradost.cloudstream3.utils.DataStoreHelper
import org.junit.Assert.*
import org.junit.Test

class NativeResumeTest {
    @Test fun accountShelfAppearsOnlyInsideItsOwningProvider() {
        val owner = ResumeSourceKey("plugin-a", "provider-a")
        assertTrue(owner.belongsToHomeProvider("provider-a", "plugin-a"))
        assertFalse(owner.belongsToHomeProvider("provider-b", "plugin-a"))
        assertFalse(owner.belongsToHomeProvider("provider-a", "plugin-b"))
        assertFalse(owner.belongsToHomeProvider("provider-a", null))
        assertFalse(owner.belongsToHomeProvider(null, null))
    }

    private val episode = ResumeEpisode("series", "episode-sub", "12", 1, "sub")
    private val item = RemoteResumeEntry("item", "account", "Title", "https://example.test/title", episode,
        ResumeProgress(59_123, 120_000), "opaque", mediaType = "Anime")
    private val request = RemoteResumeRequest(ResumeSourceKey("plugin", "provider"), ResumeContext("0", "account"), item, 7)
    private val resolved = ResolvedResume(episode, item.detailUrl, ResumeProgress(59_123, 120_000), "episode-data")
    private fun playable(data: String = "episode-data", season: Int? = 1) = ResultEpisode(
        headerName = "Title", name = "Episode 12", poster = null, episode = 12, seasonIndex = null, season = season,
        data = data, apiName = "provider", id = 12, index = 11, position = 0, duration = 0, score = null,
        description = null, isFiller = null, tvType = TvType.Anime, parentId = 1, videoWatchState = VideoWatchState.None)

    @Test fun localAndRemotePresentationUseTheSameRawProgress() {
        val local = DataStoreHelper.ResumeWatchingResult("Title", item.detailUrl, "provider", TvType.Anime, null,
            DataStoreHelper.PosDur(59_123, 120_000), 12, 1, 12, 1, false)
        val remote = RemoteResumeCard(request)
        assertEquals(local.resumeCardState()?.progress, remote.resumeCardState()?.progress)
        assertEquals(local.resumeCardState()?.number, remote.resumeCardState()?.number)
        assertEquals(60_877L, remote.resumeCardState()?.progress?.remainingMs)
        assertNull(remote.id)
    }

    @Test fun typedRequestAndLaunchSurviveFragmentArgumentRoundTrips() {
        assertEquals(request, NativeResumeTransport.decodeRequest(NativeResumeTransport.encodeRequest(request)))
        val launch = RemoteResumeLaunch(request, resolved, 12)
        assertEquals(launch, NativeResumeTransport.decodeLaunch(NativeResumeTransport.encodeLaunch(launch)))
    }

    @Test fun invalidOrOversizedFragmentArgumentsFailClosed() {
        assertNull(NativeResumeTransport.decodeRequest(null))
        assertNull(NativeResumeTransport.decodeRequest("{}"))
        assertNull(NativeResumeTransport.decodeLaunch("not json"))
        assertNull(NativeResumeTransport.decodeRequest("x".repeat(131_073)))
    }

    @Test fun resolvedEpisodeDataAndAudioMustIdentifyTheRequestedEdition() {
        assertTrue(matchesNativeEpisode(resolved, DubStatus.Subbed, playable()))
        assertFalse(matchesNativeEpisode(resolved, DubStatus.Dubbed, playable()))
        assertFalse(matchesNativeEpisode(resolved, DubStatus.Subbed, playable("other-episode")))
    }

    @Test fun fallbackMatchingNeverMixesSeasonsOrRoundsSpecialNumbers() {
        val numeric = resolved.copy(episodeData = null)
        assertTrue(matchesNativeEpisode(numeric, DubStatus.Subbed, playable()))
        assertFalse(matchesNativeEpisode(numeric, DubStatus.Subbed, playable(season = 2)))
        assertFalse(matchesNativeEpisode(numeric.copy(episode = episode.copy(number = "12.5")), DubStatus.Subbed, playable()))
        assertFalse(matchesNativeEpisode(numeric.copy(episode = episode.copy(audio = "unknown")), DubStatus.Subbed, playable()))
    }

    @Test fun nativeSeekPreservesExistingCompletionPolicyAndUnknownDuration() {
        assertEquals(59_123L, initialResumePosition(59_123, 120_000))
        assertEquals(59_123L, initialResumePosition(59_123, null))
        assertEquals(95_999L, initialResumePosition(95_999, 100_000))
        assertEquals(0L, initialResumePosition(96_000, 100_000))
        assertEquals(0L, initialResumePosition(50, 0))
        assertEquals(0L, initialResumePosition(Long.MAX_VALUE, Long.MAX_VALUE))
    }

    @Test fun unknownDurationDoesNotInventAProgressBarOrRemainingTime() {
        val remote = RemoteResumeCard(request.copy(item = item.copy(progress = ResumeProgress(59_123))))
        assertNull(remote.resumeCardState()?.progress?.fraction)
        assertNull(remote.resumeCardState()?.progress?.remainingMs)
    }
}
