package com.starter.issuechecker

import kotlinx.coroutines.runBlocking
import java.net.URL

class IssueChecker(
    val config: Config
) {

    data class Config(
        val githubToken: String? = null
    )
    private val checker = defaultChecker(config)

    fun findAllLinksBlocking(text: String): Collection<List<URL>> = runBlocking {
        checker.getLinks(text = text).filterKeys { it != null }.values
    }

    fun reportBlocking(text: String): Collection<CheckResult> = runBlocking {
        checker.report(text = text)
    }

    suspend fun report(text: String): Collection<CheckResult> =
        checker.report(text = text)
}
