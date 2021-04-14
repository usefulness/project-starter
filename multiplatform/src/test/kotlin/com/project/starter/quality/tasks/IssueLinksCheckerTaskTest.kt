package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IssueLinksCheckerTaskTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            //language=groovy
            val script =
                """
                    plugins {
                        id('com.starter.library.multiplatform')
                    }
                    
                    kotlin {
                        jvm()
                        ios()
                    }

                """.trimIndent()
            resolve("build.gradle") {
                writeText(script)
            }
        }
    }

    @Test
    fun `reports issue tracker issues`() {
        //language=kotlin
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

        rootDirectory.resolve("src/commonMain/kotlin/ValidKotlin.kt") {
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
}
