package com.project.starter.quality.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File

internal class IssueLinksCheckerTaskTest : WithGradleProjectTest() {

    lateinit var androidModuleRoot: File
    lateinit var kotlinModuleRoot: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ':module1', ':module2' """)

            resolve("build.gradle").writeText("")
            androidModuleRoot = resolve("module1") {
                val script =
                    // language=groovy
                    """
                    plugins {
                        id 'com.starter.library.android' 
                    }
                    
                    android {
                        namespace "com.example.module1"
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/com/example/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
            kotlinModuleRoot = resolve("module2") {
                val script =
                    // language=groovy
                    """
                    plugins {
                        id 'com.starter.library.kotlin' 
                    }

                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(script)
                }
                resolve("src/main/java/ValidJava2.java") {
                    writeText(javaClass("ValidJava2"))
                }
                resolve("src/test/java/com/example/ValidJavaTest2.java") {
                    writeText(javaClass("ValidJavaTest2"))
                }
            }
        }
    }

    @Test
    fun `does not warn on regular project`() {
        androidModuleRoot.resolve("src/main/kotlin/com/example/ValidKotlin.kt") {
            val randomLinks =
                // language=kotlin
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

        assertThat(result.task(":module1:issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    @Disabled("Google Issue tracker is not supported yet")
    fun `reports issuetracker issues`() {
        androidModuleRoot.resolve("src/main/kotlin/com/example/ValidKotlin.kt") {
            val randomLinks =
                // language=kotlin
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

        assertThat(result.task(":module1:issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `reports youtrack issues`() {
        val randomLinks =
            // language=kotlin
            """
                /**
                * https://news.ycombinator.com/
                * https://youtrack.jetbrains.com/issue/KT-31666 
                **/
                 object ValidKotlin {
                   // https://youtrack.jetbrains.com/issue/KT-34230
                 }
            """.trimIndent()

        assertSoftly { softly ->
            listOf("module1" to androidModuleRoot, "module2" to kotlinModuleRoot).forEach { (name, folder) ->
                folder.resolve("src/main/kotlin/com/example/ValidKotlin.kt") {
                    writeText(randomLinks)
                }

                val result = runTask(":$name:issueLinksReport")

                softly.assertThat(androidModuleRoot.resolve("build/reports/issue_comments.txt"))
                    .hasContent(
                        """
                        👉 https://youtrack.jetbrains.com/issue/KT-31666 (Closed)
                        ✅ https://youtrack.jetbrains.com/issue/KT-34230 (Opened)
                        """.trimIndent(),
                    )
                softly.assertThat(result.output).contains("\uD83D\uDC49 https://youtrack.jetbrains.com/issue/KT-31666 (Closed)")
                softly.assertThat(result.output).contains("✅ https://youtrack.jetbrains.com/issue/KT-34230 (Opened)")
                softly.assertThat(result.task(":$name:issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
            }
        }
    }

    @Test
    fun `reports github issues`() {
        androidModuleRoot.resolve("src/main/kotlin/com/example/ValidKotlin.kt") {
            val randomLinks =
                // language=kotlin
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

        assertThat(androidModuleRoot.resolve("build/reports/issue_comments.txt"))
            .hasContent(
                """
                ✅ https://github.com/isaacs/github/issues/5 (Opened)
                👉 https://github.com/apollographql/apollo-android/issues/2207 (Closed)
                """.trimIndent(),
            )
        assertThat(result.output).contains("✅ https://github.com/isaacs/github/issues/5 (Opened)")
        assertThat(result.output).contains("\uD83D\uDC49 https://github.com/apollographql/apollo-android/issues/2207 (Closed)")
        assertThat(result.task(":module1:issueLinksReport")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
