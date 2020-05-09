package com.starter.issuechecker

import com.starter.issuechecker.resolvers.StatusResolver
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

internal class DefaultCheckerTest {

    private val statusResolver = FakeStatusResolver()
    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var checker: DefaultChecker

    @BeforeEach
    internal fun setUp() {
        checker = DefaultChecker(
            supportedTrackers = mapOf("fixture.url" to statusResolver),
            dispatcher = testDispatcher
        )
    }

    @Test
    fun `does not warn on regular file`() = testDispatcher.runBlockingTest {
        @Language("kotlin")
        val randomLinks =
            """
            /**
            * http://storage.googleapis.com/r8-releases/raw
            **/
             object ValidKotlin {
               // https://www.example.com
             }
            """.trimIndent()

        val result = checker.report(randomLinks)

        assertThat(result).isEmpty()
    }

    @Test
    fun `reports links in comments`() = testDispatcher.runBlockingTest {
        statusResolver.responses["https://fixture.url/issue/1"] = IssueStatus.Closed
        statusResolver.responses["http://fixture.url/issue/2"] = IssueStatus.Open
        @Language("kotlin")
        val randomLinks =
            """
            /**
            * https://fixture.url/issue/1
            **/
             object ValidKotlin {
               // https://issuetracker.google.com/issues/121092282
               val animations = 0 // Set animation: http://fixture.url/issue/2
             }
            """.trimIndent()

        val result = checker.report(randomLinks)

        assertThat(result).containsExactly(
            CheckResult.Success(
                issueUrl = "https://fixture.url/issue/1",
                issueStatus = IssueStatus.Closed
            ),
            CheckResult.Success(
                issueUrl = "http://fixture.url/issue/2",
                issueStatus = IssueStatus.Open
            )
        )
    }
}

internal class FakeStatusResolver : StatusResolver {

    val responses = mutableMapOf<String, IssueStatus>()

    override suspend fun resolve(url: URL) = responses.getValue(url.toString())
}
