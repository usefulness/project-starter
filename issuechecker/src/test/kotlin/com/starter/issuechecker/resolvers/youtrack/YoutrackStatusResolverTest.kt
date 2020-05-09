package com.starter.issuechecker.resolvers.youtrack

import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.readJson
import com.starter.issuechecker.restApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

internal class YoutrackStatusResolverTest {

    val server = MockWebServer()
    lateinit var resolver: YoutrackStatusResolver

    @BeforeEach
    internal fun setUp() {
        val api = restApi(server.url("/").toString())
        resolver = YoutrackStatusResolver(api.create(YoutrackService::class.java))
    }

    @Test
    internal fun `correctly interprets youtrack response`() = runBlockingTest {
        server.enqueue(MockResponse().setBody(readJson("youtrack.json")))

        val result = resolver.resolve(URL("https://youtrack.jetbrains.com/issue/KT-34230"))

        assertThat(result).isEqualTo(IssueStatus.Open)
    }

    @Test
    internal fun `matchers only issues`() {
        val links = mapOf(
            "https://youtrack.jetbrains.com/issue/KT-34230" to true,
            "https://www.youtrack.jetbrains.com/issue/KT-34230" to true,
            "https://youtrack.jetbrains.com/issue/KT-34230/" to true,
            "https://www.jetbrains.com/help/youtrack/standalone/api-entities.html" to false
        )
        SoftAssertions.assertSoftly {
            links.forEach { (url, expected) ->
                val result = resolver.handles(URL(url))

                it.assertThat(result).withFailMessage("$url should ${if (expected) "" else "not "}match").isEqualTo(expected)
            }
        }
    }

    private fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }
}
