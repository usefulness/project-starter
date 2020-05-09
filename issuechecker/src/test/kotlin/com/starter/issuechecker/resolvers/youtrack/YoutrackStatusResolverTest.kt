package com.starter.issuechecker.resolvers.youtrack

import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.readJson
import com.starter.issuechecker.restApi
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
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

    private fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }
}
