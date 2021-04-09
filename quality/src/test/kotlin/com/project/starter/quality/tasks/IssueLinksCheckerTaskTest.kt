package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class IssueLinksCheckerTaskTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            @Language("groovy")
            val script =
                """
                    plugins {
                        id 'com.starter.quality'
                        id 'org.jetbrains.kotlin.jvm'
                    }
                    

                """.trimIndent()
            resolve("build.gradle") {
                writeText(script)
            }
            resolve("src/main/java/ValidJava2.java") {
                writeText(javaClass("ValidJava2"))
            }
            resolve("src/test/java/ValidJavaTest2.java") {
                writeText(javaClass("ValidJavaTest2"))
            }
        }
    }

    @Test
    fun `does not warn on regular project`() {
        rootDirectory.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://issuetracker.google.com/issues/145439806
                **/
                 object ValidKotlin {
                   // https://www.example.com
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinksReport")

        assertThat(result.task(":issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    @Disabled("Google Issue tracker is not supported yet")
    fun `reports issuetracker issues`() {
        rootDirectory.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://news.ycombinator.com/
                **/
                 object ValidKotlin {
                   // https://issuetracker.google.com/issues/121092282
                   val animations = 0 // Set animation: https://issuetracker.google.com/issues/154643058
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinksReport")

        assertThat(result.task(":issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `reports youtrack issues`() {
        @Language("kotlin")
        val randomLinks =
            """
                /**
                * https://news.ycombinator.com/
                * https://youtrack.jetbrains.com/issue/KT-31666 
                **/
                 object ValidKotlin {
                   // https://youtrack.jetbrains.com/issue/KT-34230
                 }
            """.trimIndent()

        rootDirectory.resolve("src/main/kotlin/ValidKotlin.kt") {
            writeText(randomLinks)
        }

        val result = runTask(":issueLinksReport")

        assertThat(rootDirectory.resolve("build/reports/issue_comments.txt"))
            .hasContent(
                """
                        👉 https://youtrack.jetbrains.com/issue/KT-31666 (Closed)
                        ✅ https://youtrack.jetbrains.com/issue/KT-34230 (Opened)
                """.trimIndent()
            )
        assertThat(result.output).contains("\uD83D\uDC49 https://youtrack.jetbrains.com/issue/KT-31666 (Closed)")
        assertThat(result.output).contains("✅ https://youtrack.jetbrains.com/issue/KT-34230 (Opened)")
        assertThat(result.task(":issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `reports github issues`() {
        rootDirectory.resolve("src/main/kotlin/ValidKotlin.kt") {
            @Language("kotlin")
            val randomLinks =
                """
                /**
                * https://github.com/isaacs/github/issues/5
                **/
                 object ValidKotlin {
                   // https://www.example.com
                   // https://github.com/apollographql/apollo-android/issues/2207 <- closed
                 }
                """.trimIndent()
            writeText(randomLinks)
        }

        val result = runTask("issueLinksReport")

        assertThat(rootDirectory.resolve("build/reports/issue_comments.txt"))
            .hasContent(
                """
                ✅ https://github.com/isaacs/github/issues/5 (Opened)
                👉 https://github.com/apollographql/apollo-android/issues/2207 (Closed)
                """.trimIndent()
            )
        assertThat(result.output).contains("✅ https://github.com/isaacs/github/issues/5 (Opened)")
        assertThat(result.output).contains("\uD83D\uDC49 https://github.com/apollographql/apollo-android/issues/2207 (Closed)")
        assertThat(result.task(":issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
