package com.starter.issuechecker

import kotlinx.coroutines.runBlocking

class IssueChecker(
    val config: Config
) {

    data class Config(
        val githubToken: String? = null
    )

    fun reportBlocking(text: String): Collection<CheckResult> = runBlocking {
        defaultChecker(config).report(text = text)
    }

    suspend fun report(text: String): Collection<CheckResult> =
        defaultChecker(config).report(text = text)
}
