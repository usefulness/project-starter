package com.starter.issuechecker

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class FunctionalTest {

    @Test
    internal fun `reports youtrack issue`() {
        val text =
            """
            Some text with 
            comment /* in https://youtrack.jetbrains.com/issue/KT-36808/ the middle 
            lol
            """.trimIndent()

        val result = IssueChecker(IssueChecker.Config()).reportBlocking(text)

        assertThat(result).containsExactly(
            CheckResult.Success(
                issueUrl = "https://youtrack.jetbrains.com/issue/KT-36808/",
                issueStatus = IssueStatus.Closed
            )
        )
    }

    @Test
    internal fun `finds all issue links`() {
        val text =
            """
            Some text with 
            comment /* in https://youtrack.jetbrains.com/issue/KT-36808/ the middle 
            lol
            """.trimIndent()

        val result = IssueChecker(IssueChecker.Config()).findAllLinksBlocking(text).single().map { it.toString() }

        assertThat(result).containsExactly("https://youtrack.jetbrains.com/issue/KT-36808/")
    }
}
