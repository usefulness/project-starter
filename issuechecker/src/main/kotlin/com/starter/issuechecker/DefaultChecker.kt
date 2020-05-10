package com.starter.issuechecker

import com.starter.issuechecker.resolvers.StatusResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.URL

internal class DefaultChecker internal constructor(
    private val supportedTrackers: Set<StatusResolver>,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getLinks(text: String) = withContext(dispatcher) {
        val result = linkPattern.findAll(text)
        result.mapNotNull { matcher -> URL(matcher.value) }
            .groupBy { url ->
                supportedTrackers.firstOrNull { it.handles(url) }
            }
    }

    suspend fun report(text: String): Set<CheckResult> = coroutineScope {
        val tasks = getLinks(text).flatMap { (resolver, urls) ->
            urls.mapNotNull { url ->
                resolver ?: return@mapNotNull null
                async {
                    runCatching {
                        CheckResult.Success(
                            issueUrl = url.toString(),
                            issueStatus = resolver.resolve(url = url)
                        )
                    }
                        .getOrElse {
                            CheckResult.Error(
                                issueUrl = url.toString(),
                                throwable = it
                            )
                        }
                }
            }
        }

        tasks.awaitAll().toSet()
    }

    companion object {

        private val linkPattern by lazy {
            "https?://(www\\.)?[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_+.~#?&/=]*)".toRegex()
        }
    }
}
