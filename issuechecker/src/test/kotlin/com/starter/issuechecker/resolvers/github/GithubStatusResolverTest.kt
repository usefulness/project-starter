package com.starter.issuechecker.resolvers.github

import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.readJson
import com.starter.issuechecker.restApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

internal class GithubStatusResolverTest {

    val server = MockWebServer()
    lateinit var resolver: GithubStatusResolver

    @BeforeEach
    internal fun setUp() {
        val api = restApi(server.url("/").toString())
        resolver = GithubStatusResolver(api.create(GithubService::class.java))
    }

    @Test
    internal fun `correctly interprets github response`() = runBlockingTest {
        server.enqueue(MockResponse().setBody(readJson("github.json")))

        val result = resolver.resolve(URL("https://github.com/apollographql/apollo-android/issues/2207"))

        assertThat(result).isEqualTo(IssueStatus.Closed)
    }

    private fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }
}
