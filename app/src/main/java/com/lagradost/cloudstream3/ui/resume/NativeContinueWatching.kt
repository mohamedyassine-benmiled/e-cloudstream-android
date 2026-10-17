package com.lagradost.cloudstream3.ui.resume

import com.lagradost.cloudstream3.APIHolder
import com.lagradost.cloudstream3.CloudStreamApp.Companion.getKey
import com.lagradost.cloudstream3.CloudStreamApp.Companion.setKey
import com.lagradost.cloudstream3.DubStatus
import com.lagradost.cloudstream3.Score
import com.lagradost.cloudstream3.SearchQuality
import com.lagradost.cloudstream3.SearchResponse
import com.lagradost.cloudstream3.TvType
import com.lagradost.cloudstream3.resume.*
import com.lagradost.cloudstream3.ui.result.ResultEpisode
import com.lagradost.cloudstream3.utils.DataStoreHelper
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

data class RemoteResumeRequest(val owner: ResumeSourceKey, val context: ResumeContext, val item: RemoteResumeEntry, val revision: Long)
data class RemoteResumeLaunch(val request: RemoteResumeRequest, val resolved: ResolvedResume, val localEpisodeId: Int)

data class RemoteResumeCard(val request: RemoteResumeRequest) : SearchResponse {
    override val name = request.item.title
    override val url = request.item.detailUrl
    override val apiName = request.owner.providerId
    override var type: TvType? = TvType.entries.firstOrNull { it.name == request.item.mediaType } ?: TvType.Others
    override var posterUrl = request.item.posterUrl
    override var posterHeaders: Map<String, String>? = null
    override var id: Int? = null
    override var quality: SearchQuality? = null
    override var score: Score? = null
}

/** A visible source state, not a fabricated remote title or local-history record. */
data class NativeResumeNotice(val owner: ResumeSourceKey, val signIn: Boolean, override val name: String) : SearchResponse {
    override val url = "native-resume-notice:${owner.providerId}:$signIn"
    override val apiName = owner.providerId
    override var type: TvType? = TvType.Others
    override var posterUrl: String? = null
    override var posterHeaders: Map<String, String>? = null
    override var id: Int? = null
    override var quality: SearchQuality? = null
    override var score: Score? = null
}

data class ResumeCardState(val number: String?, val season: Int?, val audio: String?, val progress: ResumeProgress?)

fun SearchResponse.resumeCardState(): ResumeCardState? = when (this) {
    is RemoteResumeCard -> request.item.let { ResumeCardState(it.episode.number, it.episode.season, it.episode.audio, it.progress) }
    is DataStoreHelper.ResumeWatchingResult -> ResumeCardState(episode?.toString(), season, null,
        watchPos?.let { if (it.position >= 0 && it.duration > 0) ResumeProgress(it.position, it.duration) else null })
    else -> null
}

internal fun matchesNativeEpisode(target: ResolvedResume, dub: DubStatus, episode: ResultEpisode): Boolean {
    val audio = when (target.episode.audio?.lowercase()) {
        "sub", "subbed" -> DubStatus.Subbed
        "dub", "dubbed" -> DubStatus.Dubbed
        null, "none" -> null
        else -> if (target.episodeData == null) return false else null
    }
    if (audio != null && audio != dub) return false
    return if (target.episodeData != null) episode.data == target.episodeData
        else target.episode.number != null && episode.episode.toString() == target.episode.number && episode.season == target.episode.season
}

/** Same completion/restart threshold as the existing Android player, without multiplying timestamps. */
internal fun initialResumePosition(positionMs: Long, durationMs: Long?): Long {
    if (durationMs == null) return positionMs
    if (durationMs <= 0 || positionMs >= durationMs / 100 * 96 + (durationMs % 100 * 96 + 99) / 100) return 0
    return positionMs
}

data class NativeResumeShelf(
    val owner: ResumeSourceKey,
    val context: ResumeContext? = null,
    val cards: List<RemoteResumeCard> = emptyList(),
    val nextCursor: String? = null,
    val state: ResumeListState = ResumeListState.READY,
    val error: ResumeFailure? = null,
    val consumedCursors: Set<String> = emptySet(),
    val newEpisodes: Boolean = false,
)

/** Account rows belong to the selected provider, never to a global or same-named foreign plugin. */
internal fun ResumeSourceKey.belongsToHomeProvider(providerName: String?, providerPlugin: String?): Boolean =
    providerPlugin != null && providerId == providerName && pluginId == providerPlugin

/** Bounded remote cache. Fetching/resolving never writes the local watch/download databases. */
object NativeContinueWatching {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val permits = Semaphore(3)
    private val loadingMore = ConcurrentHashMap.newKeySet<ResumeSourceKey>()
    private val state = MutableStateFlow<List<NativeResumeShelf>>(emptyList())
    val shelves = state.asStateFlow()

    init {
        scope.launch {
            combine(NativeResumeBridge.changes, DataStoreHelper.selectedAccountNumberFlow) { revision, _ -> revision }
                .collectLatest { revision ->
                    state.value = emptyList()
                    val profile = DataStoreHelper.currentAccount
                    val sources = NativeResumeBridge.registeredSources().filter(::providerStillLoaded).sortedBy { it.providerId }
                    state.value = sources.map { NativeResumeShelf(it) }
                    coroutineScope {
                        sources.map { owner -> launch { permits.withPermit {
                            val shelf = firstPage(owner, profile, revision)
                            if (revision == NativeResumeBridge.changes.value && profile == DataStoreHelper.currentAccount) {
                                state.update { current -> current.map { if (it.owner == owner) shelf else it } }
                            }
                        } } }.joinAll()
                    }
                }
        }
    }

