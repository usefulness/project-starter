package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class KotlinQualityPluginTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("build.gradle") {
                writeText(
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.library.kotlin')
                    }
                    
                    """.trimIndent(),
                )
            }
            resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                writeText(kotlinClass("ValidKotlinFile1"))
            }
            resolve("src/test/kotlin/com/example/ValidKotlinTest1.kt") {
                writeText(kotlinClass("ValidKotlinTest1"))
            }
            resolve("src/test/java/com/example/ValidJavaTest1.java") {
                writeText(javaClass("ValidJavaTest1"))
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
