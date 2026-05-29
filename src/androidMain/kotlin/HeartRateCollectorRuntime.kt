package com.heartwith.app

import android.content.Context
import com.heartwith.shared.HeartwithApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

object HeartRateCollectorRuntime {
    private var collector: AndroidHeartRateCollector? = null
    private var serverUrlKey: String? = null

    @Synchronized
    fun get(context: Context, serverUrl: String): AndroidHeartRateCollector {
        val normalizedServerUrl = serverUrl.trimEnd('/')
        val current = collector
        if (current != null && serverUrlKey == normalizedServerUrl) {
            return current
        }
        current?.let { old ->
            runCatching {
                runBlocking {
                    withTimeout(3_000L) {
                        old.disconnect().join()
                    }
                }
            }
        }
        return AndroidHeartRateCollector(
            context = context.applicationContext,
            api = HeartwithApi(normalizedServerUrl),
            serverUrl = normalizedServerUrl,
        ).also {
            collector = it
            serverUrlKey = normalizedServerUrl
        }
    }

    @Synchronized
    fun clearIfCurrent(instance: AndroidHeartRateCollector?) {
        if (collector == instance) {
            collector = null
            serverUrlKey = null
        }
    }
}