    fun refresh() = NativeResumeBridge.invalidate()
    private fun providerStillLoaded(owner: ResumeSourceKey): Boolean =
        APIHolder.getApiFromNameNull(owner.providerId)?.sourcePlugin == owner.pluginId

    private suspend fun accountMatches(owner: ResumeSourceKey, context: ResumeContext): Boolean {
        val result = NativeResumeBridge.getAccountId(owner, context.profileId)
        return result is ResumeCallResult.Success && result.value == context.accountId
    }

    private suspend fun firstPage(owner: ResumeSourceKey, profile: String, revision: Long): NativeResumeShelf {
        return when (val account = NativeResumeBridge.getAccountId(owner, profile)) {
            is ResumeCallResult.Failure -> NativeResumeShelf(owner, error = account.reason)
            is ResumeCallResult.Success -> {
                val id = account.value ?: return NativeResumeShelf(owner, state = ResumeListState.SIGNED_OUT)
                val context = ResumeContext(profile, id)
                val result = NativeResumeBridge.getContinueWatching(owner, ResumeListRequest(context))
                if (!accountMatches(owner, context)) return NativeResumeShelf(owner, state = ResumeListState.REAUTHENTICATION_REQUIRED)
                when (result) {
                    is ResumeCallResult.Failure -> NativeResumeShelf(owner, context, error = result.reason)
                    is ResumeCallResult.Success -> NativeResumeShelf(owner, context,
                        result.value.entries.map { RemoteResumeCard(RemoteResumeRequest(owner, context, it, revision)) }.filterNot(::hidden),
                        result.value.nextCursor, result.value.state)
                }
            }
        }
    }

    suspend fun loadMore(owner: ResumeSourceKey) = withContext(Dispatchers.IO) {
        if (!loadingMore.add(owner)) return@withContext
        try {
            val old = state.value.firstOrNull { it.owner == owner } ?: return@withContext
            val context = old.context ?: return@withContext
            val cursor = old.nextCursor ?: return@withContext
            val revision = NativeResumeBridge.changes.value
            permits.withPermit {
                if (!accountMatches(owner, context)) { refresh(); return@withPermit }
                val result = NativeResumeBridge.getContinueWatching(owner, ResumeListRequest(context, cursor))
                if (!accountMatches(owner, context)) { refresh(); return@withPermit }
                if (revision != NativeResumeBridge.changes.value || context.profileId != DataStoreHelper.currentAccount) return@withPermit
                state.update { current -> current.map { shelf ->
                    if (shelf.owner != owner || shelf.context != context || shelf.nextCursor != cursor) shelf
                    else when (result) {
                        is ResumeCallResult.Failure -> shelf.copy(error = result.reason)
                        is ResumeCallResult.Success -> {
                            val consumed = shelf.consumedCursors + cursor
                            if (result.value.nextCursor in consumed) shelf.copy(nextCursor = null, error = ResumeFailure.INVALID_RESPONSE)
                            else shelf.copy(cards = (shelf.cards + result.value.entries.map {
                                RemoteResumeCard(RemoteResumeRequest(owner, context, it, revision))
                            }).distinctBy { it.request.item.itemId }.filterNot(::hidden),
                                nextCursor = result.value.nextCursor, state = result.value.state, error = null, consumedCursors = consumed)
                        }
                    }
                } }
            }
        } finally { loadingMore.remove(owner) }
    }

    private fun hideKey(card: RemoteResumeCard): String {
        val r = card.request
        val identity = (listOf(r.owner.pluginId, r.owner.providerId, r.context.accountId, r.item.itemId) +
            if (r.item.resumeToken.startsWith("latest-v1:")) listOf("new", r.item.episode.episodeId) else emptyList())
            .joinToString("") { "${it.length}:$it" }
        val hash = MessageDigest.getInstance("SHA-256").digest(identity.toByteArray()).joinToString("") { "%02x".format(it) }
        return "${r.context.profileId}/native_resume_hidden/$hash"
    }
    internal fun hidden(card: RemoteResumeCard) = getKey<Boolean>(hideKey(card), false) == true
    fun hide(card: RemoteResumeCard) {
        if (!isCurrent(card.request)) return
        setKey(hideKey(card), true)
        NativeResumeExtras.hide(card)
        state.update { current -> current.map { it.copy(cards = it.cards.filterNot { entry -> entry.request == card.request }) } }
    }
    fun isCurrent(request: RemoteResumeRequest): Boolean =
        request.context.profileId == DataStoreHelper.currentAccount && request.revision == NativeResumeBridge.changes.value &&
            request.owner in NativeResumeBridge.registeredSources() && providerStillLoaded(request.owner)

    suspend fun resolve(request: RemoteResumeRequest): ResumeCallResult<ResolvedResume?> = withContext(Dispatchers.IO) {
        if (!isCurrent(request) || !accountMatches(request.owner, request.context)) return@withContext ResumeCallResult.Failure(ResumeFailure.NOT_REGISTERED)
        val result = NativeResumeBridge.resolveResume(request.owner, ResumeResolveRequest(request.context, request.item))
        if (isCurrent(request) && accountMatches(request.owner, request.context)) result else ResumeCallResult.Failure(ResumeFailure.NOT_REGISTERED)
    }
}
