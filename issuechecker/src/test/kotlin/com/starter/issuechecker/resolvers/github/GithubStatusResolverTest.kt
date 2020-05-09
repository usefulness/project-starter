package com.starter.issuechecker.resolvers.github

import com.starter.issuechecker.IssueStatus
import com.starter.issuechecker.readJson
import com.starter.issuechecker.restApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.HttpException
import java.net.URL

internal class GithubStatusResolverTest {

    private val server = MockWebServer()
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

    @Test
    internal fun `correctly interprets github error response`() = runBlockingTest {
        server.enqueue(MockResponse().setResponseCode(400))

        val result = runCatching { resolver.resolve(URL("https://github.com/apollographql/apollo-android/pull/2207")) }

        assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }

    @Test
    internal fun `matchers only issues and pull requests`() {
        val links = mapOf(
            "https://github.com/pinterest/ktlint/pull/730" to true,
            "https://github.com/pinterest/ktlint/issues/728" to true,
            "http://github.com/pinterest/ktlint/issues/728" to true,
            "https://www.github.com/pinterest/ktlint/issues/728" to true,
            "https://github.com/pinterest/ktlint/issues/728/" to true,
            "https://github.com/scottdweber" to false,
            "https://github.com/pinterest/ktlint/blob/master/CONTRIBUTING.md" to false
        )
        assertSoftly {
            links.forEach { (url, expected) ->
                val result = resolver.handles(URL(url))

                it.assertThat(result).withFailMessage("$url should ${if (expected) "" else "not "}match").isEqualTo(expected)
            }
        }
    }

    private fun runBlockingTest(block: suspend () -> Unit) = runBlocking { block() }
}
