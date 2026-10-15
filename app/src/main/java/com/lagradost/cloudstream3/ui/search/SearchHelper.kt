package com.lagradost.cloudstream3.ui.search

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.lagradost.cloudstream3.plugins.Plugin
import com.lagradost.cloudstream3.plugins.PluginManager
import com.lagradost.cloudstream3.ui.resume.NativeContinueWatching
import com.lagradost.cloudstream3.ui.resume.NativeResumeNotice
import com.lagradost.cloudstream3.ui.resume.RemoteResumeCard
import com.lagradost.cloudstream3.ui.result.START_ACTION_NATIVE_RESUME
import com.lagradost.cloudstream3.CommonActivity.activity
import com.lagradost.cloudstream3.CommonActivity.showToast
import com.lagradost.cloudstream3.MainActivity
import com.lagradost.cloudstream3.R
import com.lagradost.cloudstream3.ui.download.DOWNLOAD_ACTION_PLAY_FILE
import com.lagradost.cloudstream3.ui.download.DownloadButtonSetup.handleDownloadClick
import com.lagradost.cloudstream3.ui.download.DownloadClickEvent
import com.lagradost.cloudstream3.ui.result.START_ACTION_LOAD_EP
import com.lagradost.cloudstream3.utils.AppContextUtils.loadSearchResult
import com.lagradost.cloudstream3.utils.DataStoreHelper
import com.lagradost.cloudstream3.utils.downloader.DownloadObjects

object SearchHelper {
    fun handleSearchClickCallback(callback: SearchClickCallback) {
        val card = callback.card
        if (card is NativeResumeNotice) {
            if (callback.action == SEARCH_ACTION_FOCUSED) return
            if (card.signIn) {
                (PluginManager.plugins[card.owner.pluginId] as? Plugin)?.openSettings?.invoke(callback.view.context)
            } else NativeContinueWatching.refresh()
            return
        }
        if (card is RemoteResumeCard) {
            when (callback.action) {
                SEARCH_ACTION_PLAY_FILE -> loadSearchResult(card, START_ACTION_NATIVE_RESUME)
                SEARCH_ACTION_LOAD -> loadSearchResult(card)
                SEARCH_ACTION_SHOW_METADATA -> AlertDialog.Builder(callback.view.context)
                    .setTitle(card.name)
                    .setItems(arrayOf(
                        callback.view.context.getString(R.string.native_resume_action),
                        callback.view.context.getString(R.string.native_resume_open_title),
                        callback.view.context.getString(R.string.native_resume_hide),
                    )) { _, action -> when (action) {
                        0 -> loadSearchResult(card, START_ACTION_NATIVE_RESUME)
                        1 -> loadSearchResult(card)
                        2 -> NativeContinueWatching.hide(card)
                    } }.show()
            }
            return
        }
        when (callback.action) {
            SEARCH_ACTION_LOAD -> {
                loadSearchResult(card)
            }

            SEARCH_ACTION_PLAY_FILE -> {
                if (card is DataStoreHelper.ResumeWatchingResult) {
                    val id = card.id
                    if (id == null) {
                        showToast(R.string.error_invalid_id, Toast.LENGTH_SHORT)
                    } else {
                        if (card.isFromDownload) {
                            handleDownloadClick(
                                DownloadClickEvent(
                                    DOWNLOAD_ACTION_PLAY_FILE,
                                    DownloadObjects.DownloadEpisodeCached(
                                        name = card.name,
                                        poster = card.posterUrl,
                                        episode = card.episode ?: 0,
                                        season = card.season,
                                        id = id,
                                        parentId = card.parentId ?: return,
                                        score = null,
                                        description = null,
                                        cacheTime = System.currentTimeMillis(),
                                    )
                                )
                            )
                        } else {
                            loadSearchResult(card, START_ACTION_LOAD_EP, id)
                        }
                    }
                } else {
                    handleSearchClickCallback(
                        SearchClickCallback(SEARCH_ACTION_LOAD, callback.view, -1, callback.card)
                    )
                }
            }

            SEARCH_ACTION_SHOW_METADATA -> {
                (activity as? MainActivity?)?.apply {
                    loadPopup(callback.card)
                } ?: kotlin.run {
                    showToast(callback.card.name, Toast.LENGTH_SHORT)
                }
            }
        }
    }
}
