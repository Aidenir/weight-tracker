package com.aidenir.weighttracker.homeassistant

import com.aidenir.weighttracker.data.HomeAssistantConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Minimal Home Assistant REST client.
 * See https://developers.home-assistant.io/docs/api/rest/
 */
class HomeAssistantClient(
    private val client: OkHttpClient = defaultClient()
) {

    /**
     * Returns the state of an entity, or null if the request failed / config is empty.
     * A binary_sensor bed sensor reports "on" (in bed) or "off" (out of bed).
     */
    suspend fun state(config: HomeAssistantConfig, entityId: String = config.bedEntity): String? {
        if (!config.isConfigured()) return null
        val url = "${config.baseUrl}/api/states/$entityId"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer ${config.token}")
            .addHeader("Content-Type", "application/json")
            .build()
        return withContext(Dispatchers.IO) {
            runCatching {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use null
                    val body = response.body?.string() ?: return@use null
                    parseState(body)
                }
            }.getOrNull()
        }
    }

    /** Naive JSON state extractor to avoid an extra dependency. */
    private fun parseState(body: String): String? {
        val key = "\"state\":\""
        val start = body.indexOf(key)
        if (start < 0) return null
        val from = start + key.length
        val end = body.indexOf('"', from)
        if (end < 0) return null
        return body.substring(from, end)
    }

    companion object {
        fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(8, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }
}
