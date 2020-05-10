package com.project.starter.issuechecker.cli

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

internal class IssueCheckerCliTest {

    private val outContent = ByteArrayOutputStream()
    private val errContent = ByteArrayOutputStream()
    private val original = System.out to System.err

    @BeforeEach
    internal fun setUp() {
        System.setOut(PrintStream(outContent))
        System.setErr(PrintStream(errContent))
    }

    @AfterEach
    internal fun tearDown() {
        System.setOut(original.first)
        System.setErr(original.second)
    }

    @Test
    internal fun `recognizes all options`() {
        main(arrayOf("--debug", "--stacktrace", "--dry-run", "--source", "**.kt"))

        assertThat(outContent.toString()).containsPattern("Working dir: .*/issuechecker/cli")
            .containsPattern("Visiting file .*/cli/Main.kt")
            .contains("Completed")
    }

    @Test
    internal fun `recognizes source options`() {
        val link = "https://github.com/fixture/issue/issues/2137"

        main(arrayOf("--source", "**/IssueCheckerCliTest.kt"))

        assertThat(errContent.toString()).contains("-> Couldn't check url $link")
    }
}
