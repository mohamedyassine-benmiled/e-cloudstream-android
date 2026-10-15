package com.lagradost.cloudstream3.ui.resume

import com.fasterxml.jackson.module.kotlin.readValue
import com.lagradost.cloudstream3.mapper

/** Fragment arguments contain typed metadata only, never credentials or extracted stream links. */
object NativeResumeTransport {
    const val ARGUMENT = "nativeExtensionResume"
    private const val MAX_SIZE = 131_072
    fun encodeRequest(request: RemoteResumeRequest): String = mapper.writeValueAsString(request).also { require(it.length <= MAX_SIZE) }
    fun decodeRequest(json: String?): RemoteResumeRequest? = json?.takeIf { it.length <= MAX_SIZE }?.let {
        runCatching { mapper.readValue<RemoteResumeRequest>(it) }.getOrNull()
    }
    fun encodeLaunch(launch: RemoteResumeLaunch): String = mapper.writeValueAsString(launch).also { require(it.length <= MAX_SIZE) }
    fun decodeLaunch(json: String?): RemoteResumeLaunch? = json?.takeIf { it.length <= MAX_SIZE }?.let {
        runCatching { mapper.readValue<RemoteResumeLaunch>(it) }.getOrNull()
    }
}
